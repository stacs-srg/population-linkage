/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Links two people appearing as the spouses on a marriage record with the same people appearing as the parents of the groom on another marriage record,
 * i.e. links the marriage of two people to a marriage of their son.
 */
public class GroomMarriageParentsMarriageIdentityLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.6;

    public static final String LINKAGE_TYPE = "groom-parents-marriage-identity";

    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public static final int ALL_LINKAGE_FIELDS = 5;
    private int NUMBER_OF_MARRIAGES = EVERYTHING;
    private List<LXP> cached_records;
    public int linkage_fields = ALL_LINKAGE_FIELDS;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.GROOM_MOTHER_FORENAME,
            Marriage.GROOM_MOTHER_MAIDEN_SURNAME,
            Marriage.GROOM_FATHER_FORENAME,
            Marriage.GROOM_FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS= list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_IDENTITY )),
            list(pair(Marriage.GROOM_MOTHER_IDENTITY, Marriage.BRIDE_IDENTITY ))
    );

    public GroomMarriageParentsMarriageIdentityLinkageRecipe(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name, bridge);
        if( number_of_records.equals(EVERYTHING_STRING) ) {
            NUMBER_OF_MARRIAGES = EVERYTHING;
        } else {
            NUMBER_OF_MARRIAGES = Integer.parseInt(number_of_records);
        }
        setNumberLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    protected Iterable<LXP> getMarriageRecords() {
        if (cached_records == null) {
            cached_records = RecordFiltering.filter(getNoLinkageFieldsRequired(), NUMBER_OF_MARRIAGES, super.getMarriageRecords(), getLinkageFields());
        }
        return cached_records;
    }


    @Override
    public LinkStatus isTrueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage);
    }

    public static LinkStatus trueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage, TRUE_MATCH_ALTERNATIVES);
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
        return Marriage.ROLE_GROOMS_PARENTS;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_SPOUSES;  // bride and groom
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) { return isViable(proposedLink); }

    /**
     * Checks whether a plausible period has elapsed between the marriage and the marriage of the daughter.
     *
     * @param proposedLink the proposed link
     * @return true if the link is viable
     */
    public static boolean isViable(final RecordPair proposedLink) {

        return CommonLinkViabilityLogic.spouseMarriageParentsMarriageIdentityLinkIsViable(proposedLink);
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksAsymmetric();
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }
}
