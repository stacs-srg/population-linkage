package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;

/*----------------------
    Data Set conatins:
        id1
        id2
        is_true_link
        Distances:
 */
public class AnalyseGroundTruthUmea {

    public static final String ID_1 = "id1";
    public static final String ID_2 = "id2";
    private static final String IS_TRUE = "is_true_link";

    private static final String SIGMA_LEV = "SigmaOverLevenshtein";
    private static final String SIGMA_JACC = "SigmaOverJaccard";
    private static final String SIGMA_COS = "SigmaOverCosine";
    private static final String SIGMA_SED = "SigmaOversed";
    private static final String SIGMA_JS = "SigmaOverJensenShannon";
    private static final String SIGMA_JS2 = "SigmaOverJensenShannon2";

    private static final String[] metric_names = new String[]{ SIGMA_LEV, SIGMA_JACC, SIGMA_COS, SIGMA_SED, SIGMA_JS, SIGMA_JS2 };

    private static final String DELIMIT = ",";

    private final DataSet data;
    private final PrintStream outstream;

    public AnalyseGroundTruthUmea(String input_filename, String output_filename) throws Exception {

        data = new DataSet(Paths.get(input_filename));

        if( output_filename.equals("stdout") ) {
            outstream = System.out;
        } else {
            outstream = new PrintStream( output_filename );
        }

    }

    public void run() {

        analyseColumns();

    }

    public static void main(String[] args) throws Exception {

       // new AnalyseGroundTruthUmea("/Users/al/Desktop/UmeaSiblingBundlingDistances.csv","/Users/al/Desktop/UmeaSiblingBundlingMeasures.csv" ).run();
        new AnalyseGroundTruthUmea("/Users/al/Desktop/UmeaDistances.csv","/Users/al/Desktop/UmeaMeasures.csv" ).run();

    }

    //-------------- Private --------------

    private void analyseColumns() {
        outstream.println( "Metric" + DELIMIT + "threshold" + DELIMIT + "precision" + DELIMIT + "recall" + DELIMIT + "f_measure" );
        for( String metric_name : metric_names) {
            analyseColumn(metric_name);
        }
    }

    private void analyseColumn(String metric_name ) {

//        double min = Double.MAX_VALUE;
//        double max = Double.MIN_VALUE;
//
//        for (List<String> record : data.getRecords()) {
//            double distance = Double.parseDouble(data.getValue(record, key));
//            if (distance < min) {
//                min = distance;
//            }
//            if (distance > max) {
//                max = distance;
//            }
//        }

        calculate_truths( metric_name ); // , min, max );
    }

    private void calculate_truths(String metric_name) { // }, double min, double max ) {
        for( double thresh = 0.01; thresh < 1; thresh += 0.01 ) {

            int fp = 0;
            int tp = 0;
            int fn = 0;
            int tn = 0;

            for (List<String> record : data.getRecords()) {
                boolean is_true = Boolean.parseBoolean(data.getValue(record, IS_TRUE));
                double normalised_distance = normalise(Double.parseDouble(data.getValue(record, metric_name))); // , min, max);

                if (normalised_distance <= thresh) {
                    if (is_true) {
                        tp++;
                    } else {
                        fp++;
                    }
                } else {
                    if (is_true) {
                        fn++;
                    } else {
                        tn++;
                    }
                }
            }

            if( tp + fp != 0 ) {

                double precision = ((double) tp) / (tp + fp);
                double recall = ((double) tp) / (tp + fn);
                double f_measure = ( 2 * precision * recall ) / (precision + recall);

                outstream.println(metric_name + DELIMIT + String.format("%.2f",thresh) + DELIMIT + String.format("%.2f",precision) + DELIMIT + String.format("%.2f",recall) + DELIMIT + String.format("%.2f",f_measure));
                outstream.flush();
            } else {
                outstream.println(metric_name + DELIMIT + String.format("%.2f",thresh) + DELIMIT + "----" + DELIMIT + "----" + DELIMIT + "----");
                outstream.flush();
            }
        }


    }

    private double normalise(double distance) {
        return 1d - (1d / (distance + 1d));
    }

    private double normalise(double distance, double min, double max) {
        return (distance - min) / (max - min);
    }
}


