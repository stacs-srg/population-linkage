package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class UmeaBitBlasterSiblingBundling extends BitBlasterSiblingBundling {

     private static final List<Integer> SIBLING_GROUND_TRUTH_FIELDS = Collections.singletonList(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

    private UmeaBitBlasterSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected List<Integer> getSiblingGroundTruthFields() {

        return SIBLING_GROUND_TRUTH_FIELDS;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = "umea";

        new UmeaBitBlasterSiblingBundling(store_path, repository_name).run();
    }
}
