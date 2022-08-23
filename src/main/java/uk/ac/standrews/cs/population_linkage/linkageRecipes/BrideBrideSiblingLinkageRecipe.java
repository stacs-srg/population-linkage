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
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Links a person appearing as the bride on a marriage record with a sibling appearing as the bride on another marriage record.
 */
public class BrideBrideSiblingLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.15;

    public static final String LINKAGE_TYPE = "bride-bride-sibling";

    private final int number_of_marriages;

    public static final int ALL_LINKAGE_FIELDS = 4;

    private List<LXP> cached_records = null;

    public static List<Integer> LINKAGE_FIELDS = list(
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME
    );

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.BRIDE_MOTHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY),
                    pair(Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_FATHER_IDENTITY)),
            list(pair(Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY, Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY),
                    pair(Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY, Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY))
    );

    public BrideBrideSiblingLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name);
        if (number_of_records.equals(EVERYTHING_STRING)) {
            number_of_marriages = EVERYTHING;
        } else {
            number_of_marriages = Integer.parseInt(number_of_records);
        }
        setNumberOfLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    @Override
    protected Iterable<LXP> getMarriageRecords() {
        if (cached_records == null) {
            cached_records = RecordFiltering.filter(ALL_LINKAGE_FIELDS, number_of_marriages, super.getMarriageRecords(), getLinkageFields());
        }
        return cached_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String m1_bride_id = record1.getString(Marriage.BRIDE_IDENTITY);
        final String m2_bride_id = record2.getString(Marriage.BRIDE_IDENTITY);

        // Exclude matches for multiple marriages of the same bride.
        if (m1_bride_id.equals(m2_bride_id)) return LinkStatus.NOT_TRUE_MATCH;

        return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_BRIDE;
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
     * Checks whether the recorded or calculated dates of birth are acceptably close for siblings.
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP record1, final LXP record2) {

        if (record1.getString(Marriage.STANDARDISED_ID).equals(record2.getString(Marriage.STANDARDISED_ID))) { // avoid self links.
            return false;
        }

        try {
            final LocalDate date_of_birth1 = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record1, true);
            final LocalDate date_of_birth2 = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record2, true);

            return CommonLinkViabilityLogic.siblingBirthDatesAreViable(date_of_birth1, date_of_birth2);

        } catch (NumberFormatException e) {
            return true;
        }
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
            count += countBrideBrideSiblingGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BRIDE_BRIDE_GT_SIBLING_LINKS_QUERY = "MATCH (a:Marriage)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]-(b:Marriage) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBrideBrideSiblingGTLinks(NeoDbCypherBridge bridge, LXP marriage_record) {
        String standard_id_from = marriage_record.getString(Marriage.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BRIDE_BRIDE_GT_SIBLING_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }
}
