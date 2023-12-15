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
 * Defines a distance function between LXP records as the mean of the distances between values of specified fields.
 */
public class MeanOfFieldDistances {//extends LXPMeasure {

//    public MeanOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list) {
//
//        this(base_measure, field_list, field_list);
//    }
//
//    public MeanOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2) {
//
//        super(base_measure, field_list1, field_list2);
//    }
//
//    @Override
//    public String getMeasureName() {
//        return "Mean of field distances using: " + base_measure.getMeasureName();
//    }
//
//    @Override
//    public boolean maxDistanceIsOne() {
//        return base_measure.maxDistanceIsOne();
//    }
//
//    @Override
//    public double calculateDistance(final LXP x, final LXP y) {
//
//        return sumOfFieldDistances(x, y) / field_indices1.size();
//    }
//
//    public static void main(String[] args) {
//
//        final MeanOfFieldDistances birth_birth_measure1 = new MeanOfFieldDistances(Constants.LEVENSHTEIN, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
//        final MeanOfFieldDistances birth_death_measure1 = new MeanOfFieldDistances(Constants.LEVENSHTEIN, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);
//
//        final MeanOfFieldDistances birth_birth_measure2 = new MeanOfFieldDistances(Constants.SED, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
//        final MeanOfFieldDistances birth_death_measure2 = new MeanOfFieldDistances(Constants.SED, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);
//
//        printExamples(birth_birth_measure1, birth_death_measure1);
//        printExamples(birth_birth_measure2, birth_death_measure2);
//    }
}
