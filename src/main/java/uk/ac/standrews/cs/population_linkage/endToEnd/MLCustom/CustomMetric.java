/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.MLCustom;


import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

/**
 * Custom metric based on Smac experiments
 * Created by al on 22/7/21
 * Settings from smac:
 *
 *         Cosine.MOTHER_FORENAME	0.527945859
 *         Cosine.PARENTS_DAY_OF_MARRIAGE	0.455427177
 *         DamerauLevenshtein.PARENTS_PLACE_OF_MARRIAGE	0.056451535
 *         DamerauLevenshtein.PARENTS_YEAR_OF_MARRIAGE	0.998502691
 *         Jaccard.FATHER_FORENAME	0.12044174
 *         Jaccard.MOTHER_FORENAME	0.003696064
 *         Jaccard.PARENTS_MONTH_OF_MARRIAGE	0.451234003
 *         JensenShannon.MOTHER_MAIDEN_SURNAME	0.727961819
 *         SmithWaterman.FATHER_SURNAME	0.442990745
 *         threshold	0.360571156
 */
public class CustomMetric extends Metric<LXP> {

    final int id_field_index;
    
    static final Metric<String> cosine = new Cosine();
    static final Metric<String> damerau_levenshtein = new DamerauLevenshtein(1,1,1,1);
    static final Metric<String> jaccard = new Jaccard();
    static final Metric<String> jensen_shannon = new JensenShannon();
    static final Metric<String> smith_waterman = new SmithWaterman();

    static final double mf_cos_weight= 0.527945859; // MOTHER_FORENAME COSINE
    static final double pdom_weight= 0.455427177; // PARENTS_DAY_OF_MARRIAGE
    static final double ppom_weight = 0.056451535; // PARENTS_PLACE_OF_MARRIAGE
    static final double pyom_weight= 0.998502691; // PARENTS_YEAR_OF_MARRIAGE
    static final double pmom_weight= 0.451234003; // PARENTS_MONTH_OF_MARRIAGE
    static final double ff_weight= 0.12044174; // FATHER_FORENAME
    static final double mf_jacc_weight= 0.003696064; // MOTHER_FORENAME JACCARD
    static final double mms_weight= 0.727961819; // MOTHER_MAIDEN_SURNAME
    static final double fs_weight= 0.442990745; // FATHER_SURNAME

    static final double total_weights = mf_cos_weight + pdom_weight + ppom_weight + pyom_weight + pmom_weight + ff_weight + mf_jacc_weight + mms_weight + fs_weight;

    public CustomMetric(final int id_field_index) {

        this.id_field_index = id_field_index;
    }

    @Override
    public double calculateDistance(final LXP a, final LXP b) {

        double mf_cos_dist = cosine.distance(a.getString(Birth.MOTHER_FORENAME), b.getString(Birth.MOTHER_FORENAME)) * mf_cos_weight;
        double pdom_dist = cosine.distance(a.getString(Birth.PARENTS_DAY_OF_MARRIAGE), b.getString(Birth.PARENTS_DAY_OF_MARRIAGE)) * pdom_weight;
        double ppom_dist = damerau_levenshtein.distance(a.getString(Birth.PARENTS_PLACE_OF_MARRIAGE), b.getString(Birth.PARENTS_PLACE_OF_MARRIAGE)) * ppom_weight;
        double pyom_dist = damerau_levenshtein.distance(a.getString(Birth.PARENTS_YEAR_OF_MARRIAGE), b.getString(Birth.PARENTS_YEAR_OF_MARRIAGE)) * pyom_weight;
        double pmom_dist = jaccard.distance(a.getString(Birth.PARENTS_MONTH_OF_MARRIAGE), b.getString(Birth.PARENTS_MONTH_OF_MARRIAGE)) * pmom_weight;
        double ff_dist = jaccard.distance(a.getString(Birth.FATHER_FORENAME), b.getString(Birth.FATHER_FORENAME)) * ff_weight;
        double mf_dist = jaccard.distance(a.getString(Birth.MOTHER_FORENAME), b.getString(Birth.MOTHER_FORENAME)) * mf_jacc_weight;
        double mms_dist = jensen_shannon.distance(a.getString(Birth.MOTHER_MAIDEN_SURNAME), b.getString(Birth.MOTHER_MAIDEN_SURNAME)) * mms_weight;
        double fs_dist = smith_waterman.distance(a.getString(Birth.FATHER_SURNAME), b.getString(Birth.FATHER_SURNAME)) * fs_weight;

        double total_distance = mf_cos_dist + pdom_dist + ppom_dist + pyom_dist + pmom_dist + ff_dist + mf_dist + mms_dist + fs_dist;

        // Should be a weighted average. - sum and divide by total weight.

        return total_distance / total_weights;
    }

    @Override
    public String getMetricName() {
        return "Custom";
    }

    private void printExceptionDebug(final LXP a, final LXP b, final int field_index) {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Exception in distance calculation");
        System.out.println("field index list: " + field_index);
        System.out.println("a: " + (a == null ? "null" : "not null"));
        System.out.println("b: " + (b == null ? "null" : "not null"));
        System.out.println("id of a: " + a.getString(id_field_index));
        System.out.println("id of b: " + b.getString(id_field_index));
        System.out.println("field name a: " + a.getMetaData().getFieldName(field_index));
        System.out.println("field name b: " + b.getMetaData().getFieldName(field_index));
        System.out.println("field value a: " + a.getString(field_index));
        System.out.println("field value b: " + b.getString(field_index));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}
