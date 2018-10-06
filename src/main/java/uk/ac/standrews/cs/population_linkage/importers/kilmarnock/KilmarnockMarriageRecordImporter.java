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
package uk.ac.standrews.cs.population_linkage.importers.kilmarnock;

import uk.ac.standrews.cs.population_linkage.importers.MarriageRecordImporter;
import uk.ac.standrews.cs.population_linkage.normalisation.DateNormalisation;
import uk.ac.standrews.cs.population_linkage.normalisation.PlaceNormalisation;
import uk.ac.standrews.cs.population_linkage.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.HashMap;
import java.util.List;

/**
 * Utility classes for importing records in digitising scotland format
 * Created by al on 8/11/2016.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class KilmarnockMarriageRecordImporter extends MarriageRecordImporter {

    private final HashMap<Integer,String> RECORD_LABEL_MAP = new HashMap<>();

    public KilmarnockMarriageRecordImporter() {

            // Information available that doesn't currently fit:

            // "groom's mother's occ"
            // "bride's mother's occ"
            // "groom's mother's other names"
            // "bride's mother's other name/s"

            RECORD_LABEL_MAP.put( Marriage.ORIGINAL_ID, "ID");

            RECORD_LABEL_MAP.put( Marriage.YEAR_OF_REGISTRATION, "KILM_YearofRegistration");

            RECORD_LABEL_MAP.put( Marriage.REGISTRATION_DISTRICT_NUMBER, "RD identifier");

            RECORD_LABEL_MAP.put( Marriage.REGISTRATION_DISTRICT_SUFFIX, "register identifier");

            RECORD_LABEL_MAP.put( Marriage.ENTRY, "entry number");

            RECORD_LABEL_MAP.put( Marriage.DENOMINATION, "denomination");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.BRIDE_FORENAME, "forename of bride");
            RECORD_LABEL_MAP.put( Marriage.BRIDE_SURNAME, "surname of bride");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.GROOM_FORENAME, "forename of groom");
            RECORD_LABEL_MAP.put( Marriage.GROOM_SURNAME, "surname of groom");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.MARRIAGE_YEAR, "yearofMar");
            RECORD_LABEL_MAP.put( Marriage.MARRIAGE_DAY, "dayofMar");

            // RECORD_LABEL_MAP.put( PLACE_OF_MARRIAGE, "place of marriage 3" );

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH, "age of bride");
            RECORD_LABEL_MAP.put( Marriage.GROOM_AGE_OR_DATE_OF_BIRTH, "age of groom");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.BRIDE_FATHERS_FORENAME, "bride's father's forename");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_FATHERS_SURNAME, "bride's father's surname");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_MOTHERS_FORENAME, "bride's mother's forename");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_MOTHERS_MAIDEN_SURNAME, "bride's mother's maiden surname");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.GROOM_FATHERS_FORENAME, "groom's father's forename");

            RECORD_LABEL_MAP.put( Marriage.GROOM_FATHERS_SURNAME, "groom's father's surname");

            RECORD_LABEL_MAP.put( Marriage.GROOM_MOTHERS_FORENAME, "groom's mother's forename");

            RECORD_LABEL_MAP.put( Marriage.GROOM_MOTHERS_MAIDEN_SURNAME, "groom's mother's maiden surname");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.BRIDE_MARITAL_STATUS, "marital status of bride");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_DID_NOT_SIGN, "did bride sign?");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_OCCUPATION, "occupation of bride");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_FATHER_OCCUPATION, "bride's father's occupation");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_FATHER_DECEASED, "if bride's father deceased");

            RECORD_LABEL_MAP.put( Marriage.BRIDE_MOTHER_DECEASED, "if bride's mother deceased");

            // *********************************

            RECORD_LABEL_MAP.put( Marriage.GROOM_MARITAL_STATUS, "marital status of groom");

            RECORD_LABEL_MAP.put( Marriage.GROOM_DID_NOT_SIGN, "did groom sign?");

            RECORD_LABEL_MAP.put( Marriage.GROOM_OCCUPATION, "occupation of groom");

            RECORD_LABEL_MAP.put( Marriage.GROOM_FATHERS_OCCUPATION, "groom's father's occupation");

            RECORD_LABEL_MAP.put( Marriage.GROOM_FATHER_DECEASED, "if groom's father deceased");

            RECORD_LABEL_MAP.put( Marriage.GROOM_MOTHER_DECEASED, "if groom's mother deceased");

    };

    public final int[] UNAVAILABLE_RECORD_LABELS = {

            // Fields not present in Kilmarnock dataset.

            Marriage.CHANGED_GROOM_FORENAME, Marriage.IMAGE_QUALITY, Marriage.CHANGED_GROOM_SURNAME, Marriage.CHANGED_BRIDE_SURNAME, Marriage.CORRECTED_ENTRY, Marriage.CHANGED_BRIDE_FORENAME
    };

    public HashMap<Integer, String> getRecordMap() {
        return RECORD_LABEL_MAP;
    }

    public int[] getUnavailableRecords() {
        return UNAVAILABLE_RECORD_LABELS;
    }

    public void addAvailableNormalisedFields(DataSet data, List<String> record, Marriage marriage) {

        marriage.put(Marriage.MARRIAGE_MONTH, DateNormalisation.normaliseMonth(data.getValue(record, "monthofMar")));
        marriage.put(Marriage.PLACE_OF_MARRIAGE, PlaceNormalisation.normalisePlace(data.getValue(record, "place of marriage 3")));
    }

    public void addAvailableCompoundFields(final DataSet data, final List<String> record, final Marriage marriage) {

        marriage.put(Marriage.BRIDE_ADDRESS, combineFields(data, record, "address of bride 1", "address of bride 2", "address of bride 3"));
        marriage.put(Marriage.GROOM_ADDRESS, combineFields(data, record, "address of groom 1", "address of groom 2", "address of groom 3"));

        // Place of marriage 3 is the townname in this dataset - use this.
        // TODO look at this and decide what to do - create a cannonical field?
    }
}
