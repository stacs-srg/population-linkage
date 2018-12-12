package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;

@SuppressWarnings("WeakerAccess")
public class MarriageLinkageSubRecord extends StaticLXP {

    private static Metadata static_md;

    static {

        try {
            static_md = new Metadata(MarriageLinkageSubRecord.class, "BirthLinkageSubRecord");

        } catch (Exception e) {
            // Exception may occur if there's a problem accessing the store, but suppressed
            // here since we may want to create instances without storing.
        }
    }

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int STANDARDISED_ID;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_AGE_OR_DATE_OF_BIRTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_AGE_OR_DATE_OF_BIRTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_DAY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_MONTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_YEAR ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PLACE_OF_MARRIAGE ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHER_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHER_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHER_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHER_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHER_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHER_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHER_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHER_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHER_DECEASED ;

    @Override
    public Metadata getMetaData() {
        return static_md;
    }

    public MarriageLinkageSubRecord(Marriage marriage) {

        put(STANDARDISED_ID, marriage.get(Marriage.STANDARDISED_ID));
        put(BRIDE_FORENAME, marriage.get(Marriage.BRIDE_FORENAME));
        put(BRIDE_SURNAME, marriage.get(Marriage.BRIDE_SURNAME));
        put(BRIDE_AGE_OR_DATE_OF_BIRTH, marriage.get(Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH));
        put(BRIDE_MARITAL_STATUS, marriage.get(Marriage.BRIDE_MARITAL_STATUS));
        put(GROOM_FORENAME, marriage.get(Marriage.GROOM_FORENAME));
        put(GROOM_SURNAME, marriage.get(Marriage.GROOM_SURNAME));
        put(GROOM_AGE_OR_DATE_OF_BIRTH, marriage.get(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH));
        put(GROOM_MARITAL_STATUS, marriage.get(Marriage.GROOM_MARITAL_STATUS));
        put(MARRIAGE_DAY, marriage.get(Marriage.MARRIAGE_DAY));
        put(MARRIAGE_MONTH, marriage.get(Marriage.MARRIAGE_MONTH));
        put(MARRIAGE_YEAR, marriage.get(Marriage.MARRIAGE_YEAR));
        put(PLACE_OF_MARRIAGE, marriage.get(Marriage.PLACE_OF_MARRIAGE));
        put(BRIDE_MOTHER_FORENAME, marriage.get(Marriage.BRIDE_MOTHER_FORENAME));
        put(BRIDE_MOTHER_MAIDEN_SURNAME, marriage.get(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME));
        put(BRIDE_MOTHER_DECEASED, marriage.get(Marriage.BRIDE_MOTHER_DECEASED));
        put(BRIDE_FATHER_FORENAME, marriage.get(Marriage.BRIDE_FATHER_FORENAME));
        put(BRIDE_FATHER_SURNAME, marriage.get(Marriage.BRIDE_FATHER_SURNAME));
        put(BRIDE_FATHER_DECEASED, marriage.get(Marriage.BRIDE_FATHER_DECEASED));
        put(GROOM_MOTHER_FORENAME, marriage.get(Marriage.GROOM_MOTHER_FORENAME));
        put(GROOM_MOTHER_MAIDEN_SURNAME, marriage.get(Marriage.GROOM_MOTHER_MAIDEN_SURNAME));
        put(GROOM_MOTHER_DECEASED, marriage.get(Marriage.GROOM_MOTHER_DECEASED));
        put(GROOM_FATHER_FORENAME, marriage.get(Marriage.GROOM_FATHER_FORENAME));
        put(GROOM_FATHER_SURNAME, marriage.get(Marriage.GROOM_FATHER_SURNAME));
        put(GROOM_FATHER_DECEASED, marriage.get(Marriage.GROOM_FATHER_DECEASED));
    }
}
