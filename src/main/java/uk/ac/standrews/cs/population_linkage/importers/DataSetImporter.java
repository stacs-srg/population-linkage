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

import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public abstract class DataSetImporter {

    public abstract String getDataSetName();

    public abstract int importBirthRecords(RecordRepository record_repository, DataSet birth_records) throws BucketException;

    public abstract int importDeathRecords(RecordRepository record_repository, DataSet death_records) throws BucketException;

    public abstract int importMarriageRecords(RecordRepository record_repository, DataSet marriage_records) throws BucketException;
}
