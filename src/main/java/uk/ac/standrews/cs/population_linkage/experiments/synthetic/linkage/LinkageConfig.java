package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;

public class LinkageConfig {

    private static int SIBLINGS_MAX_AGE_DIFF = 30;

    public LinkageConfig(int siblingMaxAgeDiff) {
        SIBLINGS_MAX_AGE_DIFF = siblingMaxAgeDiff;
    }

    public static boolean isViableLink(RecordPair pair) {
        try {
            int yob1 = Integer.parseInt(pair.record1.getString(Birth.BIRTH_YEAR));
            int yob2 = Integer.parseInt(pair.record2.getString(Birth.BIRTH_YEAR));

            if (Math.abs(yob1 - yob2) > SIBLINGS_MAX_AGE_DIFF) {
                return false;
            }

            return true;

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR is invalid
            return false;
        }
    }


}
