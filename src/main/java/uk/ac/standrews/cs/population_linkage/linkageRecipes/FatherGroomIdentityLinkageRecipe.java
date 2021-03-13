/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import java.util.Arrays;
import java.util.List;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.pair;

public class FatherGroomIdentityLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "father-groom-identity";

    public FatherGroomIdentityLinkageRecipe(Storr storr) {
        super(storr);
    }

    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY))
    );

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public boolean isSiblingLinkage() {
        return false;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_FATHER;
    }

    @Override
    public String getSearchRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.PARENTS_PLACE_OF_MARRIAGE,
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return true;
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
                Marriage.GROOM_FORENAME,
                Marriage.GROOM_SURNAME,
                Marriage.BRIDE_FORENAME,
                Marriage.BRIDE_SURNAME,
                Marriage.PLACE_OF_MARRIAGE,
                Marriage.MARRIAGE_DAY,
                Marriage.MARRIAGE_MONTH,
                Marriage.MARRIAGE_YEAR
        );
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return TRUE_MATCH_ALTERNATIVES;
    }
}
