package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.*;
import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.model.Links;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

    private final static int NUMBER_TO_PRINT = 5;

    public static void printSampleRecords(DataSet data_set, String record_type) {

        Utilities.printRow(data_set.getColumnLabels());
        List<List<String>> records = data_set.getRecords();

        for (int i = 0; i < NUMBER_TO_PRINT; i++) {
            Utilities.printRow(records.get(i));
        }

        System.out.println("Printed " + NUMBER_TO_PRINT + " of " + records.size() + " " + record_type + " records");
    }

    private static void printRow(List<String> row) {

        boolean first = true;
        for (String element : row) {
            if (!first) {
                System.out.print(",");
            }
            first = false;
            System.out.print(element);
        }
        System.out.println();
    }

    public static List<BirthLinkageSubRecord> getBirthLinkageSubRecords(RecordRepository record_repository) {

        List<BirthLinkageSubRecord> sub_records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {

            sub_records.add(new BirthLinkageSubRecord(birth));
        }

        return sub_records;
    }

    public static List<DeathLinkageSubRecord> getDeathLinkageSubRecords(RecordRepository record_repository) {

        List<DeathLinkageSubRecord> sub_records = new ArrayList<>();

        for (Death death : record_repository.getDeaths()) {

            sub_records.add(new DeathLinkageSubRecord(death));
        }

        return sub_records;
    }

    public static List<MarriageLinkageSubRecord> getMarriageLinkageSubRecords(RecordRepository record_repository) {

        List<MarriageLinkageSubRecord> sub_records = new ArrayList<>();

        for (Marriage marriage : record_repository.getMarriages()) {

            sub_records.add(new MarriageLinkageSubRecord(marriage));
        }

        return sub_records;
    }

    public static Links getGroundTruthSiblingLinks(RecordRepository record_repository) {

        Links links = new Links();

        links.add(new Link("B_1861_597_0_118601_45", "B_1861_597_0_118601_46"));
        links.add(new Link("B_1861_597_0_118601_48", "B_1861_597_0_118601_49"));

        return links;
    }

    public static LinkageQuality evaluateLinkage(Links calculated_links, Links ground_truth_links) {

        int true_positives = countTruePositives(calculated_links, ground_truth_links);
        int false_positives = countFalsePositives(calculated_links, ground_truth_links);
        int false_negatives = countFalseNegatives(calculated_links, ground_truth_links);

        double precision = ClassificationMetrics.precision(true_positives, false_positives);
        double recall = ClassificationMetrics.recall(true_positives, false_negatives);
        double f_measure = ClassificationMetrics.F1(true_positives, false_positives, false_negatives);

        return new LinkageQuality(precision, recall, f_measure);
    }

    private static int countTruePositives(Links calculated_links, Links ground_truth_links) {

        int count = 0;

        for (Link calculated_link : calculated_links) {

            if (ground_truth_links.contains(calculated_link)) {
                count++;
            }
        }

        System.out.println("TP: " + count);
        return count;
    }

    private static int countFalsePositives(Links calculated_links, Links ground_truth_links) {

        int count = 0;

        for (Link calculated_link : calculated_links) {

            if (!ground_truth_links.contains(calculated_link)) {
                count++;
            }
        }

        System.out.println("FP: " + count);

        return count;
    }

    private static int countFalseNegatives(Links calculated_links, Links ground_truth_links) {

        int count = 0;

        for (Link ground_truth_link : ground_truth_links) {

            if (!calculated_links.contains(ground_truth_link)) {
                count++;
            }
        }

        System.out.println("FN: " + count);

        return count;
    }
}
