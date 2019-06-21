package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_records.record_types.Birth;
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
    public static final JensenShannon JENSEN_SHANNON = new JensenShannon();
    public static final Levenshtein LEVENSHTEIN = new Levenshtein();
    public static final SED SED = new SED();

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

    @SafeVarargs
    private static List<StringMetric> concatenate(final List<StringMetric>... lists) {

        final List<StringMetric> result = new ArrayList<>();

        for (List<StringMetric> list : lists) {
            result.addAll(list);
        }

        return result;
    }
}
