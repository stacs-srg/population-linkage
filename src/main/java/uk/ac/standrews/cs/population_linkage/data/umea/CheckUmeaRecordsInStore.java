/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.interfaces.IInputStream;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.nio.file.Path;
import java.util.Iterator;

public class CheckUmeaRecordsInStore {

    private final Path store_path;
    private final String repo_name;

    public static final String BIRTHS_BUCKET_NAME = "birth_records";

    public CheckUmeaRecordsInStore(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(repo_name);

        System.out.println("Checking records in repository: " + repo_name);
        System.out.println();
        IBucket birth_bucket;
        try {
            birth_bucket = record_repository.getBucket(BIRTHS_BUCKET_NAME);
            System.out.println("Number of records in dataset: " + birth_bucket.size() );
            IInputStream stream = birth_bucket.getInputStream();
            Iterator iter = stream.iterator();
            int count = 0;
            while( iter.hasNext() && count++ <= 3 ) {
                Birth rec = (Birth) iter.next();
                System.out.println( "Found birth record with id: " + rec.getId() );
            }
        } catch( RuntimeException e ) {
            System.out.println("Runtime exception getting births bucket" );
        }
        System.out.println("Complete");
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new CheckUmeaRecordsInStore(store_path, repo_name).run();
        System.exit(0);
    }
}
