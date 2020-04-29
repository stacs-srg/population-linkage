/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UmeaNameDistribution {

    private final Path store_path;
    private final String repo_name;
    private final String output_path;

    Map<String,Integer> firstnames = new HashMap<>();
    Map<String,Integer> surnames = new HashMap<>();
    Map<String,Integer> combined_names = new HashMap<>();

    long birth_record_count = 0;
    long death_record_count = 0;
    long marriage_record_count = 0;
    long total_role_count = 0;

    public UmeaNameDistribution(Path store_path, String repo_name, String output_path) {

        this.store_path = store_path;
        this.repo_name = repo_name;
        this.output_path = output_path;
    }


    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        Iterable<Birth> births = record_repository.getBirths();
        Iterable<Death> deaths = record_repository.getDeaths();
        Iterable<Marriage> marriages = record_repository.getMarriages();

        collectNames( births,deaths,marriages );

        printCounts();
    }

    private void collectNames(Iterable<Birth> births, Iterable<Death> deaths, Iterable<Marriage> marriages) {
        collectBirthNames( births );
        collectDeathNames( deaths );
        collectMarriageNames( marriages );
    }

    private void collectBirthNames(Iterable<Birth> births) {
        for( Birth b : births ) {
            birth_record_count = birth_record_count + 1;
            String baby_firstname = b.getString( Birth.FORENAME );
            String baby_surname = b.getString( Birth.SURNAME );
            addToFirstNames( baby_firstname ); addToSurnames( baby_surname ); addToCombinedNames( baby_firstname,baby_surname );
            String father_firstname = b.getString( Birth.FATHER_FORENAME );
            String father_surname = b.getString( Birth.FATHER_SURNAME );
            addToFirstNames( father_firstname ); addToSurnames( father_surname ); addToCombinedNames( father_firstname,father_surname );
            String mother_firstname = b.getString( Birth.MOTHER_FORENAME );
            String mother_surname = b.getString( Birth.MOTHER_SURNAME );
            addToFirstNames( mother_firstname ); addToSurnames( mother_surname ); addToCombinedNames( mother_firstname,mother_surname );
        }
    }

    private void collectDeathNames(Iterable<Death> deaths) {
        for( Death d : deaths ) {
            death_record_count = death_record_count + 1;
            String deceased_firstname = d.getString( Death.FORENAME );
            String deceased_surname = d.getString( Death.SURNAME );
            addToFirstNames( deceased_firstname ); addToSurnames( deceased_surname ); addToCombinedNames( deceased_firstname,deceased_surname );
            String father_firstname = d.getString( Death.FATHER_FORENAME );
            String father_surname = d.getString( Death.FATHER_SURNAME );
            addToFirstNames( father_firstname ); addToSurnames( father_surname ); addToCombinedNames( father_firstname,father_surname );
            String mother_firstname = d.getString( Death.MOTHER_FORENAME );
            String mother_surname = d.getString( Death.MOTHER_SURNAME );
            addToFirstNames( mother_firstname ); addToSurnames( mother_surname ); addToCombinedNames( mother_firstname,mother_surname );
        }
    }

    private void collectMarriageNames(Iterable<Marriage> marriages) {
        for( Marriage m : marriages ) {
            marriage_record_count = marriage_record_count + 1;
            String groom_firstname = m.getString( Marriage.GROOM_FORENAME );
            String groom_surname = m.getString( Marriage.GROOM_SURNAME );
            addToFirstNames( groom_firstname ); addToSurnames( groom_surname ); addToCombinedNames( groom_firstname,groom_surname );
            String groom_father_firstname = m.getString( Marriage.GROOM_FATHER_FORENAME );
            String groom_father_surname = m.getString( Marriage.GROOM_FATHER_SURNAME );
            addToFirstNames( groom_father_firstname ); addToSurnames( groom_father_surname ); addToCombinedNames( groom_father_firstname,groom_father_surname );
            String groom_mother_firstname = m.getString( Marriage.GROOM_MOTHER_FORENAME );
            String groom_mother_surname = m.getString( Marriage.GROOM_MOTHER_MAIDEN_SURNAME );
            addToFirstNames( groom_mother_firstname ); addToSurnames( groom_mother_surname ); addToCombinedNames( groom_mother_firstname,groom_mother_surname );

            String bride_firstname = m.getString( Marriage.BRIDE_FORENAME );
            String bride_surname = m.getString( Marriage.BRIDE_SURNAME );
            addToFirstNames( bride_firstname ); addToSurnames( bride_surname ); addToCombinedNames( bride_firstname,bride_surname );
            String bride_father_firstname = m.getString( Marriage.BRIDE_FATHER_FORENAME );
            String bride_father_surname = m.getString( Marriage.BRIDE_FATHER_SURNAME );
            addToFirstNames( bride_father_firstname ); addToSurnames( bride_father_firstname ); addToCombinedNames( bride_father_firstname,bride_father_surname );
            String bride_mother_firstname = m.getString( Marriage.BRIDE_MOTHER_FORENAME );
            String bride_mother_surname = m.getString( Marriage.BRIDE_MOTHER_MAIDEN_SURNAME );
            addToFirstNames( bride_mother_firstname ); addToSurnames( bride_mother_surname ); addToCombinedNames( bride_mother_firstname,bride_mother_surname );
        }
    }

    private void addToFirstNames(String firstname) {
        if( addToNames( firstnames,firstname ) ) {
            total_role_count = total_role_count + 1;
        }
    }

    private void addToSurnames(String surname) {
        addToNames( surnames,surname );
    }

    private void addToCombinedNames(String firstname, String surname) {
        addToNames( combined_names, firstname + " " + surname );
    }

    private boolean addToNames( Map<String,Integer> map, String key ) {
        if( key != null && ! key.equals("") ) {
            Integer count = map.get(key);
            if (count == null) {
                map.put(key, 1);
            } else {
                map.put(key, count + 1);
            }
            return true;
        }
        return false;
    }

    private void printCounts() throws Exception {

        System.out.println( "Number of birth records: " + birth_record_count );
        System.out.println( "Number of death records: " + death_record_count );
        System.out.println( "Number of marriage records: " + marriage_record_count );

        System.out.println();

        System.out.println( "Number of roles: " + total_role_count  );

        System.out.println();

        System.out.println( "Number of unique firstnames: " + firstnames.entrySet().size() );
        System.out.println( "Number of unique surnames: " + surnames.entrySet().size() );
        System.out.println( "Number of unique combined names: " + combined_names.entrySet().size() );

        System.out.println();

        printStats( firstnames,"firstnames:" );
        printStats( surnames, "surnames:" );
        printStats( combined_names, "combined names:" );

        createFileTable( firstnames,"firstnames.csv" );
        createFileTable( surnames, "surnames.csv" );
        createFileTable( combined_names, "combinednames.csv" );

        System.out.println();

        System.out.println("Complete");
    }

    public static void printStats( Map<String,Integer> counts, String title ) {

        Collection<Integer> values = counts.values();
        
        double mean = mean( values );
        double sd = stddev( values, mean );
        double idim = idim( mean,sd );
        double median = median( values );
        System.out.println( title );
        System.out.println( "min = " + min( values) + " max = " + max( values) + " mean = " + mean + " median = " + median + " sd = " + sd + " idim = " + idim );
        System.out.println();
    }

    private static double median(Collection<Integer> values) {
        ArrayList<Integer> l = new ArrayList<>(values);
        return l.get( l.size() / 2 );
    }

    private static double idim(double mean, double sd) {
        return Math.sqrt( Math.pow( mean,2 ) / ( 2 * (Math.pow(sd,2) ) ) ) ;
    }

    public static int max( Collection<Integer> arrai ) {
        int result = Integer.MIN_VALUE;
        for( int i : arrai ) {
            if( i > result ) {
                result = i;
            }
        }
        return result;
    }

    public static int min( Collection<Integer>  values ) {
        int result = Integer.MAX_VALUE;
        for( int i : values ) {
            if( i < result ) {
                result = i;
            }
        }
        return result;
    }

    public static double mean( Collection<Integer>  values ) {
        int result = 0;
        for( int i : values ) {
            result = result + i;
        }
        return ((double) result) / values.size();
    }

    public static double stddev( Collection<Integer> values, double mean ) {
        double sd = 0;
        for( int i : values ) {
            sd += Math.pow(i - mean, 2);
        }
        return Math.sqrt( sd / values.size() );
    }

    public void createFileTable( Map<String,Integer> counts, String filename ) throws Exception {

        PrintStream stream = new PrintStream(Files.newOutputStream( Paths.get(output_path, filename)));

        stream.println( "name\tcount" );
        for( Map.Entry<String,Integer> entry : counts.entrySet() ) {
            stream.println( entry.getKey() + "\t" + entry.getValue() );
        }
        stream.close();

        System.out.println("Output " + counts.values().size() + " map records to " + filename);
    }
    
    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new UmeaNameDistribution( store_path,repo_name,"/Users/al/Desktop/" ).run();
    }
}
