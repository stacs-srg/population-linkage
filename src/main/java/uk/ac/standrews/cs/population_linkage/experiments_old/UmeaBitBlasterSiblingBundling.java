package uk.ac.standrews.cs.population_linkage.experiments_old;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.nio.file.Path;

public class UmeaBitBlasterSiblingBundling extends BitBlasterSiblingBundling {

    private static final double MATCH_THRESHOLD = 2.03; // from R metric power table [FRobustness2).
    private static final int NUMBER_OF_PROGRESS_UPDATES = 100;

    private UmeaBitBlasterSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        new UmeaBitBlasterSiblingBundling(store_path, repository_name).run();
    }

    @Override
    protected StringMetric getBaseMetric() {

        return Utilities.JACCARD;
    }

    @Override
    protected Metric<LXP> getCompositeMetric() {

        return new Sigma(getBaseMetric(), getMatchFields());
    }

    @Override
    protected double getMatchThreshold() {

        return MATCH_THRESHOLD;
    }

    @Override
    protected int getNumberOfProgressUpdates() {

        return NUMBER_OF_PROGRESS_UPDATES;
    }
}
