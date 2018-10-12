package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

public class BirthLinkageSubRecord extends StaticLXP {

    private static Metadata static_md;

    static {

        try {
            static_md = new Metadata(BirthLinkageSubRecord.class, "BirthLinkageSubRecord");

        } catch (Exception e) {
            ErrorHandling.exceptionError(e);
        }
    }

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int STANDARDISED_ID;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_MAIDEN_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_DAY_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_MONTH_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_YEAR_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_PLACE_OF_MARRIAGE;

    @Override
    public Metadata getMetaData() {
        return static_md;
    }

    public BirthLinkageSubRecord(Birth birth) {

        put(STANDARDISED_ID, birth.get(Birth.STANDARDISED_ID));
        put(MOTHERS_FORENAME, birth.get(Birth.MOTHERS_FORENAME));
        put(MOTHERS_MAIDEN_SURNAME, birth.get(Birth.MOTHERS_MAIDEN_SURNAME));
        put(FATHERS_FORENAME, birth.get(Birth.FATHERS_FORENAME));
        put(FATHERS_SURNAME, birth.get(Birth.FATHERS_SURNAME));
        put(PARENTS_DAY_OF_MARRIAGE, birth.get(Birth.PARENTS_DAY_OF_MARRIAGE));
        put(PARENTS_MONTH_OF_MARRIAGE, birth.get(Birth.PARENTS_MONTH_OF_MARRIAGE));
        put(PARENTS_YEAR_OF_MARRIAGE, birth.get(Birth.PARENTS_YEAR_OF_MARRIAGE));
        put(PARENTS_PLACE_OF_MARRIAGE, birth.get(Birth.PARENTS_PLACE_OF_MARRIAGE));
    }
}
