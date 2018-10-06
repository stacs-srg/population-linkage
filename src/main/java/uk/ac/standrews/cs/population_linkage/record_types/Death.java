/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module linkage-java.
 *
 * linkage-java is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * linkage-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with linkage-java. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.record_types;

import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;
import uk.ac.standrews.cs.utilities.JSONReader;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

/**
 * Created by al on 03/10/2014.
 */
public class Death extends StaticLXP {

    private static Metadata static_md;
    static {

        try {
            static_md = new Metadata( Death.class,"Death" );

        } catch (Exception e) {
            ErrorHandling.exceptionError( e );
        }
    }

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ORIGINAL_ID ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SEX ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_MOTHERS_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int YEAR_OF_REGISTRATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ENTRY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_SUFFIX ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_NUMBER ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CORRECTED_ENTRY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int IMAGE_QUALITY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int COD_A ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int COD_B ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int COD_C ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PLACE_OF_DEATH ;

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
    public static int CHANGED_DEATH_AGE ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SPOUSES_NAMES ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SPOUSES_OCCUPATIONS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CERTIFYING_DOCTOR ;

    public Death() {

        super();
    }

    public Death(long persistent_Object_id, JSONReader reader, IBucket bucket) throws PersistentObjectException, IllegalKeyException {

        super(persistent_Object_id, reader, bucket);
    }

    @Override
    public Metadata getMetaData() {
        return static_md;
    }

    public String getForename() {

        return getString(FORENAME);
    }

    public String getSurname() {

        return getString(SURNAME);
    }

    public String getFathersForename() {

        return getString(FATHERS_FORENAME);
    }

    public String getFathersSurname() {

        return getString(FATHERS_SURNAME);
    }

    public String getMothersForename() {

        return getString(MOTHERS_FORENAME);
    }

    public String getMothersMaidenSurname() {

        return getString(MOTHERS_MAIDEN_SURNAME);
    }

    public String getDOB() {

        return getString(DATE_OF_BIRTH);
    }

    public String getSex() {
        return getString(SEX);
    }
}
