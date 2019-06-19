package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.ac.standrews.cs.utilities.phonetic.Metaphone;
import uk.ac.standrews.cs.utilities.phonetic.NYSIIS;
import uk.ac.standrews.cs.utilities.phonetic.PhoneticWrapper;

import java.util.*;

public class Utilities {

    private static final int CHARVAL = 512;
    private static final long SEED = 34553543456223L;

    public static final Levenshtein LEVENSHTEIN = new Levenshtein();
    public static final Jaccard JACCARD = new Jaccard();
    public static final Cosine COSINE = new Cosine();
    public static final SED SED = new SED();
    public static final JensenShannon JENSEN_SHANNON = new JensenShannon();
    public static final DamerauLevenshtein DAMERAU_LEVENSHTEIN = new DamerauLevenshtein(1, 1, 1, 1);

    private static final Jaro JARO = new Jaro();
    private static final JaroWinkler JARO_WINKLER = new JaroWinkler();
    private static final Dice DICE = new Dice();
    private static final NeedlemanWunsch NEEDLEMAN_WUNSCH = new NeedlemanWunsch();
    private static final SmithWaterman SMITH_WATERMAN = new SmithWaterman();
    private static final LongestCommonSubstring LONGEST_COMMON_SUBSTRING = new LongestCommonSubstring();
    private static final BagDistance BAG_DISTANCE = new BagDistance();
    private static final PhoneticWrapper METAPHONE = new PhoneticWrapper(new Metaphone(), new Levenshtein());
    private static final PhoneticWrapper NYSIIS = new PhoneticWrapper(new NYSIIS(), new Levenshtein());

    public static final List<StringMetric> BASE_METRICS = Arrays.asList(

            // True metrics
            LEVENSHTEIN, JACCARD, COSINE, SED, JENSEN_SHANNON, DAMERAU_LEVENSHTEIN,

            // Pseudo metrics
            JARO, JARO_WINKLER, DICE, NEEDLEMAN_WUNSCH, SMITH_WATERMAN, LONGEST_COMMON_SUBSTRING, BAG_DISTANCE,

            // Phonetic comparisons
            METAPHONE, NYSIIS
    );

    public static final List<Integer> SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS = Arrays.asList(

            Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    public static LinkStatus isTrueMatchBirthSiblingUmea(LXP record1, LXP record2) {

        final String b1_parent_marriage_id = record1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_marriage_id = record2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        final String b1_mother_id = record1.getString(Birth.MOTHER_IDENTITY);
        final String b2_mother_id = record2.getString(Birth.MOTHER_IDENTITY);

        final String b1_father_id = record1.getString(Birth.FATHER_IDENTITY);
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY);

        final String b1_mother_birth_id = record1.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);
        final String b2_mother_birth_id = record2.getString(Birth.MOTHER_BIRTH_RECORD_IDENTITY);

        final String b1_father_birth_id = record1.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);
        final String b2_father_birth_id = record2.getString(Birth.FATHER_BIRTH_RECORD_IDENTITY);

        if (!b1_parent_marriage_id.isEmpty() && b1_parent_marriage_id.equals(b2_parent_marriage_id)) return LinkStatus.TRUE_MATCH;

        if (!b1_mother_id.isEmpty() && b1_mother_id.equals(b2_mother_id) && !b1_father_id.isEmpty() && b1_father_id.equals(b2_father_id)) return LinkStatus.TRUE_MATCH;

        if (!b1_mother_birth_id.isEmpty() && b1_mother_birth_id.equals(b2_mother_birth_id) && !b1_father_birth_id.isEmpty() && b1_father_birth_id.equals(b2_father_birth_id)) return LinkStatus.TRUE_MATCH;

        if (b1_parent_marriage_id.isEmpty() && b2_parent_marriage_id.isEmpty() &&
                b1_mother_id.isEmpty() && b2_mother_id.isEmpty() &&
                b1_father_id.isEmpty() && b2_father_id.isEmpty() &&
                b1_mother_birth_id.isEmpty() && b2_mother_birth_id.isEmpty() &&
                b1_father_birth_id.isEmpty() && b2_father_birth_id.isEmpty()) return LinkStatus.UNKNOWN;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    public static LinkStatus isTrueMatchBirthSiblingSkye(LXP record1, LXP record2) {

        final String b1_family_id = record1.getString(Birth.FAMILY);
        final String b2_family_id = record2.getString(Birth.FAMILY);

        if (b1_family_id.isEmpty() || b2_family_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_family_id.equals(b2_family_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    public static Iterable<LXP> getBirthRecords(RecordRepository record_repository) {

        return () -> new Iterator<LXP>() {

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

        return () -> new Iterator<LXP>() {

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

        return () -> new Iterator<LXP>() {

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

    public static <T> List<T> permute(final Iterable<T> records) {

        return permute(records, SEED);
    }

    public static <T> List<T> permute(final Iterable<T> records, long seed) {

        Random random = new Random(seed);

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
