/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Links a person appearing as the bride on a marriage record with the same person appearing as the bride on another marriage record,
 * i.e. links a woman's multiple marriages.
 */
public class BrideBrideIdentityLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0.49;

    public static final String LINKAGE_TYPE = "bride-bride-identity";

    public static final int ID_FIELD_INDEX1 = Marriage.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public static final int ALL_LINKAGE_FIELDS = 8; // 8 is all of them

    public static final List<Integer> LINKAGE_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION,
            Marriage.BRIDE_OCCUPATION
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME,
            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_FATHER_OCCUPATION,
            Marriage.BRIDE_OCCUPATION
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Marriage.BRIDE_IDENTITY, Marriage.BRIDE_IDENTITY))
    );

    public BrideBrideIdentityLinkageRecipe(String source_repository_name, String links_persistent_name) {

        super(source_repository_name, links_persistent_name);

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
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_BRIDE;
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
//    public double getThreshold() {
//        return DISTANCE_THRESHOLD;
//    }

//    @Override
//    public LXPMeasure getCompositeMeasure() {
//        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
//    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    /**
     * Checks whether the discrepancy between the recorded or calculated dates of birth on the two records is acceptably low.
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP record1, final LXP record2) {

        try {
            final LocalDate date_of_birth1 = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record1, true);
            final LocalDate date_of_birth2 = CommonLinkViabilityLogic.getBirthDateFromMarriageRecord(record2, true);

            return CommonLinkViabilityLogic.alternativeIdentityBirthDatesAreViable(date_of_birth1, date_of_birth2);

        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksAsymmetric();
    }
}
