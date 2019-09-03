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

    public static final List<Integer> SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS = Arrays.asList(

            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    public static final List<Integer> SIBLING_BUNDLING_BRIDE_MARRIAGE_LINKAGE_FIELDS = Arrays.asList(

            Marriage.BRIDE_FATHER_FORENAME,
            Marriage.BRIDE_FATHER_SURNAME,
            Marriage.BRIDE_MOTHER_FORENAME,
            Marriage.BRIDE_MOTHER_MAIDEN_SURNAME
    );

    public static final List<Integer> SIBLING_BUNDLING_BIRTH_TO_DEATH_LINKAGE_FIELDS = Arrays.asList(

            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME
    );

    public static final List<Integer> SIBLING_BUNDLING_DEATH_TO_BIRTH_LINKAGE_FIELDS = Arrays.asList(

            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME
    );

    public static final String SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS_AS_STRINGS = "\"FATHER_FORENAME, FATHER_SURNAME, MOTHER_FORENAME, MOTHER_MAIDEN_SURNAME, PARENTS_PLACE_OF_MARRIAGE, PARENTS_DAY_OF_MARRIAGE, PARENTS_MONTH_OF_MARRIAGE, PARENTS_YEAR_OF_MARRIAGE\"";
    public static final String SIBLING_BUNDLING_BIRTH_TO_DEATH_LINKAGE_FIELDS_AS_STRINGS = "\"Birth.FATHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME\"";
    public static final String SIBLING_BUNDLING_DEATH_TO_BIRTH_LINKAGE_FIELDS_AS_STRINGS = "\"Death.FATHER_FORENAME, Death.FATHER_SURNAME, Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME\"";


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
