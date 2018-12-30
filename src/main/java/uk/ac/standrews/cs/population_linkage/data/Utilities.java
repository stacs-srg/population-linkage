package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {

    private static final int CHARVAL = 512;

    public static final Levenshtein LEVENSHTEIN = new Levenshtein();
    public static final Jaccard JACCARD = new Jaccard();
    public static final Cosine COSINE = new Cosine();
    public static final SED SED = new SED(CHARVAL);
    public static final JensenShannon JENSEN_SHANNON = new JensenShannon();
    public static final JensenShannon2 JENSEN_SHANNON2 = new JensenShannon2(CHARVAL);

    public static final NamedMetric<String>[] BASE_METRICS = new NamedMetric[] {LEVENSHTEIN, JACCARD, COSINE, SED, JENSEN_SHANNON, JENSEN_SHANNON2};

    public static final List<Integer> BIRTH_MATCH_FIELDS = Arrays.asList(
            Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    public static List<LXP> getBirthLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {
            sub_records.add(birth);
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static List<LXP> getDeathLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Death death : record_repository.getDeaths()) {
            sub_records.add(death);
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static List<LXP> getMarriageLinkageSubRecords(RecordRepository record_repository) {

        List<LXP> sub_records = new ArrayList<>();

        for (Marriage marriage : record_repository.getMarriages()) {
            sub_records.add(marriage);
        }

        if (sub_records.size() == 0) throw new RuntimeException("No records found in repository");
        return sub_records;
    }

    public static NamedMetric<LXP> weightedAverageLevenshteinOverBirths() {

        return new Sigma(new Levenshtein(), BIRTH_MATCH_FIELDS);
    }

    public static void printSampleRecords(DataSet data_set, String record_type, int number_to_print) {
        uk.ac.standrews.cs.population_records.record_types.Utilities.printSampleRecords(data_set, record_type, number_to_print);
    }
}
