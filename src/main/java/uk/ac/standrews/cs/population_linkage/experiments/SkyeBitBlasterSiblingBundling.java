package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SkyeBitBlasterSiblingBundling extends BitBlasterSiblingBundling {

    private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.FAMILY);

    private SkyeBitBlasterSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected List<Integer> getSiblingGroundTruthFields() {

        return SIBLING_GROUND_TRUTH_FIELDS;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "skye";

        new SkyeBitBlasterSiblingBundling(store_path, repository_name).run();
    }
}
