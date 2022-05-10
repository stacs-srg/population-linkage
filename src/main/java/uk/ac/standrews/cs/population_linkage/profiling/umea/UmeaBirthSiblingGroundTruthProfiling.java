/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.util.ArrayList;
import java.util.List;

public class UmeaBirthSiblingGroundTruthProfiling {

    public static void main(String[] args) {

        final Iterable<LXP> records = Utilities.getBirthRecords(new RecordRepository(Umea.REPOSITORY_NAME));

        List<LXP> record_list = new ArrayList<>(230000);
        for (LXP record : records) {
            record_list.add(record);
        }

        final int size = record_list.size();

        List<Integer> linkage_fields = getSymmetricLinkageFields(BirthSiblingLinkageRecipe.TRUE_MATCH_ALTERNATIVES);

        for (int linkage_field : linkage_fields) {

            int field_present_count = 0;
            for (LXP record : record_list) {
                if (!record.getString(linkage_field).isEmpty()) field_present_count++;
            }
            printPercentage(field_present_count, size, "value present for field " + Birth.getLabels().get(linkage_field));
        }

        long match_count = 0;
        long non_match_count = 0;
        long unknown_count = 0;
        long total = 0;

        long[] known_link_counts = new long[BirthSiblingLinkageRecipe.TRUE_MATCH_ALTERNATIVES.size()];
        double[] known_link_percentages = new double[BirthSiblingLinkageRecipe.TRUE_MATCH_ALTERNATIVES.size()];

        System.out.println("sampling...");

        for (int i = 0; i < size; i++) {

            final LXP record1 = record_list.get(i);
            for (int j = i + 1; j < size; j++) {

                total++;

                final LXP record2 = record_list.get(j);

                final LinkStatus linkStatus = BirthSiblingLinkageRecipe.trueMatch(record1, record2);

                switch (linkStatus) {
                    case TRUE_MATCH:
                        match_count++;
                        break;
                    case NOT_TRUE_MATCH:
                        non_match_count++;
                        break;
                    default:
                        unknown_count++;
                }

                int alternative_index = 0;
                for (List<LinkageRecipe.Pair> alternative : BirthSiblingLinkageRecipe.TRUE_MATCH_ALTERNATIVES) {
                    if (!LinkageRecipe.allIdentityFieldsEmpty(record1, record2, alternative))
                        known_link_counts[alternative_index++]++;
                }
            }

            if (i % 100 == 0) {
                boolean percentages_changed_significantly = false;
                for (int k = 0; k < known_link_counts.length; k++) {
                    double percentage_known = 100.0 * known_link_counts[k] / total;
                    System.out.printf("values present for linkage alternative %d: %.1f%%\n", k + 1, percentage_known);

                    double amount_of_change = Math.abs(percentage_known - known_link_percentages[k]);
                    if (amount_of_change > 0.05) percentages_changed_significantly = true;
                    known_link_percentages[k] = percentage_known;
                }
                double percentage_known = 100.0 * (match_count + non_match_count) / total;
                System.out.printf("values present for linkage overall: %.1f%%\n\n", percentage_known);

                if (!percentages_changed_significantly) return;
            }
        }

        System.out.println("matches: " + match_count);
        System.out.println("non-matches: " + non_match_count);
        System.out.println("unknown: " + unknown_count);
        System.out.println("total: " + total);
    }

    private static List<Integer> getSymmetricLinkageFields(final List<List<LinkageRecipe.Pair>> alternatives) {

        final List<Integer> result = new ArrayList<>();

        for (List<LinkageRecipe.Pair> alternative : alternatives) {
            for (LinkageRecipe.Pair pair : alternative) {
                result.add(pair.first);
            }
        }

        return result;
    }

    public static void printPercentage(final long count, final long total, final String label) {

        System.out.printf(label + ": %.1f%%\n", 100.0 * count / total);
    }
}
