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

import uk.ac.standrews.cs.population_linkage.normalisation.DateNormalisation;
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
public class Marriage extends StaticLXP {

    private static Metadata static_md;
    static {

        try {
            static_md = new Metadata( Marriage.class,"Marriage" );

        } catch (Exception e) {
            ErrorHandling.exceptionError( e );
        }
    }

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ORIGINAL_ID ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHERS_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHER_OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_GROOM_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int IMAGE_QUALITY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_ADDRESS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHERS_MAIDEN_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHERS_OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ENTRY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_ADDRESS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_MONTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_YEAR ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_DID_NOT_SIGN ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DENOMINATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_OCCUPATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_GROOM_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_DID_NOT_SIGN ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_MOTHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHERS_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MOTHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_NUMBER ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_SUFFIX ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_MARITAL_STATUS ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FATHERS_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_AGE_OR_DATE_OF_BIRTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_BRIDE_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CORRECTED_ENTRY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_BRIDE_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BRIDE_FORENAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHERS_SURNAME ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_FATHER_DECEASED ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int GROOM_AGE_OR_DATE_OF_BIRTH ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int YEAR_OF_REGISTRATION ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MARRIAGE_DAY ;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PLACE_OF_MARRIAGE ;

    //******************** Constructors ********************

    public Marriage() {

        super();
    }

    public Marriage(long persistent_object_id, JSONReader reader, IBucket bucket) throws PersistentObjectException, IllegalKeyException {

        super(persistent_object_id, reader, bucket);
    }

    @Override
    public Metadata getMetaData() {
        return static_md;
    }

    //******************** Selectors ********************

    public String getGroomsForename() {

        return getString(GROOM_FORENAME);
    }

    public String getGroomsSurname() {

        return getString(GROOM_SURNAME);
    }

    public String getGroomsDob() {

        return getString(GROOM_AGE_OR_DATE_OF_BIRTH);
    }

    public String getGFFN() {

        return getString(GROOM_FATHERS_FORENAME);
    }

    public String getGFLN() {

        return getString(GROOM_FATHERS_SURNAME);
    }

    public String getGMFN() {

        return getString(GROOM_MOTHERS_FORENAME);
    }

    public String getGMMN() {

        return getString(GROOM_MOTHERS_MAIDEN_SURNAME);
    }

    public String getBridesForename() {

        return getString(BRIDE_FORENAME);
    }

    public String getBridesSurname() {

        return getString(BRIDE_SURNAME);
    }

    public String getBridesDob() {

        return getString(BRIDE_AGE_OR_DATE_OF_BIRTH);
    }

    public String getBFFN() {

        return getString(BRIDE_FATHERS_FORENAME);
    }

    public String getBFLN() {

        return getString(BRIDE_FATHERS_SURNAME);
    }

    public String getBMFN() {

        return getString(BRIDE_MOTHERS_FORENAME);
    }

    public String getBMMN() {

        return getString(BRIDE_MOTHERS_MAIDEN_SURNAME);
    }

    public String getPlaceOfMarriage() {

        return getString(PLACE_OF_MARRIAGE);
    }

    public String getDateOfMarriage() {

        return DateNormalisation.cleanDate(getString(MARRIAGE_DAY), getString(MARRIAGE_MONTH), getString(MARRIAGE_YEAR));
    }

    public String getGroomsFathersForename() {
        return getString( GROOM_FATHERS_FORENAME );
    }

    public String getGroomsFathersSurname() {
        return getString( GROOM_FATHERS_SURNAME );
    }

    public String getGroomsMothersForename() {
        return getString( GROOM_MOTHERS_FORENAME );
    }

    public String getGroomsMothersMaidenSurname() {
        return getString( GROOM_MOTHERS_MAIDEN_SURNAME );
    }

    public String getBridesFathersForename() {
        return getString( BRIDE_FATHERS_FORENAME );
    }

    public String getBridesFathersSurname() {
        return getString( BRIDE_FATHERS_SURNAME );
    }

    public String getBridesMothersForename() {
        return getString( BRIDE_MOTHERS_FORENAME );
    }

    public String getBridesMothersMaidenSurname() {
        return getString( BRIDE_MOTHERS_MAIDEN_SURNAME );
    }
}
