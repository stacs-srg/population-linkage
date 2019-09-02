package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.Statistics;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.al_richard.metricbitblaster.production.DistanceExponent;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UmeaBirthBirthIDIM {

    private final RecordRepository record_repository;
    private final List<Integer> matchFields10;
    private final List<Integer> matchFields6;
    private final List<Integer> matchFields4;

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    private UmeaBirthBirthIDIM(Path store_path, String repo_name) {

        record_repository = new RecordRepository(store_path, repo_name);

        matchFields10 = Arrays.asList(Birth.FORENAME, Birth.SURNAME, Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME, Birth.PARENTS_YEAR_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE, Birth.PARENTS_DAY_OF_MARRIAGE, Birth.PARENTS_PLACE_OF_MARRIAGE);

        matchFields6 = Arrays.asList(Birth.FORENAME, Birth.SURNAME, Birth.FATHER_FORENAME, Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME);

        matchFields4 = Arrays.asList(Birth.FORENAME, Birth.SURNAME, Birth.FATHER_FORENAME, Birth.FATHER_SURNAME, Birth.MOTHER_FORENAME);
    }

    public void run() throws Exception {

        final Iterable<LXP> birth_records = Utilities.getDeathRecords(record_repository);

        List<LXP> dat = new ArrayList<>();
        birth_records.forEach(dat::add);

        for (StringMetric metric : Constants.BASE_METRICS) {

            printIDIM(dat, metric, matchFields4, "BFN BFS BMF MFS");
            printIDIM(dat, metric, matchFields6, "BN BS BFN BFS BMF MFS");
            printIDIM(dat, metric, matchFields10, "BN BS BFN BFS BMF MFS POM DOM");
        }
    }

    private void printIDIM(List<LXP> dat, StringMetric metric, List<Integer> fields, String field_names) throws Exception {

        final int num_samples = 10;
        final ArrayList<Double> triana_data = new ArrayList<>();
        final ArrayList<Double> chavez_data = new ArrayList<>();
        for (int i = 0; i < num_samples; i++) {
            DistanceExponent<LXP> de = new DistanceExponent<>(getCompositeMetric(metric, fields)::distance, dat);  // each has random pivots
            chavez_data.add(de.chavezIDIM());
            triana_data.add(de.IDIM());
        }

        print_dimensions(metric, triana_data, field_names, "Triana");
        print_dimensions(metric, chavez_data, field_names, "Chavez");
        System.out.println();
    }

    private void print_dimensions(StringMetric metric, ArrayList<Double> data, String field_names, String idim_method) {
        double mean = Statistics.mean(data);
        double ci = Statistics.confidenceInterval(data);
        double min = Collections.min(data);
        double max = Collections.max(data);

        System.out.println(idim_method + ": 95% CI Birth-Birth IDIM over " + field_names + " (" + metric.getMetricName() + ") : " + df2.format(mean - ci) + " , " + df2.format(mean) + " , " + df2.format(mean + ci) + " min/max = " + df2.format(min) + "," + df2.format(max));
    }

    protected Metric<LXP> getCompositeMetric(StringMetric metric, List<Integer> fields) {

        return new Sigma(metric, fields);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        new UmeaBirthBirthIDIM(store_path, repository_name).run();
    }
}
