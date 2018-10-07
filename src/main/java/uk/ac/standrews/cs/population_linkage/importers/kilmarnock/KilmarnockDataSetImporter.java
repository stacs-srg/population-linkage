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

import uk.ac.standrews.cs.population_linkage.importers.*;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class KilmarnockDataSetImporter extends DataSetImporter {

    private final BirthRecordImporter birth_record_importer;
    private final DeathRecordImporter death_record_importer;
    private final MarriageRecordImporter marriage_record_importer;

    public KilmarnockDataSetImporter() {

        birth_record_importer = new KilmarnockBirthRecordImporter();
        death_record_importer = new KilmarnockDeathRecordImporter();
        marriage_record_importer = new KilmarnockMarriageRecordImporter();
    }

    @Override
    public String getDataSetName() {
        return "Kilmarnock";
    }

    @Override
    public int importBirthRecords(RecordRepository record_repository, DataSet birth_records) throws BucketException {

        return birth_record_importer.importBirthRecords(record_repository.births, birth_records);
    }

    @Override
    public int importDeathRecords(RecordRepository record_repository, DataSet death_records) throws BucketException {

        return death_record_importer.importDeathRecords(record_repository.deaths, death_records);
    }

    @Override
    public int importMarriageRecords(RecordRepository record_repository, DataSet marriage_records) throws BucketException {

        return marriage_record_importer.importMarriageRecords(record_repository.marriages, marriage_records);
    }
}
