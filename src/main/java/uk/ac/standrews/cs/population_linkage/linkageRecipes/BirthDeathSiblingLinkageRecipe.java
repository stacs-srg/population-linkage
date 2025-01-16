/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic.siblingBirthDatesAreViable;

/**
 * Links a person appearing as the child on a birth record with a sibling appearing as the deceased on a death record.
 */
public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.36;
    private static double MAX_THRESHOLD = 0;

    public static final String LINKAGE_TYPE = "birth-death-sibling";

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Death.STANDARDISED_ID;

    public static final int ALL_LINKAGE_FIELDS = 4;
    private final int number_of_births;

    // Don't use father/mother occupation due to likely long duration between birth and death events.

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.MOTHER_IDENTITY, Death.MOTHER_IDENTITY),
                    pair(Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY)));

    public BirthDeathSiblingLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name);
        if (number_of_records.equals(EVERYTHING_STRING)) {
            number_of_births = EVERYTHING;
        } else {
            number_of_births = Integer.parseInt(number_of_records);
        }
        setNumberOfLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    @Override
    public Iterable<LXP> getBirthRecords() {
        return RecordFiltering.filter(getNumberOfLinkageFieldsRequired(), number_of_births, super.getBirthRecords(), getLinkageFields());
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    /**
     * Checks:
     *    1. whether the age difference between the potential siblings is plausible.
     *    2. If the two primaries are actually the same person (to prevent self links)
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP birth_record, final LXP death_record) {
        final StringMeasure base_measure = Constants.LEVENSHTEIN;;
        final LXPMeasure composite_measure_bd = getCompositeMeasureBirthDeath(base_measure);
        RecordRepository record_repository = new RecordRepository("umea");

        try {
            String birth_name = CommonLinkViabilityLogic.getPrimaryNameFromBirthRecord(birth_record);
            String death_name = CommonLinkViabilityLogic.getPrimaryNameFromDeathRecord(death_record);
//            if( birth_name.equals(death_name)) {
//                return false; // they are the same person and therefore not siblings
//            }

            //TODO make this a distance comparison
            if( getDistance(birth_record, death_record, composite_measure_bd) < 5) {
                return false; // they are the same person and therefore not siblings
            }

            final LocalDate date_of_birth_from_birth_record = CommonLinkViabilityLogic.getBirthDateFromBirthRecord(birth_record);
            final LocalDate date_of_birth_from_death_record = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record);

            return siblingBirthDatesAreViable(date_of_birth_from_birth_record, date_of_birth_from_death_record);

        } catch (NumberFormatException e) { // Invalid year.
            return true;
        } catch (BucketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {

        int count = 0;
        for (LXP query_record : getQueryRecords()) {
            count += countBirthDeathSiblingGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BIRTH_DEATH_GT_SIBLING_LINKS_QUERY = "MATCH (a:Birth)-[r:GT_SIBLING, { actors: \"Child-Deceased\" } ]-(b:Death) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBirthDeathSiblingGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {

        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BIRTH_DEATH_GT_SIBLING_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

    public static void setMaxThreshold(double maxThreshold) {
        MAX_THRESHOLD = maxThreshold;
    }

    @Override
    public double getThreshold() {
        if(MAX_THRESHOLD > 0){
            return MAX_THRESHOLD;
        }

        switch (getNumberOfLinkageFieldsRequired()){
            case 4:
            case 3:
                return 0.83;
            case 2:
                return 0.5;
            default:
                return DISTANCE_THRESHOLD;
        }
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }

    protected static LXPMeasure getCompositeMeasureBirthDeath(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS_BIRTH = list(
                Birth.FORENAME,
                Birth.SURNAME
        );

        final List<Integer> LINKAGE_FIELDS_DEATH = list(
                Death.FORENAME,
                Death.SURNAME
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS_BIRTH, LINKAGE_FIELDS_DEATH);
    }

    private static double getDistance(LXP id1, LXP id2, LXPMeasure composite_measure) throws BucketException {
        return composite_measure.distance(id1, id2);
    }
}
