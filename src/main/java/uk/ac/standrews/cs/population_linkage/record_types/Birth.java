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
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;
import uk.ac.standrews.cs.utilities.JSONReader;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

/**
 * Created by al on 03/10/2014.
 */
public class Birth extends StaticLXP {

    private static Metadata static_md;
    static {

        try {
            static_md = new Metadata( Birth.class,"Birth" );

        } catch (Exception e) {
            ErrorHandling.exceptionError( e );
        }
    }

    // Fields need to be duplicated for reflective use to work.

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ORIGINAL_ID;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SEX;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_FORENAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_MAIDEN_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CHANGED_MOTHERS_MAIDEN_SURNAME;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_OCCUPATION;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int YEAR_OF_REGISTRATION;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ENTRY;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_SUFFIX;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int REGISTRATION_DISTRICT_NUMBER;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int CORRECTED_ENTRY;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int IMAGE_QUALITY;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BIRTH_DAY;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BIRTH_MONTH;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BIRTH_YEAR;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int BIRTH_ADDRESS;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ILLEGITIMATE_INDICATOR;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ADOPTION;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_DAY_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_MONTH_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_YEAR_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int PARENTS_PLACE_OF_MARRIAGE;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int INFORMANT_DID_NOT_SIGN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int INFORMANT;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FAMILY;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int DEATH;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FORENAME_CLEAN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int SURNAME_CLEAN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_FORENAME_CLEAN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int FATHERS_SURNAME_CLEAN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_FORENAME_CLEAN;

    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int MOTHERS_SURNAME_CLEAN;

    public Birth() {

        super();
    }

    public Birth(long persistent_object_id, JSONReader reader, IBucket bucket) throws PersistentObjectException {

        super(persistent_object_id, reader, bucket);
    }

    @Override
    public Metadata getMetaData() {
        return static_md;
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

    public String getPlaceOfMarriage() {

        return getString(PARENTS_PLACE_OF_MARRIAGE);
    }

    public String getDateOfMarriage() {

        return DateNormalisation.cleanDate(getString(PARENTS_DAY_OF_MARRIAGE), getString(PARENTS_MONTH_OF_MARRIAGE), getString(PARENTS_YEAR_OF_MARRIAGE));
    }

    public String getDOB() {

        return getString(BIRTH_DAY) + "/" + getString(BIRTH_MONTH) + "/" + getString(BIRTH_YEAR);
    }

    public String getForename() {
        return getString(FORENAME);
    }

    public String getSurname() {
        return getString(SURNAME);
    }

    public String getSex() {
        return getString(SEX);
    }

    public String getForenameClean() { return getString(FORENAME_CLEAN); }

    public String getSurnameClean() {
        return getString(SURNAME_CLEAN);
    }

    public String getFathersForenameClean() { return getString(FATHERS_SURNAME_CLEAN); }

    public String getFathersSurnameClean() {
        return getString(FATHERS_SURNAME_CLEAN);
    }

    public String getMothersForenameClean() { return getString(MOTHERS_SURNAME_CLEAN); }

    public String getMothersSurnameClean() { return getString(MOTHERS_SURNAME_CLEAN); }

    @Override
    public boolean equals( final Object o ) {
        return o instanceof Birth && (( ((Birth) o).getId()) == this.getId());
    }

    @Override
    public int hashCode() { return new Long( this.getId() ).hashCode(); }
}
