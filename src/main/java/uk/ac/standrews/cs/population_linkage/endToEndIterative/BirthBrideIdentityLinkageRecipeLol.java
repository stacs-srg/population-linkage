/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

/**
 * Links a person appearing as the child on a birth record with the same person appearing as the bride on a marriage record.
 * Now also performs subsetting 11/11/21
 */
public class BirthBrideIdentityLinkageRecipeLol extends LinkageRecipe {
    public static final int ALL_LINKAGE_FIELDS = 6; // 6 is all of them

    // TODO Some Wrigley rules not obvious where to place in viability checks.
    // e.g. date of birth should not be after death of mother - identity linkage of mother on birth record to
    // deceased on death record, but not a strong linkage.
    //
    // Maybe better to do such checks on linked population at a later stage?

    // TODO Experiment with including father/mother occupation in all relevant linkages.

    private static final double DISTANCE_THRESHOLD = 0.49;

    public static final String LINKAGE_TYPE = "birth-bride-identity";

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    protected int NUMBER_OF_BIRTHS;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Marriage.BRIDE_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Marriage.BRIDE_BIRTH_RECORD_IDENTITY))
    );
    protected List<LXP> cached_records = null;

    public BirthBrideIdentityLinkageRecipeLol(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name, bridge);
        if (number_of_records.equals(EVERYTHING_STRING)) {
            NUMBER_OF_BIRTHS = EVERYTHING;
        } else {
            NUMBER_OF_BIRTHS = Integer.parseInt(number_of_records);
        }
        setNumberOfLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
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
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

//    @Override
//    public Iterable<LXP> getBirthRecords() {
//        if( cached_records == null ) {
//            Iterable<LXP> f = filterBySex(super.getBirthRecords(), Birth.SEX, "f");
//            cached_records = filter(getNoLinkageFieldsRequired(), NUMBER_OF_BIRTHS, f, getLinkageFields());
//        }
//        System.out.println( "Processing " + cached_records.size() + " birth records");
//        return cached_records;
//    }

    @Override
    public Iterable<LXP> getBirthRecords() {
        System.out.println("*** Real getBirthRecords() commented in BirthBrideIdentityLinkageRecipeLol ***");
        if (cached_records == null) {
            Iterable<LXP> f = filterBySex(super.getBirthRecords(), Birth.SEX, "f");
            cached_records = filter(6, NUMBER_OF_BIRTHS, f, getLinkageFields());
        }
        System.out.println("Processing " + cached_records.size() + " birth records");
        return cached_records;
    }

    private Collection<LXP> filterOut(List<LXP> matched, Iterable<LXP> records) {
        Collection<LXP> filteredRecords = new HashSet<>();

        for (LXP record : records) {
            if (!matched.contains(record)) {
                filteredRecords.add(record);
            }
        }
        return filteredRecords;
    }

    @Override
    protected Collection<LXP> getMarriageRecords() {
        List<LXP> records = StreamSupport.stream(super.getMarriageRecords().spliterator(), false).collect(Collectors.toList());
        System.out.println("Processing " + records.size() + " marriage records");
        return records;
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    /**
     * Checks whether the date of marriage is sufficiently long after the date of birth for the bride to have
     * attained the minimum age for marriage, and that any discrepancy between that calculated age at marriage
     * and the age derived from the marriage record (either explicitly recorded or derived from a date of birth
     * recorded there) is acceptably low.
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP record1, final LXP record2) {

        return CommonLinkViabilityLogic.birthMarriageIdentityLinkIsViable(record1, record2, true);
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        int count = 0;
        for (LXP query_record : getQueryRecords()) {
            count += countBirthBrideIdentityGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]-(m:Marriage) WHERE m.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBirthBrideIdentityGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {
        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }
}