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
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Defines a distance function between LXP records as the maximum of the distances between values of specified fields.
 */
public class MaximumOfFieldDistances extends LXPMeasure {

    public MaximumOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list) {

        this(base_measure, field_list, field_list);
    }

    public MaximumOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2) {

        super(base_measure, field_list1, field_list2);
    }

    @Override
    public String getMeasureName() {
        return "Maximum of field distances using: " + base_measure.getMeasureName();
    }

    @Override
    public boolean maxDistanceIsOne() {
        return base_measure.maxDistanceIsOne();
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {
        
        double max = 0.0d;

        for (int i = 0; i < field_indices1.size(); i++) {
            try {
                final int field_index1 = field_indices1.get(i);
                final int field_index2 = field_indices2.get(i);

                final String field_value1 = x.getString(field_index1);
                final String field_value2 = y.getString(field_index2);

                max = Math.max(max, base_measure.distance(field_value1, field_value2));

            } catch (Exception e) {
                throw new RuntimeException("exception comparing fields " + x.getMetaData().getFieldName(field_indices1.get(i)) + " and " + y.getMetaData().getFieldName(field_indices2.get(i)) + " in records \n" + x + "\n and \n" + y, e);
            }
        }

        return max;
    }

    public static void main(String[] args) {

        final MaximumOfFieldDistances birth_birth_measure1 = new MaximumOfFieldDistances(Constants.LEVENSHTEIN, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
        final MaximumOfFieldDistances birth_death_measure1 = new MaximumOfFieldDistances(Constants.LEVENSHTEIN, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);

        final MaximumOfFieldDistances birth_birth_measure2 = new MaximumOfFieldDistances(Constants.SED, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
        final MaximumOfFieldDistances birth_death_measure2 = new MaximumOfFieldDistances(Constants.SED, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);

        printExamples(birth_birth_measure1, birth_death_measure1);
        printExamples(birth_birth_measure2, birth_death_measure2);
    }
}
