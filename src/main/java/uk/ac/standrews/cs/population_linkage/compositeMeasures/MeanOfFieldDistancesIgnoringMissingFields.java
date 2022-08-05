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
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Defines a distance function between LXP records as the mean of the distances between values of specified non-empty fields.
 * Only fields where a value is present in both records are considered.
 * A defined maximum distance is returned when all fields are missing. This is constrained to be one
 * where the base measure is intrinsically normalised, but may be sampled from the dataset for non-normalised measures.
 */
public class MeanOfFieldDistancesIgnoringMissingFields extends LXPMeasure {

    /*
        Maximum and mean field distances sampled from Umea Birth-Birth linkage:

        Cosine: 1.0, 0.87
        Damerau-Levenshtein: 20.0, 6.71
        Jaccard: 1.0, 0.88
        JensenShannon: 1.0, 0.78
        Levenshtein: 20.0, 6.71
        SED: 1.0, 0.74
        BagDistance: 1.0, 0.64
        Dice: 1.0, 0.81
        Jaro: 1.0, 0.51
        JaroWinkler: 1.0, 0.50
        LongestCommonSubstring: 29.5, 11.28
        NeedlemanWunsch: 1.0, 0.42
        SmithWaterman: 1.0, 0.68
        Metaphone-Levenshtein: 4.0, 2.71
        NYSIIS-Levenshtein: 6.0, 3.53
     */

    private final double max_field_distance;

    public MeanOfFieldDistancesIgnoringMissingFields(final StringMeasure base_measure, final List<Integer> field_list, final double max_field_distance) {

        this(base_measure, field_list, field_list, max_field_distance);
    }

    public MeanOfFieldDistancesIgnoringMissingFields(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2, final double max_field_distance) {

        super(base_measure, field_list1, field_list2);
        if (maxDistanceIsOne() && max_field_distance != 1d) throw new RuntimeException("invalid max distance");
        this.max_field_distance = max_field_distance;
    }

    @Override
    public String getMeasureName() {
        return "Mean of non-missing field distances using: " + base_measure.getMeasureName();
    }

    @Override
    public boolean maxDistanceIsOne() {
        return base_measure.maxDistanceIsOne();
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {

        double total_distance = 0.0d;
        int present_count = 0;

        for (int i = 0; i < field_list1.size(); i++) {
            try {
                final int field_index1 = field_list1.get(i);
                final int field_index2 = field_list2.get(i);

                final String field_value1 = x.getString(field_index1);
                final String field_value2 = y.getString(field_index2);

                if (!RecordFiltering.isMissing(field_value1) && !RecordFiltering.isMissing(field_value2)) {

                    present_count++;
                    total_distance += base_measure.distance(field_value1, field_value2);
                }

            } catch (Exception e) {
                throwExceptionWithDebug(x, y, i, e);
            }
        }

        return present_count > 0 ? total_distance / present_count : max_field_distance;
    }

    public static void main(String[] args) {

        final MeanOfFieldDistancesIgnoringMissingFields birth_birth_measure1 = new MeanOfFieldDistancesIgnoringMissingFields(Constants.LEVENSHTEIN, BirthSiblingLinkageRecipe.LINKAGE_FIELDS, 100d);
        final MeanOfFieldDistancesIgnoringMissingFields birth_death_measure1 = new MeanOfFieldDistancesIgnoringMissingFields(Constants.LEVENSHTEIN, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS, 100d);

        final MeanOfFieldDistancesIgnoringMissingFields birth_birth_measure2 = new MeanOfFieldDistancesIgnoringMissingFields(Constants.SED, BirthSiblingLinkageRecipe.LINKAGE_FIELDS, 1d);
        final MeanOfFieldDistancesIgnoringMissingFields birth_death_measure2 = new MeanOfFieldDistancesIgnoringMissingFields(Constants.SED, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS, 1d);

        printExamples(birth_birth_measure1, birth_death_measure1);
        printExamples(birth_birth_measure2, birth_death_measure2);
    }
}
