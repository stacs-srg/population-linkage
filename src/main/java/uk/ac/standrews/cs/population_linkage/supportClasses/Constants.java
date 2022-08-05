/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.measures.*;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;
import uk.ac.standrews.cs.utilities.phonetic.Metaphone;
import uk.ac.standrews.cs.utilities.phonetic.NYSIIS;
import uk.ac.standrews.cs.utilities.phonetic.PhoneticWrapper;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final int DEFAULT_CHAR_VAL_UPPER_BOUND = 2048;

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

    public static final List<StringMeasure> TRUE_METRICS = List.of(
            COSINE, DAMERAU_LEVENSHTEIN, JACCARD, JENSEN_SHANNON, LEVENSHTEIN, SED);

    public static final List<StringMeasure> NON_METRIC_MEASURES = List.of(
            BAG_DISTANCE, DICE, JARO, JARO_WINKLER, LONGEST_COMMON_SUBSTRING, NEEDLEMAN_WUNSCH, SMITH_WATERMAN);

    public static final List<StringMeasure> PHONETIC_COMPARATORS = List.of(
            METAPHONE, NYSIIS);

    public static final List<StringMeasure> BASE_MEASURES = concatenate(TRUE_METRICS, NON_METRIC_MEASURES, PHONETIC_COMPARATORS);

    public static String stringRepresentationOf(List<Integer> fields, Class<? extends LXP> record, List<String> labels) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getSimpleName()).append("[ ");

        for (int field : fields)
            sb.append(labels.get(field)).append(" ");

        sb.append("]");
        return sb.toString();
    }

    @SafeVarargs
    private static List<StringMeasure> concatenate(final List<StringMeasure>... lists) {

        final List<StringMeasure> result = new ArrayList<>();

        for (List<StringMeasure> list : lists) {
            result.addAll(list);
        }

        return result;
    }

    public static StringMeasure get(String StringMeasure) {
        return get(StringMeasure, DEFAULT_CHAR_VAL_UPPER_BOUND);
    }

    public static StringMeasure get(String StringMeasure, int charValUpperBound) {

        switch (StringMeasure.toUpperCase()) {
            case "COSINE":
                return COSINE;
            case "DAMERAU_LEVENSHTEIN":
                return DAMERAU_LEVENSHTEIN;
            case "JACCARD":
                return JACCARD;
            case "JENSEN_SHANNON":
                return new JensenShannon(charValUpperBound);
            case "LEVENSHTEIN":
                return LEVENSHTEIN;
            case "SED":
                return new SED(charValUpperBound);
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
                throw new UnsupportedOperationException("metric not supported: " + StringMeasure);
        }
    }

    public static void main(String[] args) {

        for (StringMeasure measure : BASE_MEASURES) {
            measure.printExamples();
        }
    }
}
