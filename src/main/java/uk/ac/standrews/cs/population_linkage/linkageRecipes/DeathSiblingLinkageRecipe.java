/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
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

    protected static final double DISTANCE_THRESHOLD = 0.53; // 0.53 - This is very high

    public static final String LINKAGE_TYPE = "death-death-sibling";

    public static final int ID_FIELD_INDEX = Death.STANDARDISED_ID;

    private int NUMBER_OF_DEATHS = EVERYTHING;
    public static final int ALL_LINKAGE_FIELDS = 4;
    private List<LXP> cached_records = null;

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

    public DeathSiblingLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name, bridge);
        if (number_of_records.equals(EVERYTHING_STRING)) {
            NUMBER_OF_DEATHS = EVERYTHING;
        } else {
            NUMBER_OF_DEATHS = Integer.parseInt(number_of_records);
        }
        setNumberLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    @Override
    protected Iterable<LXP> getDeathRecords() {
        if (cached_records == null) {
            cached_records = filter(getNumberOfLinkageFieldsRequired(), NUMBER_OF_DEATHS, super.getDeathRecords(), getLinkageFields());
        }
        return cached_records;
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

    public static boolean isViable(RecordPair proposedLink) {

        try {
            final LXP death_record1 = proposedLink.stored_record;
            final LXP death_record2 = proposedLink.query_record;

            final LocalDate date_of_birth_from_death_record1 = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record1);
            final LocalDate date_of_birth_from_death_record2 = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record2);

            return siblingBirthDatesAreViable(date_of_birth_from_death_record1, date_of_birth_from_death_record2);

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
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

    private static final String DEATH_GT_SIBLING_LINKS_QUERY = "MATCH (a:Death)-[r:GROUND_TRUTH_DEATH_SIBLING]-(b:Death) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

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
        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }
}
