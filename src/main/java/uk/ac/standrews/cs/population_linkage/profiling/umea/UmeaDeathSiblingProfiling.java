package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.metrics.Dice;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UmeaDeathSiblingProfiling {

    public static final Dice BASE_METRIC = Constants.DICE;
    private static final double DISTANCE_THRESHOLD = 0.48;
    private static final int OUTER_LOOP_SIZE = 25000;

    public static void main(String[] args) {

        profileData();
    }

    public static void profileData() {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        final Iterable<LXP> records = Utilities.getDeathRecords(new RecordRepository(store_path, repo_name));

        List<LXP> record_list = new ArrayList<>(105000);
        for (LXP record : records) {
            record_list.add(record);
        }

        long tp_with_viability = 0;
        long tp_without_viability = 0;
        long fp_with_viability = 0;
        long fp_without_viability = 0;
        long tn_with_viability = 0;
        long tn_without_viability = 0;
        long fn_with_viability = 0;
        long fn_without_viability = 0;

        long unknown_count = 0;
        long total = 0;

        final long size = record_list.size();
        System.out.println("size: " + size);

        Metric<LXP> metric = new Sigma(BASE_METRIC, DeathDeathSiblingLinkageRecipe.getComparisonFields());

        for (int i = 0; i < OUTER_LOOP_SIZE; i++) {

             for (int j = i + 1; j < size; j++) {

                 total++;

                 final LXP record1 = record_list.get(i);
                 final LXP record2 = record_list.get(j);

                 final double distance = metric.distance(record1, record2);

                 final LinkStatus match_status = DeathDeathSiblingLinkageRecipe.trueMatch(record1, record2);
                 final boolean close_enough = distance <= DISTANCE_THRESHOLD;
                 final boolean viable = DeathDeathSiblingLinkageRecipe.isViable(new RecordPair(record1, record2, distance));

                 switch (match_status) {

                     case TRUE_MATCH: {
                         if (close_enough) {
                             tp_without_viability++;
                         } else {
                             fn_without_viability++;
                         }
                         if (close_enough && viable) {
                             tp_with_viability++;
                         } else {
                             fn_with_viability++;
                         }
                     } break;

                     case NOT_TRUE_MATCH: {
                         if (close_enough) {
                             fp_without_viability++;
                         } else {
                             tn_without_viability++;
                         }
                         if (close_enough && viable) {
                             fp_with_viability++;

//                             System.out.println("\nFP: distance: " + distance);
//                             System.out.println("record 1: " + record1);
//                             System.out.println("record 2: " + record2);


                         } else {
                             tn_with_viability++;
                         }
                     } break;

                     default: unknown_count++;
                 }
            }

            if (i%100 == 0) {

//                System.out.println("without viability check");
//                printCounts(tp_without_viability, fp_without_viability, tn_without_viability, fn_without_viability, unknown_count, total);

                System.out.println("with viability check");
                printCounts(tp_with_viability, fp_with_viability, tn_with_viability, fn_with_viability, unknown_count, total);
            }
        }

        System.out.printf("total: %,d\n", total);
    }

    public static void printCounts(final long tp, final long fp, final long tn, final long fn, final long unknown_count, long total2) {

        long total1 = tp + fp + tn + fn + unknown_count;
        double precision = ClassificationMetrics.precision(tp, fp);
        double recall = ClassificationMetrics.recall(tp, fn);

        printPercentage(tp, total1, "TPs");
        printPercentage(fp, total1, "FPs");
        printPercentage(tn, total1, "TNs");
        printPercentage(fn, total1, "FNs");

        System.out.printf("unknown: %,d (%.2f%%)\n\n", unknown_count, 100.0 * unknown_count / (double) total1);
        System.out.printf("precision: %.2f%%\n", precision);
        System.out.printf("recall: %.2f%%\n\n", recall);
        System.out.printf("total: %,d\n", total1);
//        System.out.printf("total2: %,d\n", total2);
    }

    public static void printPercentage(final long count, final long total, final String label) {

        System.out.printf(label + ": %,d (%.2f%%)\n", count, 100.0 * count / total);
    }
}
