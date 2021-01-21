/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;

import java.nio.file.Path;
import java.util.Scanner;

public class Showbirth {

    private final RecordRepository record_repository;
    private final IBucket<Birth> births_bucket;
    protected Path store_path;

    public Showbirth(String source_repository_name) {

        store_path = ApplicationProperties.getStorePath();
        this.record_repository = new RecordRepository(store_path, source_repository_name);
        births_bucket = record_repository.getBucket("birth_records");

    }

    public void serviceQueries() throws BucketException {
        Scanner in = new Scanner(System.in);
        while (true) {
                System.out.print("Id: ");
                long next_oid = in.nextLong();
                LXP record = births_bucket.getObjectById(next_oid);
                show(record);
        }
    }

    private void show(LXP birth) {

        System.out.println( "id:\t" + birth.getId() );
        System.out.println( "fam_id:\t" + birth.get(Birth.FAMILY) );
        System.out.println( "orig_id:\t" + birth.get(Birth.ORIGINAL_ID) );
        System.out.println( "std_id:\t" + birth.get(Birth.STANDARDISED_ID ) );
        System.out.println( "firstname:\t" + "[" + birth.get(Birth.FORENAME) + "]" );
        System.out.println( "surname:\t" + "[" + birth.get(Birth.SURNAME) + "]");
        System.out.println( "fatherf:\t" + "[" + birth.get(Birth.FATHER_FORENAME) + "]");
        System.out.println( "fathers:\t" + "[" + birth.get(Birth.FATHER_SURNAME) + "]");
        System.out.println( "fid:\t" + birth.get(Birth.FATHER_BIRTH_RECORD_IDENTITY) );
        System.out.println( "motherf:\t" + "[" + birth.get(Birth.MOTHER_FORENAME) + "]");
        System.out.println( "mothers:\t" + "[" + birth.get(Birth.MOTHER_SURNAME) + "]");
        System.out.println( "motherm:\t" + "[" + birth.get(Birth.MOTHER_MAIDEN_SURNAME) + "]");
        System.out.println( "mid:\t" + birth.get(Birth.MOTHER_BIRTH_RECORD_IDENTITY) );
        System.out.println( "dob:\t" + birth.get(Birth.BIRTH_DAY) );
        System.out.println( "mob:\t" + birth.get(Birth.BIRTH_MONTH) );
        System.out.println( "yob:\t" + birth.get(Birth.BIRTH_YEAR) );
        System.out.println( "dom:\t" + birth.get(Birth.PARENTS_DAY_OF_MARRIAGE) );
        System.out.println( "mom:\t" + birth.get(Birth.PARENTS_MONTH_OF_MARRIAGE) );
        System.out.println( "yom:\t" + birth.get(Birth.PARENTS_YEAR_OF_MARRIAGE) );
        System.out.println( "pom:\t" + birth.get(Birth.PARENTS_PLACE_OF_MARRIAGE) );
        System.out.println( "pob:\t" + birth.get(Birth.PLACE_OF_BIRTH) );

    }

    public static void main(String[] args) throws BucketException {

        Showbirth sb = new Showbirth("Umea");
        sb.serviceQueries();
    }
}
