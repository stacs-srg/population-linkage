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
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic.siblingBirthDatesAreViable;

/**
 * Links a person appearing as the child on a birth record with a sibling appearing as the child on another birth record.
 */
public class BirthHalfSiblingLinkageRecipe extends LinkageRecipe {

    private static final double THRESHOLD = 0.67;

    public static final String LINKAGE_TYPE = "birth-birth-half-sibling";

    public static final int ID_FIELD_INDEX = Birth.STANDARDISED_ID;

    public static final int ALL_LINKAGE_FIELDS = 2;
    private int number_of_births = 0;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME
    );

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' marriage record
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.MOTHER_IDENTITY, Birth.MOTHER_IDENTITY)),
            list(pair(Birth.MOTHER_BIRTH_RECORD_IDENTITY, Birth.MOTHER_BIRTH_RECORD_IDENTITY))
    );

    public BirthHalfSiblingLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name) {

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
        Iterable<LXP> filtered = filterBySingleParent(super.getBirthRecords(), Birth.FATHER_SURNAME);
        return RecordFiltering.filter(getNumberOfLinkageFieldsRequired(), number_of_births, filtered, getLinkageFields());
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
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    public static boolean isViable(final LXP birth_record1, final LXP birth_record2) {

        try {
            final LocalDate date_of_birth_from_birth_record1 = CommonLinkViabilityLogic.getBirthDateFromBirthRecord(birth_record1);
            final LocalDate date_of_birth_from_birth_record2 = CommonLinkViabilityLogic.getBirthDateFromBirthRecord(birth_record2);

            return siblingBirthDatesAreViable(date_of_birth_from_birth_record1, date_of_birth_from_birth_record2);

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
            count += countBirthSiblingGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BIRTH_GT_SIBLING_LINKS_QUERY = "MATCH (a:Birth)-[r:GT_HALF_SIBLING, { actors: \"Child-Child\" } ]-(b:Birth) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBirthSiblingGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {
        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BIRTH_GT_SIBLING_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

    @Override
    public double getThreshold() {
        return THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }
}
