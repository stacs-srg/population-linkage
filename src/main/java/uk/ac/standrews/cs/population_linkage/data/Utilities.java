package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.BirthLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.DeathLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.MarriageLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {

    private final static int NUMBER_TO_PRINT = 5;

    public static final List<Integer> MATCH_FIELDS = Arrays.asList(
            BirthLinkageSubRecord.FATHERS_FORENAME, BirthLinkageSubRecord.FATHERS_SURNAME,
            BirthLinkageSubRecord.MOTHERS_FORENAME, BirthLinkageSubRecord.MOTHERS_MAIDEN_SURNAME,
            BirthLinkageSubRecord.PARENTS_PLACE_OF_MARRIAGE,
            BirthLinkageSubRecord.PARENTS_DAY_OF_MARRIAGE, BirthLinkageSubRecord.PARENTS_MONTH_OF_MARRIAGE, BirthLinkageSubRecord.PARENTS_YEAR_OF_MARRIAGE);

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

    public static List<LXP> getBirthLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {
            sub_records.add(new BirthLinkageSubRecord(birth));
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static List<LXP> getDeathLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Death death : record_repository.getDeaths()) {
            sub_records.add(new DeathLinkageSubRecord(death));
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static List<LXP> getMarriageLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Marriage marriage : record_repository.getMarriages()) {
            sub_records.add(new MarriageLinkageSubRecord(marriage));
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static NamedMetric<LXP> weightedAverageLevenshteinOverBirths() throws InvalidWeightsException {

        return new WeightedAverageLevenshtein<>(MATCH_FIELDS);
    }

    public static Links getGroundTruthSiblingLinks(RecordRepository record_repository) {

        Links links = new Links();

        List<Birth> records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {
            records.add(birth);
        }

        int number_of_records = records.size();

        ExactMatchMatcher matcher = new ExactMatchMatcher(Arrays.asList(Birth.FAMILY));

        for (int i = 0; i < number_of_records; i++) {
            for (int j = i + 1; j < number_of_records; j++) {

                Birth record1 = records.get(i);
                Birth record2 = records.get(j);

                if (matcher.match(record1, record2)) {

                    Role role1 = new Role(record1.getString(Birth.STANDARDISED_ID), Birth.ROLE_BABY);
                    Role role2 = new Role(record2.getString(Birth.STANDARDISED_ID), Birth.ROLE_BABY);
                    links.add(new Link(role1, role2,1.0f, "ground truth"));
                }
            }
        }

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
