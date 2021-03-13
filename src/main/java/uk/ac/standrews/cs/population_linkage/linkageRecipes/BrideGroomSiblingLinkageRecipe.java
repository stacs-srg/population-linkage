/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.SpouseRole;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class BrideGroomSiblingLinkageRecipe extends LinkageRecipe {

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.GROOM_FATHER_FORENAME,
            Marriage.GROOM_FATHER_SURNAME,
            Marriage.GROOM_MOTHER_FORENAME,
            Marriage.GROOM_MOTHER_MAIDEN_SURNAME
    );

    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    /**
     * Various possible relevant sources of ground truth for siblings:
     * * identities of parents
     * * identities of parents' birth records
     */
    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.BRIDE_MOTHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY), pair(Marriage.BRIDE_FATHER_IDENTITY, Marriage.GROOM_FATHER_IDENTITY)),
            list(pair(Marriage.BRIDE_MOTHER_BIRTH_RECORD_IDENTITY, Marriage.GROOM_MOTHER_BIRTH_RECORD_IDENTITY), pair(Marriage.BRIDE_FATHER_BIRTH_RECORD_IDENTITY, Marriage.GROOM_FATHER_BIRTH_RECORD_IDENTITY))
    );

    public static final String LINKAGE_TYPE = "bride-groom-sibling";

    public BrideGroomSiblingLinkageRecipe(Storr storr) {
        super(storr);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public boolean isSiblingLinkage() {
        return true;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() { return Marriage.ROLE_BRIDE; }

    @Override
    public String getSearchRole() { return Marriage.ROLE_GROOM; }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.spouseSpouseSiblingLinkIsViable(proposedLink, SpouseRole.BRIDE, SpouseRole.GROOM);
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }
}
