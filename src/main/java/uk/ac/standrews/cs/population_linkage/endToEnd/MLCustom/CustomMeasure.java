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
package uk.ac.standrews.cs.population_linkage.endToEnd.MLCustom;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Custom measure based on Smac experiments
 * Settings from smac:
 * <p>
 * Cosine.MOTHER_FORENAME	0.527945859
 * Cosine.PARENTS_DAY_OF_MARRIAGE	0.455427177
 * DamerauLevenshtein.PARENTS_PLACE_OF_MARRIAGE	0.056451535
 * DamerauLevenshtein.PARENTS_YEAR_OF_MARRIAGE	0.998502691
 * Jaccard.FATHER_FORENAME	0.12044174
 * Jaccard.MOTHER_FORENAME	0.003696064
 * Jaccard.PARENTS_MONTH_OF_MARRIAGE	0.451234003
 * JensenShannon.MOTHER_MAIDEN_SURNAME	0.727961819
 * SmithWaterman.FATHER_SURNAME	0.442990745
 * threshold	0.360571156
 */
public class CustomMeasure extends LXPMeasure {

    static final double mf_cos_weight = 0.527945859; // MOTHER_FORENAME COSINE
    static final double pdom_weight = 0.455427177; // PARENTS_DAY_OF_MARRIAGE
    static final double ppom_weight = 0.056451535; // PARENTS_PLACE_OF_MARRIAGE
    static final double pyom_weight = 0.998502691; // PARENTS_YEAR_OF_MARRIAGE
    static final double pmom_weight = 0.451234003; // PARENTS_MONTH_OF_MARRIAGE
    static final double ff_weight = 0.12044174; // FATHER_FORENAME
    static final double mf_jacc_weight = 0.003696064; // MOTHER_FORENAME JACCARD
    static final double mms_weight = 0.727961819; // MOTHER_MAIDEN_SURNAME
    static final double fs_weight = 0.442990745; // FATHER_SURNAME

    static final double total_weights = mf_cos_weight + pdom_weight + ppom_weight + pyom_weight + pmom_weight + ff_weight + mf_jacc_weight + mms_weight + fs_weight;

    public CustomMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure) {

        super(field_indices1, field_indices2, base_measure);
    }

    @Override
    public double calculateDistance(final LXP a, final LXP b) {

        double mf_cos_dist = Constants.COSINE.distance(a.getString(Birth.MOTHER_FORENAME), b.getString(Birth.MOTHER_FORENAME)) * mf_cos_weight;
        double pdom_dist = Constants.COSINE.distance(a.getString(Birth.PARENTS_DAY_OF_MARRIAGE), b.getString(Birth.PARENTS_DAY_OF_MARRIAGE)) * pdom_weight;
        double ppom_dist = normaliseArbitraryPositiveDistance(Constants.DAMERAU_LEVENSHTEIN.distance(a.getString(Birth.PARENTS_PLACE_OF_MARRIAGE), b.getString(Birth.PARENTS_PLACE_OF_MARRIAGE)) * ppom_weight);
        double pyom_dist = normaliseArbitraryPositiveDistance(Constants.DAMERAU_LEVENSHTEIN.distance(a.getString(Birth.PARENTS_YEAR_OF_MARRIAGE), b.getString(Birth.PARENTS_YEAR_OF_MARRIAGE)) * pyom_weight);
        double pmom_dist = Constants.JACCARD.distance(a.getString(Birth.PARENTS_MONTH_OF_MARRIAGE), b.getString(Birth.PARENTS_MONTH_OF_MARRIAGE)) * pmom_weight;
        double ff_dist = Constants.JACCARD.distance(a.getString(Birth.FATHER_FORENAME), b.getString(Birth.FATHER_FORENAME)) * ff_weight;
        double mf_dist = Constants.JACCARD.distance(a.getString(Birth.MOTHER_FORENAME), b.getString(Birth.MOTHER_FORENAME)) * mf_jacc_weight;
        double mms_dist = Constants.JENSEN_SHANNON.distance(a.getString(Birth.MOTHER_MAIDEN_SURNAME), b.getString(Birth.MOTHER_MAIDEN_SURNAME)) * mms_weight;
        double fs_dist = Constants.SMITH_WATERMAN.distance(a.getString(Birth.FATHER_SURNAME), b.getString(Birth.FATHER_SURNAME)) * fs_weight;

        double total_distance = mf_cos_dist + pdom_dist + ppom_dist + pyom_dist + pmom_dist + ff_dist + mf_dist + mms_dist + fs_dist;

        // Should be a weighted average. - sum and divide by total weight.

        return total_distance / total_weights;
    }

    @Override
    public String toString() {
        return "Custom";
    }

    @Override
    public double getMaxDistance() {
        return 1;
    }
}
