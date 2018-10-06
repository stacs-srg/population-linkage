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

import uk.ac.standrews.cs.population_linkage.importers.DeathRecordImporter;
import uk.ac.standrews.cs.population_linkage.normalisation.DateNormalisation;
import uk.ac.standrews.cs.population_linkage.record_types.Death;
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
public class KilmarnockDeathRecordImporter extends DeathRecordImporter {

    private final HashMap<Integer,String> RECORD_LABEL_MAP = new HashMap<>();

    public KilmarnockDeathRecordImporter() {

            // Information available that doesn't currently fit:
            // "day of reg", "month of reg"

            RECORD_LABEL_MAP.put( Death.ORIGINAL_ID, "ID" );

            RECORD_LABEL_MAP.put( Death.YEAR_OF_REGISTRATION, "year of reg" );

            RECORD_LABEL_MAP.put( Death.REGISTRATION_DISTRICT_NUMBER, "identifier" );

            RECORD_LABEL_MAP.put( Death.REGISTRATION_DISTRICT_SUFFIX, "register identifier" );

            RECORD_LABEL_MAP.put( Death.ENTRY, "entry no" );

            // *********************************

            RECORD_LABEL_MAP.put( Death.FORENAME, "forename(s) of deceased" );
            RECORD_LABEL_MAP.put( Death.SURNAME, "surname of deceased" );

            RECORD_LABEL_MAP.put( Death.SEX, "sex" );

            // *********************************

            RECORD_LABEL_MAP.put( Death.DEATH_YEAR, "year" );
            RECORD_LABEL_MAP.put( Death.DEATH_DAY, "day" );

            RECORD_LABEL_MAP.put( Death.AGE_AT_DEATH, "age at death" );

            // *********************************

            RECORD_LABEL_MAP.put( Death.MOTHERS_FORENAME, "mother's forename" );

            RECORD_LABEL_MAP.put( Death.MOTHERS_MAIDEN_SURNAME, "mother's maiden surname" );

            RECORD_LABEL_MAP.put( Death.MOTHER_DECEASED, "if mother deceased" );

            // *********************************

            RECORD_LABEL_MAP.put( Death.FATHERS_FORENAME, "father's forename" );

            RECORD_LABEL_MAP.put( Death.FATHERS_SURNAME, "father's surname" );

            RECORD_LABEL_MAP.put( Death.FATHERS_OCCUPATION, "father's occupation" );

            RECORD_LABEL_MAP.put( Death.FATHER_DECEASED, "if father deceased" );

            // *********************************

            RECORD_LABEL_MAP.put( Death.OCCUPATION, "occupation" );

            RECORD_LABEL_MAP.put( Death.MARITAL_STATUS, "marital status" );

            RECORD_LABEL_MAP.put( Death.SPOUSES_OCCUPATIONS, "spouse's occ" );

            RECORD_LABEL_MAP.put( Death.COD_A, "cause of death");
    }

    private final int[] UNAVAILABLE_RECORD_LABELS = {

            // Fields not present in Kilmarnock dataset.

            Death.CHANGED_FORENAME, Death.CHANGED_SURNAME, Death.CHANGED_MOTHERS_MAIDEN_SURNAME, Death.CORRECTED_ENTRY, Death.IMAGE_QUALITY,
            Death.CHANGED_DEATH_AGE, Death.COD_B, Death.COD_C, Death.PLACE_OF_DEATH, Death.DATE_OF_BIRTH, Death.CERTIFYING_DOCTOR, Death.MOTHERS_SURNAME
    };

    @Override
    public HashMap<Integer, String> getRecordMap() { return RECORD_LABEL_MAP; }

    @Override
    public int[] getUnavailableRecords() { return UNAVAILABLE_RECORD_LABELS; }

    @Override
    public void addAvailableCompoundFields(final DataSet data, final List<String> record, final Death death) {

        death.put(Death.SPOUSES_NAMES, combineFields(data, record, "forename of spouse", "surname of spouse"));
        death.put(Death.PLACE_OF_DEATH, combineFields(data, record, "address 1", "address 2", "address 3"));
    }

    @Override
    public void addAvailableNormalisedFields(DataSet data, List<String> record, Death death) {

        death.put(Death.DEATH_MONTH, DateNormalisation.normaliseMonth(data.getValue(record, "month")));
    }
}
