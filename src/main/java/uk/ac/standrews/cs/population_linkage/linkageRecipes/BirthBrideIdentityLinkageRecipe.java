/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class BirthBrideIdentityLinkageRecipe extends LinkageRecipe {

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

    public static final String LINKAGE_TYPE = "birth-bride-identity";

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Marriage.BRIDE_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Marriage.BRIDE_BIRTH_RECORD_IDENTITY))
    );

    public BirthBrideIdentityLinkageRecipe(Storr storr) {
        super(storr);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public boolean isSiblingLinkage() {
        return false;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Marriage.ROLE_BRIDE;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.spouseBirthIdentityLinkIsViable(proposedLink, true);
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return SEARCH_FIELDS;
    }
}
