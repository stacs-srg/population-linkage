package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;

@SuppressWarnings("WeakerAccess")
public class DeathLinkageSubRecord extends StaticLXP {

    private static Metadata static_md;

    static {

        try {
            static_md = new Metadata(DeathLinkageSubRecord.class, "BirthLinkageSubRecord");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int STANDARDISED_ID;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SEX ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DATE_OF_BIRTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DEATH_DAY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DEATH_MONTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DEATH_YEAR ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int AGE_AT_DEATH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SPOUSES_NAMES ;

    @Override
    public Metadata getMetaData() {
        return static_md;
    }

    public DeathLinkageSubRecord(Death death) {

        put(STANDARDISED_ID, death.get(Death.STANDARDISED_ID));
        put(FORENAME, death.get(Death.FORENAME));
        put(SURNAME, death.get(Death.SURNAME));
        put(SEX, death.get(Death.SEX));
        put(FATHERS_FORENAME, death.get(Death.FATHERS_FORENAME));
        put(FATHERS_SURNAME, death.get(Death.FATHERS_SURNAME));
        put(MOTHERS_FORENAME, death.get(Death.MOTHERS_FORENAME));
        put(MOTHERS_MAIDEN_SURNAME, death.get(Death.MOTHERS_MAIDEN_SURNAME));
        put(DATE_OF_BIRTH, death.get(Death.DATE_OF_BIRTH));
        put(DEATH_DAY, death.get(Death.DEATH_DAY));
        put(DEATH_MONTH, death.get(Death.DEATH_MONTH));
        put(DEATH_YEAR, death.get(Death.DEATH_YEAR));
        put(AGE_AT_DEATH, death.get(Death.AGE_AT_DEATH));
        put(MARITAL_STATUS, death.get(Death.MARITAL_STATUS));
        put(SPOUSES_NAMES, death.get(Death.SPOUSES_NAMES));
    }
}
