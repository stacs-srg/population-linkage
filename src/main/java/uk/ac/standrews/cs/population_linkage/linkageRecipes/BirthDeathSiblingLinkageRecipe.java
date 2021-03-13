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

import java.util.Arrays;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "birth-death-sibling";

    public BirthDeathSiblingLinkageRecipe(Storr storr) {
        super(storr);
    }

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.FATHER_IDENTITY, Death.FATHER_IDENTITY), pair(Birth.MOTHER_IDENTITY, Death.MOTHER_IDENTITY))
    );

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public boolean isSiblingLinkage() {
        return true;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
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
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.birthDeathSiblingLinkIsViable(proposedLink);
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }
}
