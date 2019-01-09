package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma2;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CreateGroundTruthUmeaBirthDeath {

    private static final int CHARVAL = 512; // value must be big enough to encode values from character set
    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final String DASH = "-";
    private final PrintStream outstream;

    NamedMetric<String>[] base_metrics = new NamedMetric[] { new Levenshtein(), new Jaccard(), new Cosine(), new SED(CHARVAL), new JensenShannon(CHARVAL) };
    NamedMetric<LXP>[] combined_metrics;
    private int[] birth_fields = new int[] { Birth.FORENAME, Birth.SURNAME };
    private int[] death_fields = new int[] { Death.FORENAME, Death.SURNAME };

    /* Umea Birth match fields:
            CHILD_IDENTITY
            MOTHER_IDENTITY
            FATHER_IDENTITY
            BIRTH_RECORD_IDENTITY
            DEATH_RECORD_IDENTITY
            PARENT_MARRIAGE_RECORD_IDENTITY
            FATHER_BIRTH_RECORD_IDENTITY
            MOTHER_BIRTH_RECORD_IDENTITY
            MARRIAGE_RECORD_IDENTITY1
            MARRIAGE_RECORD_IDENTITY2
     */


    public CreateGroundTruthUmeaBirthDeath(Path store_path, String repo_name, String filename) throws Exception {

        this.store_path = store_path;
        this.repo_name = repo_name;

        if( filename.equals("stdout") ) {
            outstream = System.out;
        } else {
            outstream = new PrintStream( filename );
        }

        List<Integer> death_field_list = Arrays.stream(death_fields).boxed().collect(Collectors.toList());
        List<Integer> baby_field_list = Arrays.stream(birth_fields).boxed().collect(Collectors.toList());

        combined_metrics = new NamedMetric[ base_metrics.length]; // sigma for each
        for( int i = 0; i < base_metrics.length; i++ ) {
            combined_metrics[i] = new Sigma2( base_metrics[i],baby_field_list,death_field_list );
        }
    }


    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println("Creating BirthDeath ground truth");
        System.out.println();

        Iterable<Birth> birth_records = record_repository.getBirths();
        Iterable<Death> death_records = record_repository.getDeaths();

        TreeMap<String, List<Birth>> indices = groupRecordsBy(birth_records,Birth.DEATH_RECORD_IDENTITY);                     // a map keyed on DEATH_RECORD_IDENTITY mapping to lists of births of siblings
        TreeMap<String, RecordDistances> true_link_distances = (TreeMap<String, RecordDistances>) populateTrueLinkRecordDistances(indices, death_records);                       // a map from concatenated record id to RecordDistances for all true links
        TreeMap<String, RecordDistances> non_link_distances = populateFalseLinkRecordDistances(birth_records, death_records, true_link_distances);  // a map from concatenated record id to RecordDistances for chosen non links

        printColumnHeaders(outstream);
        printDistances(true_link_distances, true, outstream);
        printDistances(non_link_distances, false, outstream);
    }

    /**
     * @param births - the birth set to process
     * @return a tree map keyed on DEATH_RECORD_IDENTITY mapping to births with some key
     */
    private TreeMap<String, List<Birth>> groupRecordsBy(Iterable<Birth> births, int field_selector ) {

        TreeMap<String, List<Birth>> map = new TreeMap<>();

        for( Birth b : births ) {
            String key = b.getString( field_selector );

            if( ! key.equals( "" ) ) {

                // groups all the full siblings together based on DEATH_RECORD_IDENTITY
                List already = map.get(key);
                if (already == null) {
                    already = new ArrayList<>();
                }
                already.add(b);
                map.put(key, already);
            }
        }

        return map;
    }


    private TreeMap<String,RecordDistances> populateTrueLinkRecordDistances(TreeMap<String, List<Birth>> index, Iterable<Death> death_records) {

        TreeMap<String, RecordDistances> distances = new TreeMap<>();

        for( Death death_record : death_records ) {

            String id = death_record.getString( Death.STANDARDISED_ID );
            List<Birth> births = index.get(id);

            if( births != null ) {

                addDistances(distances, births, death_record );
            }
        }
        return distances;
    }


    /**
     *
     * @param birth_records - the birth records we are processing
     * @param death_records
     * @param true_link_distances - the true links in the dataset
     * @return a tree map of record distances containing false links (with no record pairs exisiting in true_link_distances).
     */
    private TreeMap<String, RecordDistances> populateFalseLinkRecordDistances(Iterable<Birth> birth_records, Iterable<Death> death_records, TreeMap<String, RecordDistances> true_link_distances ) {

        ArrayList<Birth> all_births = new ArrayList<>();
        for( Birth b : birth_records ) {
            all_births.add(b);
        }
        int num_births = all_births.size();
        ArrayList<Death> all_deaths = new ArrayList<>();
        for( Death d : death_records ) {
            all_deaths.add(d);
        }
        int num_deaths = all_deaths.size();

        TreeMap<String, RecordDistances> false_link_distances = new TreeMap<>();

        Random rand = new Random();

        while( false_link_distances.size() < true_link_distances.size() ) {

            Birth b1 = all_births.get( rand.nextInt( num_births ) );
            Death d1 = all_deaths.get( rand.nextInt( num_deaths ) );

            String key = makeKey(b1,d1);
            if( ! true_link_distances.containsKey(key) && ! false_link_distances.containsKey(key) ) {
                // not used for false link already and not a true link
                false_link_distances.put(key,new RecordDistances( b1,d1,computeCombinedMetricDistances( b1, d1, combined_metrics ) ) );
            }
        }
        return false_link_distances;
    }




    /**
     * computes the distances between births and deaths
     * @param distances
     * @param births
     * @param death
     */
    private void addDistances(TreeMap<String, RecordDistances> distances, List<Birth> births, Death death ) {

        Birth[] births_array = births.toArray(new Birth[births.size()]);

        for (int i = 0; i < births_array.length; i++) {
            Birth b1 = births_array[i];

            String key = makeKey(b1, death);
            distances.put(key, new RecordDistances(b1, death, computeCombinedMetricDistances(b1, death, combined_metrics)));

        }
    }

    private String makeKey(Birth b1, Death b2) {
        return Long.toString( b1.getId() ) + Long.toString( b2.getId() );
    }

    private double[] computeCombinedMetricDistances(Birth b1, Death d1, NamedMetric<LXP>[] combined_metrics) {
        double[] distances = new double[ combined_metrics.length ];

        for( int cm_index = 0; cm_index < combined_metrics.length; cm_index++ ) {

            distances[ cm_index ] = combined_metrics[cm_index].distance(b1,d1);
        }
        return distances;
    }

    private void printDistances(Map<String, RecordDistances> distances, boolean is_true_link, PrintStream out) {


        for( RecordDistances r : distances.values() ) {

            out.print( r.record1.getId() + DELIMIT + r.record2.getId() + DELIMIT + is_true_link );
            for(int metric_distance_index = 0; metric_distance_index < r.distances.length; metric_distance_index++ ) {
                out.print( DELIMIT + normalise( r.distances[metric_distance_index]));
            }
            out.println();
            out.flush();
        }
    }

    /**
     *
     * @param distance - the distance to be normalised
     * @return the distance in the range 0-1:  1 - ( 1 / d + 1 )
     */
    private double normalise(double distance) {
        return 1d - ( 1d / ( distance + 1d ));
    }

    private void printColumnHeaders( PrintStream out ) {
        out.print( "id1" + DELIMIT + "id2" + DELIMIT + "is_true_link" + DELIMIT );
        for( int cm_index = 0; cm_index < combined_metrics.length; cm_index++ ) {
            out.print( combined_metrics[cm_index].getMetricName() + DELIMIT );
        }
        out.println();
        out.flush();
    }


    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = ApplicationProperties.getRepositoryName();

        new CreateGroundTruthUmeaBirthDeath( store_path, repo_name, "/Users/al/Desktop/UmeaDistances.csv" ).run();
    }

    private class RecordDistances {
        public final LXP record1;
        public final LXP record2;
        public final double[] distances;

        public RecordDistances(LXP record1, LXP record2, double[] distances) {
            this.record1 = record1;
            this.record2 = record2;
            this.distances = distances;
        }
    }
}
