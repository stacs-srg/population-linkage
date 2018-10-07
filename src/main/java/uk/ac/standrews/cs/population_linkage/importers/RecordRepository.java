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

import uk.ac.standrews.cs.population_linkage.record_types.Birth;
import uk.ac.standrews.cs.population_linkage.record_types.Death;
import uk.ac.standrews.cs.population_linkage.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.BucketKind;
import uk.ac.standrews.cs.storr.impl.Store;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.interfaces.IRepository;
import uk.ac.standrews.cs.storr.interfaces.IStore;

import java.nio.file.Path;

/**
 * Module to initialise the store ready for ingesting of birth/death/marriage records.
 * Created by al on 22/3/2017.
 *
 * @author al@st-andrews.ac.uk
 */
public class RecordRepository {

    private static final String BIRTHS_BUCKET_NAME = "birth_records";              // Name of bucket containing birth records (inputs).
    private static final String DEATHS_BUCKET_NAME = "death_records";              // Name of bucket containing death records (inputs).
    private static final String MARRIAGES_BUCKET_NAME = "marriage_records";        // Name of bucket containing marriage records (inputs).

    private IStore store;

    public IBucket<Birth> births;
    public IBucket<Marriage> marriages;
    public IBucket<Death> deaths;

    public RecordRepository(Path store_path, String repository_name) throws Exception {

        store = new Store(store_path);

        initialiseBuckets(repository_name);
    }

    private void initialiseBuckets(String repository_name) throws RepositoryException {

        try {
            IRepository input_repository = store.getRepository(repository_name);

            births = input_repository.getBucket(BIRTHS_BUCKET_NAME, Birth.class);
            deaths = input_repository.getBucket(DEATHS_BUCKET_NAME, Death.class);
            marriages = input_repository.getBucket(MARRIAGES_BUCKET_NAME, Marriage.class);

        } catch (RepositoryException e) {

            // The repository hasn't previously been initialised.

            IRepository input_repository = store.makeRepository(repository_name);

            births = input_repository.makeBucket(BIRTHS_BUCKET_NAME, BucketKind.DIRECTORYBACKED, Birth.class);
            deaths = input_repository.makeBucket(DEATHS_BUCKET_NAME, BucketKind.DIRECTORYBACKED, Death.class);
            marriages = input_repository.makeBucket(MARRIAGES_BUCKET_NAME, BucketKind.DIRECTORYBACKED, Marriage.class);
        }
    }
}
