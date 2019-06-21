package uk.ac.standrews.cs.population_linkage.experiments.umea.calibration;

import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class GroundTruth {

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
}
