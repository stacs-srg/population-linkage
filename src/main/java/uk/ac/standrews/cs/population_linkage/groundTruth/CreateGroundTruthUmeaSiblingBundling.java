package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.*;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CreateGroundTruthUmeaSiblingBundling {

    private static final int CHARVAL = 512;
    private final Path store_path;
    private final String repo_name;

    private final String DELIMIT = ",";
    private final String DASH = "-";
    private final PrintStream outstream;

    NamedMetric<String>[] base_metrics = new NamedMetric[] { new Levenshtein(), new Jaccard(), new Cosine(), new SED(CHARVAL), new JensenShannon(), new JensenShannon2(CHARVAL) };
    NamedMetric<LXP>[] combined_metrics;
    private int[] sibling_bundling_fields = new int[] { Birth.FATHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_FORENAME, Birth.MOTHER_SURNAME,
                                                        Birth.PARENTS_PLACE_OF_MARRIAGE, Birth.PARENTS_DAY_OF_MARRIAGE, Birth.PARENTS_MONTH_OF_MARRIAGE, Birth.PARENTS_YEAR_OF_MARRIAGE };

    public CreateGroundTruthUmeaSiblingBundling(Path store_path, String repo_name, String filename) throws Exception {

        this.store_path = store_path;
        this.repo_name = repo_name;

        if( filename.equals("stdout") ) {
            outstream = System.out;
        } else {
            outstream = new PrintStream( filename );
        }

        List<Integer> sibling_field_list = Arrays.stream(sibling_bundling_fields).boxed().collect(Collectors.toList());

        combined_metrics = new NamedMetric[ base_metrics.length]; // sigma for each
        for( int i = 0; i < base_metrics.length; i++ ) {
            combined_metrics[i] = new Sigma( base_metrics[i],sibling_field_list );
        }
    }


    public void run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Reading records from repository: " + repo_name);
        System.out.println("Creating Sibling Bundling ground truth" );
        System.out.println();

        ArrayList<Birth> all_records = createList( record_repository.getBirths() );

        TreeMap<String, List<Birth>> real_sibling_bundles = matchSiblingsUsingMarriageGroundTruth(all_records);                     // a map keyed on PARENT_MARRIAGE_RECORD_IDENTITY mapping to lists of births of siblings
        TreeMap<String, RecordDistances> true_link_distances = populateTrueLinkRecordDistances( real_sibling_bundles );             // a map from concatenated record id to RecordDistances for all true links

        TreeMap<String, RecordDistances> non_link_distances = populateFalseLinkRecordDistances( all_records, true_link_distances ); // a map from concatenated record id to RecordDistances for chosen non links


        printColumnHeaders( outstream );
        printDistances( true_link_distances, true, outstream );
        printDistances( non_link_distances, false, outstream );

    }

    private ArrayList<Birth> createList(Iterable<Birth> births) {

        ArrayList<Birth> birth_records = new ArrayList<>();
        for( Birth b : births ) {
            String key = b.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
            birth_records.add(b);
        }
        return birth_records;
    }

    /**
     *
     * @param all_records - the birth records we are processing
     * @param true_link_distances - the true links in the dataset
     * @param all_records - the records being processed
     * @return a tree map of record distances containing false links (with no record pairs exisiting in true_link_distances).
     */
    private TreeMap<String, RecordDistances> populateFalseLinkRecordDistances(ArrayList<Birth> all_records, TreeMap<String, RecordDistances> true_link_distances ) {

        int num_records = all_records.size();
        TreeMap<String, RecordDistances> false_link_distances = new TreeMap<>();

        Random rand = new Random();

        while( false_link_distances.size() < true_link_distances.size() ) {

            Birth b1 = all_records.get( rand.nextInt( num_records ) );
            Birth b2 = all_records.get( rand.nextInt( num_records ) );

            String key = makeKey(b1,b2);
            if( ! true_link_distances.containsKey(key) && ! false_link_distances.containsKey(key) ) {
                // not used for false link already and not a true link
                false_link_distances.put(key,new RecordDistances( b1,b2,computeCombinedMetricDistances( b1, b2, combined_metrics ) ) );
            }
        }
        return false_link_distances;
    }

    /**
     * @param births - the birth set to process
     * @return a tree map keyed on PARENT_MARRIAGE_RECORD_IDENTITY field contents - mapping to lists of births of siblings
     */
    private TreeMap<String, List<Birth>> matchSiblingsUsingMarriageGroundTruth(Iterable<Birth> births) {

        TreeMap<String, List<Birth>> map = new TreeMap<>();

        for( Birth birth_record : births ) {
            String key = birth_record.getString( Birth.PARENT_MARRIAGE_RECORD_IDENTITY );

            if( ! key.equals( "" ) ) {

                // groups all the full siblings together based on PARENT_MARRIAGE_RECORD_IDENTITY
                List already = map.get(key);
                if (already == null) {
                    already = new ArrayList<>();
                }
                already.add(birth_record);
                map.put(key, already);
            }
        }


        return map;
    }

    private TreeMap<String,RecordDistances> populateTrueLinkRecordDistances(TreeMap<String, List<Birth>> birth_map) {

        int count = 0;

        TreeMap<String,RecordDistances> distances = new TreeMap<>();

        for( Map.Entry<String,List<Birth>> map_entry : birth_map.entrySet() ) {

            List<Birth> birth_list = map_entry.getValue();
            if (birth_list.size() > 1) {
                for (Birth b : birth_list) {

                    addDistances( distances, birth_list );
                    count = count + birth_list.size();
                }
            }
        }
        System.out.println( "Number of links = " + count );
        return distances;
    }

    /**
     * computes the distances between all the births in a list of siblings
     * @param distances
     * @param births
     */
    private void addDistances(TreeMap<String, RecordDistances> distances, List<Birth> births ) {

        Birth[] births_array = births.toArray(new Birth[births.size()]);

        for( int i = 0; i < births_array.length - 1; i++ ) {
            Birth b1 = births_array[i];
            for( int j = i+1; j < births_array.length; j++ ) {
                Birth b2 = births_array[j];
                String key = makeKey(b1, b2);
                distances.put( key, new RecordDistances( b1,b2,computeCombinedMetricDistances( b1, b2, combined_metrics ) ) );

            }
        }
    }

    private String makeKey(Birth b1, Birth b2) {
        return Long.toString( b1.getId() ) + Long.toString( b2.getId() );
    }

    private double[] computeCombinedMetricDistances(Birth b1, Birth b2, NamedMetric<LXP>[] combined_metrics) {
        double[] distances = new double[ combined_metrics.length ];

        for( int cm_index = 0; cm_index < combined_metrics.length; cm_index++ ) {

            distances[ cm_index ] = combined_metrics[cm_index].distance(b1,b2);
        }
        return distances;
    }

    private void printFullSiblingBundles(TreeMap<String, List<Birth>> birth_map) {

        for( Map.Entry<String,List<Birth>> map_entry : birth_map.entrySet() ) {
            List<Birth> birth_list = map_entry.getValue();
            if( birth_list.size() > 1 ) {
                System.out.println( map_entry.getKey() );
                for (Birth b : birth_list) {
                    System.out.println(b.toString());
                }
            }
        }
    }

    private void printAbridgedSiblingBundles(TreeMap<String, List<Birth>> birth_map) {

        for( Map.Entry<String,List<Birth>> map_entry : birth_map.entrySet() ) {

            List<Birth> birth_list = map_entry.getValue();
            if( birth_list.size() > 1 ) {
                for (Birth b : birth_list) {
                    System.out.println(
                            "id:" + b.getId() + DELIMIT +
                            "name:" + b.getString( Birth.FORENAME ) + DELIMIT + b.getString( Birth.SURNAME ) + DELIMIT +
                            "mother:" +  b.getString( Birth.MOTHER_FORENAME ) + b.getString( Birth.MOTHER_SURNAME ) + DELIMIT +
                            "father:" + b.getString( Birth.FATHER_FORENAME ) + b.getString( Birth.FATHER_SURNAME ) + DELIMIT +
                            "DoM:" + b.getString( Birth.PARENTS_DAY_OF_MARRIAGE ) + DASH + b.getString( Birth.PARENTS_YEAR_OF_MARRIAGE ) + DELIMIT +
                            ((b.getString( Birth.PARENTS_PLACE_OF_MARRIAGE ) ).equals( "----" ) ? "" : "PoM" + DELIMIT + b.getString( Birth.PARENTS_PLACE_OF_MARRIAGE ) ) +
                            "parent marriage id:" + DELIMIT + map_entry.getKey()
                    );

                }
                System.out.println();
            }
        }
    }

    private void printDistances(Map<String, RecordDistances> distances, boolean is_true_link, PrintStream out) {


        for( RecordDistances r : distances.values() ) {

            out.print( r.record1.getId() + DELIMIT + r.record2.getId() + DELIMIT + is_true_link );
            for( int cm_index = 0; cm_index < r.distances.length; cm_index++ ) {
                out.print( DELIMIT + normalise( r.distances[cm_index]));
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

        new CreateGroundTruthUmeaSiblingBundling( store_path, repo_name, "/Users/al/Desktop/UmeaDistances.csv" ).run();
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
