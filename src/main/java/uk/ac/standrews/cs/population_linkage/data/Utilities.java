package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.BirthLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.DeathLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.MarriageLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {

    private static final List<Integer> BIRTH_MATCH_FIELDS = Arrays.asList(
            BirthLinkageSubRecord.FATHER_FORENAME, BirthLinkageSubRecord.FATHER_SURNAME,
            BirthLinkageSubRecord.MOTHER_FORENAME, BirthLinkageSubRecord.MOTHER_MAIDEN_SURNAME,
            BirthLinkageSubRecord.PARENTS_PLACE_OF_MARRIAGE,
            BirthLinkageSubRecord.PARENTS_DAY_OF_MARRIAGE,
            BirthLinkageSubRecord.PARENTS_MONTH_OF_MARRIAGE,
            BirthLinkageSubRecord.PARENTS_YEAR_OF_MARRIAGE
    );

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

    public static NamedMetric<LXP> weightedAverageLevenshteinOverBirths() {

        return new WeightedAverageLevenshtein<>(BIRTH_MATCH_FIELDS);
    }

    public static void printSampleRecords(DataSet data_set, String record_type, int number_to_print) {
        uk.ac.standrews.cs.population_records.record_types.Utilities.printSampleRecords(data_set, record_type, number_to_print);
    }
}
