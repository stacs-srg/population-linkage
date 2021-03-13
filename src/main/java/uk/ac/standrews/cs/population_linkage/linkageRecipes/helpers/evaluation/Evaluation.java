/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation;

import java.util.Arrays;
import java.util.List;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.Pair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

public class Evaluation {

    /**
     * If TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN is false, then the recipe is tuned to the Umea dataset,
     * for which it is assumed that where an identifier is not
     * present, this means that the corresponding person/record is not included in the dataset. This
     * would be because the parent was not born or married within the geographical and temporal region.
     * <p>
     * Therefore we interpret absence of an identifier as having a particular meaning, and thus where
     * one record in a pair has an identifier and one doesn't, we classify as a non-match.
     * <p>
     * For use in a more general context with dirtier data, TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN
     * should be set to true. We then have less information about what a missing
     * identifier means, so classify as unknown.
     */
    protected static final boolean TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN = false;

    public static LinkStatus trueMatch(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives, final List<List<Pair>> excludedMatchMappings, boolean reversed) {
        if(reversed) {
            return trueMatch(record2, record1, true_match_alternatives, excludedMatchMappings);
        } else {
            return trueMatch(record1, record2, true_match_alternatives, excludedMatchMappings);
        }
    }

    public static LinkStatus trueMatch(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives, final List<List<Pair>> excludedMatchMappings) {

        for (List<Pair> excludedMatchFields : excludedMatchMappings) {
            boolean excludedMatch = true;

            for (Pair fields : excludedMatchFields) {
                if (!notEmptyAndAreEqual(record1.getString(fields.first), record2.getString(fields.second))) {
                    excludedMatch = false; // if any one pair in the set doesn't match then it isn't a match
                }
            }
            if(excludedMatch) return LinkStatus.EXCLUDED;
        }

        // this loop is about the different gt alternatives - we only need one of the to agree to the link being true
        for (List<Pair> true_match_fields : true_match_alternatives) {

            boolean match = true;

            // this loop is about checking every pair in a given gt alternative -
            // we need every pair in a given alternative to be equal else it isn't a match
            for (Pair fields : true_match_fields) {
                if (!notEmptyAndAreEqual(record1.getString(fields.first), record2.getString(fields.second))) {
                    match = false; // if any one pair in the alternative doesn't match then it isn't a match
                }
            }
            if (match) return LinkStatus.TRUE_MATCH; // hence why this can return before all gt alternatives are checked if we get agreement
        }

        boolean all_empty = allEmpty(record1, record2, true_match_alternatives);
        boolean any_empty = anyEmpty(record1, record2, true_match_alternatives);

        if ((TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && any_empty) || (!TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && all_empty)) {
            return LinkStatus.UNKNOWN;
        }

        return LinkStatus.NOT_TRUE_MATCH;
    }

    private static boolean allEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            if (!allFieldsEmpty(record1, record2, true_match_fields)) return false;
        }
        return true;
    }

    public static boolean allFieldsEmpty(final LXP record1, final LXP record2, final List<Pair> true_match_fields) {

        for (Pair fields : true_match_fields) {
            if (identityFieldNotEmpty(record1, fields.first)) return false;
            if (identityFieldNotEmpty(record2, fields.second)) return false;
        }
        return true;
    }

    private static boolean anyEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            for (Pair fields : true_match_fields) {
                if (identityFieldEmpty(record1, fields.first)) return true;
                if (identityFieldEmpty(record2, fields.second)) return true;
            }
        }
        return false;
    }

    private static boolean identityFieldEmpty(final LXP record, final int field_number) {

        // Ignore the record id field.
        return field_number != getRecordIdFieldNumber(record) && record.getString(field_number).isEmpty();
    }

    private static boolean identityFieldNotEmpty(final LXP record, final int field_number) {

        // Ignore the record id field.
        return field_number != getRecordIdFieldNumber(record) && !record.getString(field_number).isEmpty();
    }

    public static int getRecordIdFieldNumber(final LXP record) {

        if (record instanceof Birth) return Birth.STANDARDISED_ID;
        if (record instanceof Marriage) return Marriage.STANDARDISED_ID;
        if (record instanceof Death) return Death.STANDARDISED_ID;

        try { // this is used in the case that storr has lost track of the instance type
            return record.getMetaData().getSlot("STANDARDISED_ID");
        } catch (Exception e) {
            throw new Error("Record of unknown type: " + record.getClass().getCanonicalName());
        }
    }

    public static Pair pair(final int first, final int second) {
        return new Pair(first, second);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> list(final T... values) {
        return Arrays.asList(values);
    }

    static boolean notEmptyAndAreEqual(final String s1, final String s2) {
        return !s1.isEmpty() && s1.equals(s2);
    }
}
