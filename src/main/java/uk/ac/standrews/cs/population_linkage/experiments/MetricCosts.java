package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MetricCosts {

    private final Path store_path;
    protected final String repo_name;

    private MetricCosts(Path store_path, String repo_name) {

        this.store_path = store_path;
        this.repo_name = repo_name;
    }

    public void run() {

        final RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        final List<LXP> birth_records = Utilities.permute(Utilities.getBirthRecords(record_repository)).subList(0, 1000);

        for (NamedMetric<String> metric : Utilities.BASE_METRICS) {

            calculateAllDistances(birth_records, new Sigma(metric, Utilities.SIBLING_BUNDLING_BIRTH_MATCH_FIELDS));
        }
    }

    private void calculateAllDistances(final List<LXP> birth_records, NamedMetric<LXP> metric) {

        LocalDateTime start = LocalDateTime.now();

        for (LXP record1 : birth_records) {
            for (LXP record2: birth_records) {
                double distance = metric.distance(record1, record2);
            }
        }

        System.out.println("elapsed for " + metric.getMetricName() + ": " + Duration.between(start, LocalDateTime.now()));
    }

    public static void main(String[] args) {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        new MetricCosts(store_path, repository_name).run();
    }
}
