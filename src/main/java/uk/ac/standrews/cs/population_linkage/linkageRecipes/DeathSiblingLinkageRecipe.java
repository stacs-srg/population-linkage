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

public class DeathSiblingLinkageRecipe extends LinkageRecipe {

    public static final List<Integer> COMPARISON_FIELDS = list(
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME
    );

    public static final int ID_FIELD_INDEX = Death.STANDARDISED_ID;

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

    public static final List<List<Pair>> EXCLUDED_MATCH_MAPPINGS = list(
            list(pair(Death.DECEASED_IDENTITY, Death.DECEASED_IDENTITY))
    );

    public static final String LINKAGE_TYPE = "death-death-sibling";

    public DeathSiblingLinkageRecipe(Storr storr) {
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
        return Death.class;
    }

    @Override
    public Class<? extends LXP> getSearchType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getSearchRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return getComparisonFields();
    }

    public static List<Integer> getComparisonFields() {
        return COMPARISON_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.deathSiblingLinkIsViable(proposedLink);
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return getLinkageFields();
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }

    @Override
    public List<List<Pair>> getExcludedMatchMappings() {
        return EXCLUDED_MATCH_MAPPINGS;
    }
}
