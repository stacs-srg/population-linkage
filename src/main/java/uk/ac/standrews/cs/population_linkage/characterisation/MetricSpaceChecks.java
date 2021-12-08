/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.characterisation;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.ac.standrews.cs.population_linkage.supportClasses.Constants.TRUE_METRICS;

public class MetricSpaceChecks {

    private static final int DUMP_COUNT_INTERVAL = 1000000;
    private static final long SEED = 34553543456223L;
    private static final double DELTA = 0.0000001;

    private final List<Metric<LXP>> combined_metrics;

    private MetricSpaceChecks() {

        combined_metrics = getCombinedMetrics();
    }

    public void run() throws Exception {

        checkTriangleInequality();
    }

    private List<Metric<LXP>> getCombinedMetrics() {

        List<Metric<LXP>> result = new ArrayList<>();

        for (StringMetric base_metric : TRUE_METRICS) {
            result.add(new Sigma(base_metric, BirthSiblingLinkageRecipe.LINKAGE_FIELDS, 0));
        }
        return result;
    }

    private void checkTriangleInequality() throws IOException {

        Random random = new Random(SEED);
        RecordRepository record_repository = new RecordRepository("umea");

        final List<LXP> birth_records = new ArrayList<>();
        for (Birth birth : record_repository.getBirths()) {
            birth_records.add(birth);
        }

        long counter = 0;

        while (true) {

            int size = birth_records.size();

            LXP b1 = birth_records.get(random.nextInt(size));
            LXP b2 = birth_records.get(random.nextInt(size));
            LXP b3 = birth_records.get(random.nextInt(size));

            for (Metric<LXP> metric : combined_metrics) {

                String metric_name = metric.getMetricName();

                double distance1 = metric.distance(b1, b2);
                double distance2 = metric.distance(b1, b3);
                double distance3 = metric.distance(b2, b3);

                if (violatesTriangleInequality(distance1, distance2, distance3)) {

                    System.out.println("violation of triangle inequality for " + metric_name);
                    System.out.println(b1);
                    System.out.println(b2);
                    System.out.println(b3);
                    System.out.println(distance1);
                    System.out.println(distance2);
                    System.out.println(distance3);
                    System.out.println();
                }
            }

            if (counter % DUMP_COUNT_INTERVAL == 0) {
                System.out.println("checked: " + counter);
            }

            counter++;
        }
    }

    private boolean violatesTriangleInequality(final double distance1, final double distance2, final double distance3) {

        return distance1 > distance2 + distance3 + DELTA || distance2 > distance1 + distance3 + DELTA || distance3 > distance1 + distance2 + DELTA;
    }

    public static void main(String[] args) throws Exception {

        new MetricSpaceChecks().checkTriangleInequality();
    }
}
