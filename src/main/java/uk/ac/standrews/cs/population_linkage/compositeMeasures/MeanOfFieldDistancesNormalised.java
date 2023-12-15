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
 * Defines a distance function between LXP records as the mean of the distances between values of specified fields,
 * normalised to range 0-1 if the field measure isn't already normalised.
 *
 * If a normalisation cutoff value is supplied, then where the field measure isn't intrinsically normalised, the
 * record distance is normalised linearly relative to that value. Otherwise, a non-linear mapping of the entire
 * double range to 0-1 is used.
 */
public class MeanOfFieldDistancesNormalised {//extends LXPMeasure {

//    private double normalisation_cutoff = 0d;
//
//    public MeanOfFieldDistancesNormalised(final StringMeasure base_measure, final List<Integer> field_list) {
//
//        this(base_measure, field_list, field_list);
//    }
//
//    public MeanOfFieldDistancesNormalised(final StringMeasure base_measure, final List<Integer> field_list, final double normalisation_cutoff) {
//
//        this(base_measure, field_list, field_list, normalisation_cutoff);
//    }
//
//    public MeanOfFieldDistancesNormalised(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2) {
//
//        super(base_measure, field_list1, field_list2);
//    }
//
//    public MeanOfFieldDistancesNormalised(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2, final double normalisation_cutoff) {
//
//        super(base_measure, field_list1, field_list2);
//        this.normalisation_cutoff = normalisation_cutoff;
//    }
//
//    @Override
//    public String getMeasureName() {
//        return "Normalised mean of field distances using: " + base_measure.getMeasureName();
//    }
//
//    @Override
//    public boolean maxDistanceIsOne() {
//        return true;
//    }
//
//    @Override
//    public double calculateDistance(final LXP x, final LXP y) {
//
//        final double mean = sumOfFieldDistances(x, y) / field_indices1.size();
//        return base_measure.maxDistanceIsOne() ? mean : normalise(mean);
//    }
//
//    private double normalise(final double d) {
//
//        return normalisation_cutoff > 0d ? Math.min(d, normalisation_cutoff) / normalisation_cutoff : normaliseArbitraryPositiveDistance(d);
//    }
//
//    public static void main(String[] args) {
//
//        final MeanOfFieldDistancesNormalised birth_birth_measure1 = new MeanOfFieldDistancesNormalised(Constants.LEVENSHTEIN, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
//        final MeanOfFieldDistancesNormalised birth_death_measure1 = new MeanOfFieldDistancesNormalised(Constants.LEVENSHTEIN, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);
//
//        final MeanOfFieldDistancesNormalised birth_birth_measure2 = new MeanOfFieldDistancesNormalised(Constants.SED, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
//        final MeanOfFieldDistancesNormalised birth_death_measure2 = new MeanOfFieldDistancesNormalised(Constants.SED, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);
//
//        printExamples(birth_birth_measure1, birth_death_measure1);
//        printExamples(birth_birth_measure2, birth_death_measure2);
//    }
}
