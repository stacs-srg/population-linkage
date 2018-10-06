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

import java.nio.file.Path;

public abstract class DataSetImporter {

    private boolean from_resource;

    private Path birth_records_path;
    private Path death_records_path;
    private Path marriage_records_path;

    RecordRepository record_repository;

    protected abstract BirthRecordImporter getBirthImporter();
    protected abstract DeathRecordImporter getDeathImporter();
    protected abstract MarriageRecordImporter getMarriageImporter();

    public abstract String getDataSetName();

    public DataSetImporter(Path store_path, String repo_name, boolean from_resource, Path birth_records_path, Path death_records_path, Path marriage_records_path) throws Exception {

        this.from_resource = from_resource;

        this.birth_records_path = birth_records_path;
        this.death_records_path = death_records_path;
        this.marriage_records_path = marriage_records_path;

        record_repository = new RecordRepository(store_path, repo_name);
    }

    public int importBirthRecords() throws Exception {

        return getBirthImporter().importBirthRecords(record_repository.births, birth_records_path, from_resource);
    }

    public int importDeathRecords() throws Exception {

        return getDeathImporter().importDeathRecords(record_repository.deaths, death_records_path, from_resource);
    }

    public int importMarriageRecords() throws Exception {

        return getMarriageImporter().importMarriageRecords(record_repository.marriages, marriage_records_path, from_resource);
    }
}
