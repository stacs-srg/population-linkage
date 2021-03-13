/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.FORENAME,
            Death.SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Death.BIRTH_RECORD_IDENTITY)),
            list(pair(Birth.DEATH_RECORD_IDENTITY, Death.STANDARDISED_ID))
    );

    public static final String LINKAGE_TYPE = "birth-death-identity";

    public BirthDeathIdentityLinkageRecipe(Storr storr) {
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
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.birthDeathIdentityLinkIsViable( proposedLink );
    }

    @Override
    public List<Integer> getSearchMappingFields() { return SEARCH_FIELDS; }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }
}
