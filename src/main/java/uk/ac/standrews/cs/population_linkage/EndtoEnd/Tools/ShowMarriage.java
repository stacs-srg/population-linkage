/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.Tools;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;

import java.nio.file.Path;
import java.util.Scanner;

public class ShowMarriage {

    private final RecordRepository record_repository;
    private final IBucket<Birth> marriages_bucket;
    protected Path store_path;

    public ShowMarriage(String source_repository_name) {

        store_path = ApplicationProperties.getStorePath();
        this.record_repository = new RecordRepository(store_path, source_repository_name);
        marriages_bucket = record_repository.getBucket("marriage_records");

    }

    public void serviceQueries() throws BucketException {
        Scanner in = new Scanner(System.in);
        while (true) {
                System.out.print("Id: ");
                long next_oid = in.nextLong();
                LXP record = marriages_bucket.getObjectById(next_oid);
                show(record);
        }
    }

    private void show(LXP marriage) {

        System.out.println( "id:\t" + marriage.getId() );
        System.out.println( "orig_id:\t" + marriage.get(Marriage.ORIGINAL_ID) );
        System.out.println( "std_id:\t" + marriage.get(Marriage.STANDARDISED_ID ) );
        
        System.out.println( "gfirstname:\t" + "[" + marriage.get(Marriage.GROOM_FORENAME) + "]" );
        System.out.println( "gsurname:\t" + "[" + marriage.get(Marriage.GROOM_SURNAME) + "]");
        System.out.println( "gid:\t" + "[" + marriage.get(Marriage.GROOM_IDENTITY) + "]");

        System.out.println( "gfatherf:\t" + "[" + marriage.get(Marriage.GROOM_FATHER_FORENAME) + "]");
        System.out.println( "gfathers:\t" + "[" + marriage.get(Marriage.GROOM_FATHER_SURNAME) + "]");
        System.out.println( "gfid:\t" + "[" + marriage.get(Marriage.GROOM_FATHER_IDENTITY) + "]");

        System.out.println( "gmotherf:\t" + "[" + marriage.get(Marriage.GROOM_MOTHER_FORENAME) + "]");
        System.out.println( "gmothers:\t" + "[" + marriage.get(Marriage.GROOM_MOTHER_MAIDEN_SURNAME) + "]");
        System.out.println( "gmid:\t" + "[" + marriage.get(Marriage.GROOM_MOTHER_IDENTITY) + "]");

        System.out.println( "bfatherf:\t" + "[" + marriage.get(Marriage.BRIDE_FATHER_FORENAME) + "]");
        System.out.println( "bfathers:\t" + "[" + marriage.get(Marriage.BRIDE_FATHER_SURNAME) + "]");
        System.out.println( "bfid:\t" + "[" + marriage.get(Marriage.BRIDE_FATHER_IDENTITY) + "]");

        System.out.println( "bmotherf:\t" + "[" + marriage.get(Marriage.BRIDE_MOTHER_FORENAME) + "]");
        System.out.println( "bmothers:\t" + "[" + marriage.get(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME) + "]");
        System.out.println( "bmid:\t" + "[" + marriage.get(Marriage.BRIDE_MOTHER_IDENTITY) + "]");
        
        System.out.println( "dob:\t" + marriage.get(Marriage.MARRIAGE_DAY) );
        System.out.println( "mob:\t" + marriage.get(Marriage.MARRIAGE_MONTH) );
        System.out.println( "yob:\t" + marriage.get(Marriage.MARRIAGE_YEAR) );
        System.out.println( "pom:\t" + marriage.get(Marriage.PLACE_OF_MARRIAGE) );

    }

    public static void main(String[] args) throws BucketException {

        ShowMarriage sb = new ShowMarriage("Umea");
        sb.serviceQueries();
    }
}
