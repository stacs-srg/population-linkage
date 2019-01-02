package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.*;

public class Utilities {

    private static final int CHARVAL = 512;
    private static final long SEED = 34553543456223L;

    public static final Levenshtein LEVENSHTEIN = new Levenshtein();
    public static final Jaccard JACCARD = new Jaccard();
    public static final Cosine COSINE = new Cosine();
    public static final SED SED = new SED(CHARVAL);
    public static final JensenShannon JENSEN_SHANNON = new JensenShannon();
    public static final JensenShannon2 JENSEN_SHANNON2 = new JensenShannon2(CHARVAL);

    public static final List<NamedMetric<String>> BASE_METRICS = Arrays.asList(LEVENSHTEIN, JACCARD, COSINE, SED, JENSEN_SHANNON, JENSEN_SHANNON2);

    public static final List<Integer> SIBLING_BUNDLING_BIRTH_MATCH_FIELDS = Arrays.asList(
            Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    public static Iterable<LXP> getBirthRecords(RecordRepository record_repository) {

        return () -> new Iterator<LXP>(){

            Iterator<Birth> birth_records = record_repository.getBirths().iterator();

            @Override
            public boolean hasNext() {
                return birth_records.hasNext();
            }

            @Override
            public LXP next() {
                return birth_records.next();
            }
        };
    }

    public static Iterable<LXP> getDeathRecords(RecordRepository record_repository) {

        return () -> new Iterator<LXP>(){

            Iterator<Death> death_records = record_repository.getDeaths().iterator();

            @Override
            public boolean hasNext() {
                return death_records.hasNext();
            }

            @Override
            public LXP next() {
                return death_records.next();
            }
        };
    }

    public static Iterable<LXP> getMarriageRecords(RecordRepository record_repository) {

        return () -> new Iterator<LXP>(){

            Iterator<Marriage> marriage_records = record_repository.getMarriages().iterator();

            @Override
            public boolean hasNext() {
                return marriage_records.hasNext();
            }

            @Override
            public LXP next() {
                return marriage_records.next();
            }
        };
    }

    public static void printSampleRecords(DataSet data_set, String record_type, int number_to_print) {
        uk.ac.standrews.cs.population_records.record_types.Utilities.printSampleRecords(data_set, record_type, number_to_print);
    }

    public static <T> List<T> randomise(final Iterable<T> records) {

        Random random = new Random(SEED);

        List<T> record_list = new ArrayList<>();
        for (T record : records) {
            record_list.add(record);
        }

        int number_of_records = record_list.size();

        for (int i = 0; i < number_of_records; i++) {
            int swap_index = random.nextInt(number_of_records);
            T temp = record_list.get(i);
            record_list.set(i, record_list.get(swap_index));
            record_list.set(swap_index, temp);
        }
        return record_list;
    }
}
