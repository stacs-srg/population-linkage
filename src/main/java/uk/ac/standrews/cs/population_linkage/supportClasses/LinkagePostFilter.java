package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

public class LinkagePostFilter {

    private static Integer SIBLINGS_MAX_AGE_DIFF = null;
    private static Integer MIN_AGE_AT_MARRIAGE = 15;

    public static boolean isViableBBSiblingLink(RecordPair pair) {
        if(SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int yob2 = Integer.parseInt(pair.record2.getString(Birth.BIRTH_YEAR));

            return Math.abs(yob1 - yob2) <= SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return true;
        }
    }

    public static boolean isViableBDSiblingLink(RecordPair pair) {
        if(SIBLINGS_MAX_AGE_DIFF == null) return true;

        try {
            int yob1 = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int approxYob2 = Integer.parseInt(pair.record2.getString(Death.DEATH_YEAR)) - Integer.parseInt(pair.record2.getString(Death.AGE_AT_DEATH));

            return Math.abs(yob1 - approxYob2) <= SIBLINGS_MAX_AGE_DIFF;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or DEATH_YEAR is invalid
            return true;
        }
    }

    public static boolean isViableBDIdentityLink(RecordPair pair) {

        try {
            int yob = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int yod = Integer.parseInt(pair.record2.getString(Death.DEATH_YEAR));

            return yod >= yob; // is year of death later than year of birth

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or DEATH_YEAR is invalid
            return true;
        }
    }

    public static boolean isViableGroomBirthIdentityLink(RecordPair pair) {

        try {
            int yom = Integer.parseInt(pair.record1.getString(Marriage.MARRIAGE_YEAR));
            int yob = Integer.parseInt(pair.record2.getString(Birth.BIRTH_YEAR));

            return yob + MIN_AGE_AT_MARRIAGE <= yom; // is person at least 15 on marriage date

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or MARRIAGE_YEAR is invalid
            return true;
        }
    }

    public static boolean isViableDeathGroomIdentityLink(RecordPair pair) {

        try {
            int yod = Integer.parseInt(pair.record1.getString(Death.DEATH_YEAR));
            int yom = Integer.parseInt(pair.record2.getString(Marriage.MARRIAGE_YEAR));

            return yod >= yom; // is death after marriage

        } catch(NumberFormatException e) { // in this case a DEATH_YEAR or MARRIAGE_YEAR is invalid
            return true;
        }
    }

    public static boolean isViableDeathBrideIdentityLink(RecordPair pair) {
        return isViableDeathGroomIdentityLink(pair);
    }

    public static boolean isViableBrideBirthIdentityLink(RecordPair pair) {
        return isViableGroomBirthIdentityLink(pair);
    }

    public static boolean noViabilityCheck(RecordPair pair) {
        return true; // returns true as all pairs are valid as no validity criteria is specified
    }


    public static void setMaxSiblingGap(Integer maxSiblingGap) {
        SIBLINGS_MAX_AGE_DIFF = maxSiblingGap;
    }

    public static void setMinAgeAtMarriage(Integer minAgeAtMarriage) {
        MIN_AGE_AT_MARRIAGE = minAgeAtMarriage;
    }
}
