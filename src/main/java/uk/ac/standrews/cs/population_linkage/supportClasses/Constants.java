package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.ac.standrews.cs.utilities.phonetic.Metaphone;
import uk.ac.standrews.cs.utilities.phonetic.NYSIIS;
import uk.ac.standrews.cs.utilities.phonetic.PhoneticWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final Cosine COSINE = new Cosine();
    public static final DamerauLevenshtein DAMERAU_LEVENSHTEIN = new DamerauLevenshtein(1, 1, 1, 1);
    public static final Jaccard JACCARD = new Jaccard();
    public static JensenShannon JENSEN_SHANNON = new JensenShannon();
    public static final Levenshtein LEVENSHTEIN = new Levenshtein();
    public static SED SED = new SED();

    public static final BagDistance BAG_DISTANCE = new BagDistance();
    public static final Dice DICE = new Dice();
    public static final Jaro JARO = new Jaro();
    public static final JaroWinkler JARO_WINKLER = new JaroWinkler();
    public static final LongestCommonSubstring LONGEST_COMMON_SUBSTRING = new LongestCommonSubstring();
    public static final NeedlemanWunsch NEEDLEMAN_WUNSCH = new NeedlemanWunsch();
    public static final SmithWaterman SMITH_WATERMAN = new SmithWaterman();

    public static final PhoneticWrapper METAPHONE = new PhoneticWrapper(new Metaphone(), new Levenshtein());
    public static final PhoneticWrapper NYSIIS = new PhoneticWrapper(new NYSIIS(), new Levenshtein());

    public static final List<StringMetric> TRUE_METRICS = Arrays.asList(
            COSINE, DAMERAU_LEVENSHTEIN, JACCARD, JENSEN_SHANNON, LEVENSHTEIN, SED);

    public static final List<StringMetric> PSEUDO_METRICS = Arrays.asList(
            BAG_DISTANCE, DICE, JARO, JARO_WINKLER, LONGEST_COMMON_SUBSTRING, NEEDLEMAN_WUNSCH, SMITH_WATERMAN);

    public static final List<StringMetric> PHONETIC_COMPARATORS = Arrays.asList(
            METAPHONE, NYSIIS);

    public static final List<StringMetric> BASE_METRICS = concatenate(TRUE_METRICS, PSEUDO_METRICS, PHONETIC_COMPARATORS);

    public static String stringRepresentationOf(List<Integer> fields, Class record, List<String> labels) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getSimpleName()).append("[ ");

        for(Integer field : fields)
            sb.append(labels.get(field)).append(" ");

        sb.append("]");
        return sb.toString();
    }

    @SafeVarargs
    private static List<StringMetric> concatenate(final List<StringMetric>... lists) {

        final List<StringMetric> result = new ArrayList<>();

        for (List<StringMetric> list : lists) {
            result.addAll(list);
        }

        return result;
    }

    public static StringMetric get(String stringMetric) {
        return get(stringMetric, 2048);
    }

    public static StringMetric get(String stringMetric, int CHAR_VAL_UPPER_BOUND) {

        switch (stringMetric.toUpperCase()) {
            case "COSINE":
                return COSINE;
            case "DAMERAU_LEVENSHTEIN":
                return DAMERAU_LEVENSHTEIN;
            case "JACCARD":
                return JACCARD;
            case "JENSEN_SHANNON":
                JENSEN_SHANNON = new JensenShannon(CHAR_VAL_UPPER_BOUND);
                return JENSEN_SHANNON;
            case "LEVENSHTEIN":
                return LEVENSHTEIN;
            case "SED":
                return SED = new SED(CHAR_VAL_UPPER_BOUND);
            case "BAG_DISTANCE":
                return BAG_DISTANCE;
            case "DICE":
                return DICE;
            case "JARO":
                return JARO;
            case "JARO_WINKLER":
                return JARO_WINKLER;
            case "LONGEST_COMMON_SUBSTRING":
                return LONGEST_COMMON_SUBSTRING;
            case "NEEDLEMAN_WUNSCH":
                return NEEDLEMAN_WUNSCH;
            case "SMITH_WATERMAN":
                return SMITH_WATERMAN;
            case "METAPHONE":
                return METAPHONE;
            case "NYSIIS":
                return NYSIIS;
            default:
                throw new UnsupportedOperationException("Specified Metric Not Supported");
        }

    }
}
