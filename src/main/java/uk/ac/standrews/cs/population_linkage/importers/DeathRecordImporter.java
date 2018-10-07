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
package uk.ac.standrews.cs.population_linkage.importers;

import uk.ac.standrews.cs.population_linkage.record_types.Death;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility classes for importing records in digitising scotland format
 * Created by al on 8/11/2016.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class DeathRecordImporter extends RecordImporter {

    public abstract void addAvailableCompoundFields(final DataSet data, final List<String> record, final Death death);

    public abstract void addAvailableNormalisedFields(DataSet data, List<String> record, Death death);

    /**
     * @param deaths   the bucket from which to import
     * @param data the source records in digitising scotland format
     * @return the number of records read in
     * @throws IOException
     * @throws RecordFormatException
     * @throws BucketException
     */
    public int importDeathRecords(IBucket<Death> deaths, DataSet data) throws BucketException {

        int count = 0;

        for (List<String> record : data.getRecords()) {

            Death death_record = importDeathRecord(data, record);
            deaths.makePersistent(death_record);
            count++;
        }

        return count;
    }

    /**
     * Fills in a record.
     */
    protected Death importDeathRecord(DataSet data, List<String> record) {

        Death death = new Death();

        addAvailableSingleFields(data, record, death, getRecordMap());
        addAvailableCompoundFields(data, record, death);
        addAvailableNormalisedFields(data, record, death);
        addUnavailableFields(death, getUnavailableRecords());

        return death;
    }
}
