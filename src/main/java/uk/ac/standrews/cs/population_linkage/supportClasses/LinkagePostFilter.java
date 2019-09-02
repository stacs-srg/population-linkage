package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

public class LinkagePostFilter {

    private static Integer SIBLINGS_MAX_AGE_DIFF = null;

    public static boolean isViableBBSiblingLink(RecordPair pair) {
        if(SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int yob2 = Integer.parseInt(pair.record2.getString(Birth.BIRTH_YEAR));

            return Math.abs(yob1 - yob2) <= SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return false;
        }
    }

    public static boolean isViableBDSiblingLink(RecordPair pair) {
        if(SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int approxYob2 = Integer.parseInt(pair.record2.getString(Death.DEATH_YEAR)) - Integer.parseInt(pair.record2.getString(Death.AGE_AT_DEATH));

            return Math.abs(yob1 - approxYob2) <= SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return false;
        }
    }


    public static void setMaxSiblingGap(int maxSiblingGap) {
        SIBLINGS_MAX_AGE_DIFF = maxSiblingGap;
    }
}
