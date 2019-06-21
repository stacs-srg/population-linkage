package uk.ac.standrews.cs.population_linkage.experiments.skye.calibration;

import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class GroundTruth {

    public static LinkStatus isTrueMatchBirthSiblingSkye(LXP record1, LXP record2) {

        final String b1_family_id = record1.getString(Birth.FAMILY);
        final String b2_family_id = record2.getString(Birth.FAMILY);

        if (b1_family_id.isEmpty() || b2_family_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_family_id.equals(b2_family_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }
}
