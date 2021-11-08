/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.List;
import java.util.Map;

/**
 * Links two people appearing as the spouses on a marriage record with the same people appearing as the parents of the bride on another marriage record,
 * i.e. links the marriage of two people to a marriage of a daughter.
 */
public class BrideMarriageParentsMarriageIdentityLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.55;

    public static final String LINKAGE_TYPE = "bride-parents-marriage-identity";

    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    // TODO should occupation be used, given long elapsed time?

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME,
            Marriage.GROOM_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.BRIDE_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY)),
            list(pair(Marriage.GROOM_IDENTITY, Marriage.BRIDE_FATHER_IDENTITY))
    );

    public BrideMarriageParentsMarriageIdentityLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP marriage1, LXP marriage2) {
        return trueMatch(marriage1, marriage2);
    }

    public static LinkStatus trueMatch(LXP marriage1, LXP marriage2) {
        return trueMatch(marriage1, marriage2, TRUE_MATCH_ALTERNATIVES);
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
        return Marriage.ROLE_SPOUSES;
    }

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_BRIDES_PARENTS;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    /**
     * Checks whether a plausible period has elapsed between the marriage and the marriage of the daughter.
     *
     * @param proposedLink the proposed link
     * @return true if the link is viable
     */
    public static boolean isViable(final RecordPair proposedLink) {

        try {
            final LXP parents_marriage = proposedLink.record1;
            final LXP bride_marriage = proposedLink.record2;

            final int parents_year_of_marriage = CommonLinkViabilityLogic.getMarriageDateFromMarriageRecord(parents_marriage).getYear();
            final int bride_year_of_marriage = CommonLinkViabilityLogic.getMarriageDateFromMarriageRecord(bride_marriage).getYear();

            final int years_between_marriages = bride_year_of_marriage - parents_year_of_marriage;

            return years_between_marriages >= LinkageConfig.MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE &&
                    years_between_marriages <= LinkageConfig.MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksAsymmetric();
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }
}
