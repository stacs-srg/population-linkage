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
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic.siblingBirthDatesAreViable;

/**
 * Links a person appearing as the deceased on a death record with a sibling appearing as the deceased on another death record.
 */
public class DeathSiblingLinkageRecipe extends LinkageRecipe {

    protected static final double DISTANCE_THRESHOLD = 0.53;

    public static final String LINKAGE_TYPE = "death-death-sibling";

    public static final int ALL_LINKAGE_FIELDS = 4;
    private final int number_of_deaths;

    public static final int ID_FIELD_INDEX1 = Death.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' marriage record
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Death.MOTHER_IDENTITY, Death.MOTHER_IDENTITY), pair(Death.FATHER_IDENTITY, Death.FATHER_IDENTITY)),
            list(pair(Death.PARENT_MARRIAGE_RECORD_IDENTITY, Death.PARENT_MARRIAGE_RECORD_IDENTITY)),
            list(pair(Death.MOTHER_BIRTH_RECORD_IDENTITY, Death.MOTHER_BIRTH_RECORD_IDENTITY), pair(Death.FATHER_BIRTH_RECORD_IDENTITY, Death.FATHER_BIRTH_RECORD_IDENTITY))
    );

    public DeathSiblingLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
        if (number_of_records.equals(EVERYTHING_STRING)) {
            number_of_deaths = EVERYTHING;
        } else {
            number_of_deaths = Integer.parseInt(number_of_records);
        }
        setNumberLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    @Override
    protected Iterable<LXP> getDeathRecords() {
        return filter(getNumberOfLinkageFieldsRequired(), number_of_deaths, super.getDeathRecords(), getLinkageFields());
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
        return Death.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getQueryRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    public static List<Integer> getComparisonFields() {
        return LINKAGE_FIELDS;
    }

    public static boolean isViable(final LXP death_record1, final LXP death_record2) {

        try {
            final LocalDate date_of_birth_from_death_record1 = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record1);
            final LocalDate date_of_birth_from_death_record2 = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record2);

            return siblingBirthDatesAreViable(date_of_birth_from_death_record1, date_of_birth_from_death_record2);

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return getLinkageFields();
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksSymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        int count = 0;
        for (LXP query_record : getQueryRecords()) {
            count += countDeathSiblingGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String DEATH_GT_SIBLING_LINKS_QUERY = "MATCH (a:Death)-[r:GT_SIBLING, { actors: \"Deceased-Deceased\" } ]-(b:Death) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countDeathSiblingGTLinks(NeoDbCypherBridge bridge, LXP death_record) {
        String standard_id_from = death_record.getString(Death.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(DEATH_GT_SIBLING_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

    @Override
    public double getThreshold() {
//        if(getNumberOfLinkageFieldsRequired() == 4 || getNumberOfLinkageFieldsRequired() == 3){
//            return 1;
//        } else if (getNumberOfLinkageFieldsRequired() == 2) {
//            return 0.35;
//        }else{
//            return DISTANCE_THRESHOLD;
//        }

        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }
}
