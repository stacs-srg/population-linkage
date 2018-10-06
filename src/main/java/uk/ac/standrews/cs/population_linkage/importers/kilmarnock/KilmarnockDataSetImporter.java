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

import uk.ac.standrews.cs.population_linkage.importers.BirthRecordImporter;
import uk.ac.standrews.cs.population_linkage.importers.DataSetImporter;
import uk.ac.standrews.cs.population_linkage.importers.DeathRecordImporter;
import uk.ac.standrews.cs.population_linkage.importers.MarriageRecordImporter;

import java.nio.file.Path;

/**
 * Module to ingest BBM records into a store initialised by class such as StoreInitialiser in this package
 * Created by al on 22/3/2017.
 *
 * @author al@st-andrews.ac.uk
 */
public class KilmarnockDataSetImporter extends DataSetImporter {

    public KilmarnockDataSetImporter(Path store_path, String repo_name, boolean from_resource, Path birth_records_path, Path death_records_path, Path marriage_records_path) throws Exception {

        super(store_path, repo_name, from_resource, birth_records_path, death_records_path, marriage_records_path);
    }

    @Override
    protected BirthRecordImporter getBirthImporter() {
        return new KilmarnockBirthRecordImporter();
    }

    @Override
    protected DeathRecordImporter getDeathImporter() {
        return new KilmarnockDeathRecordImporter();
    }

    @Override
    protected MarriageRecordImporter getMarriageImporter() {
        return new KilmarnockMarriageRecordImporter();
    }

    @Override
    public String getDataSetName() {
        return "Kilmarnock";
    }
}
