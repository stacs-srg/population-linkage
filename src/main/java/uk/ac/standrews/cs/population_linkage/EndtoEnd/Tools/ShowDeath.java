/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.Tools;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;

import java.nio.file.Path;
import java.util.Scanner;

public class ShowDeath {

    private final RecordRepository record_repository;
    private final IBucket<Birth> deaths_bucket;
    protected Path store_path;

    public ShowDeath(String source_repository_name) {

        store_path = ApplicationProperties.getStorePath();
        this.record_repository = new RecordRepository(store_path, source_repository_name);
        deaths_bucket = record_repository.getBucket("death_records");

    }

    public void serviceQueries() throws BucketException {
        Scanner in = new Scanner(System.in);
        while (true) {
                System.out.print("Id: ");
                long next_oid = in.nextLong();
                LXP record = deaths_bucket.getObjectById(next_oid);
                show(record);
        }
    }

    private void show(LXP death) {

        System.out.println( "id:\t" + death.getId() );
        System.out.println( "orig_id:\t" + death.get(Death.ORIGINAL_ID) );
        System.out.println( "std_id:\t" + death.get(Death.STANDARDISED_ID ) );
        System.out.println( "firstname:\t" + "[" + death.get(Death.FORENAME) + "]" );
        System.out.println( "surname:\t" + "[" + death.get(Death.SURNAME) + "]");
        System.out.println( "fatherf:\t" + "[" + death.get(Death.FATHER_FORENAME) + "]");
        System.out.println( "fathers:\t" + "[" + death.get(Death.FATHER_SURNAME) + "]");
        System.out.println( "motherf:\t" + "[" + death.get(Death.MOTHER_FORENAME) + "]");
        System.out.println( "mothers:\t" + "[" + death.get(Death.MOTHER_SURNAME) + "]");
        System.out.println( "motherm:\t" + "[" + death.get(Death.MOTHER_MAIDEN_SURNAME) + "]");
        System.out.println( "dob:\t" + death.get(Death.DEATH_DAY) );
        System.out.println( "mob:\t" + death.get(Death.DEATH_MONTH) );
        System.out.println( "yob:\t" + death.get(Death.DEATH_YEAR) );


    }

    public static void main(String[] args) throws BucketException {

        ShowDeath sb = new ShowDeath("Umea");
        sb.serviceQueries();
    }
}
