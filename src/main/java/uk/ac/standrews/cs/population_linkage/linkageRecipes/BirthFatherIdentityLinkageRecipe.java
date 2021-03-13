/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class BirthFatherIdentityLinkageRecipe extends LinkageRecipe {

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Birth.STANDARDISED_ID;

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Birth.FATHER_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Birth.FATHER_BIRTH_RECORD_IDENTITY))
    );

    public static final String LINKAGE_TYPE = "birth-father-identity";

    public BirthFatherIdentityLinkageRecipe(Storr storr) {
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
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Birth.ROLE_FATHER;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    public static boolean isViable(RecordPair proposedLink) {

        // Proposed link is between a person being born on the first record, and the same person
        // appearing as father on the second record. Check that a plausible period has elapsed for
        // the person to be the father.

        return ViableLink.birthParentIdentityLinkIsViable(proposedLink);
    }

    @Override
    public List<Integer> getSearchMappingFields() { return SEARCH_FIELDS; }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {
        return filterBySex(super.getPreFilteredStoredRecords(), Birth.SEX, "m");
    }
}
