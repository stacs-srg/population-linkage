package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.metrics.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SkyeMTreeSiblingBundling extends MTreeSiblingBundling {

    private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.FAMILY);
    private static final double MATCH_THRESHOLD = 4.0;
    private static final int NUMBER_OF_PROGRESS_UPDATES = 100;

    private SkyeMTreeSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    @Override
    protected NamedMetric<String> getBaseMetric() {

        return Utilities.JENSEN_SHANNON2;
    }

    @Override
    protected NamedMetric<LXP> getCompositeMetric() {

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

    @Override
    protected List<Integer> getSiblingGroundTruthFields() {

        return SIBLING_GROUND_TRUTH_FIELDS;
    }

    public static void main(String[] args) throws Exception {

        final Path store_path = ApplicationProperties.getStorePath();
        final String repository_name = "skye";

        new SkyeMTreeSiblingBundling(store_path, repository_name).run();
    }
}
