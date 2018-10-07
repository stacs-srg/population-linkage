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

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public abstract class DataSetImporter {

    private DataSet birth_records;
    private DataSet death_records;
    private DataSet marriage_records;

    RecordRepository record_repository;

    protected abstract BirthRecordImporter getBirthImporter();
    protected abstract DeathRecordImporter getDeathImporter();
    protected abstract MarriageRecordImporter getMarriageImporter();

    public abstract String getDataSetName();

    public DataSetImporter(Path store_path, String repo_name, DataSet birth_records, DataSet death_records, DataSet marriage_records) throws Exception {

        this.birth_records = birth_records;
        this.death_records = death_records;
        this.marriage_records = marriage_records;

        record_repository = new RecordRepository(store_path, repo_name);
    }

    public int importBirthRecords() throws Exception {

        return getBirthImporter().importBirthRecords(record_repository.births, birth_records);
    }

    public int importDeathRecords() throws Exception {

        return getDeathImporter().importDeathRecords(record_repository.deaths, death_records);
    }

    public int importMarriageRecords() throws Exception {

        return getMarriageImporter().importMarriageRecords(record_repository.marriages, marriage_records);
    }
}
