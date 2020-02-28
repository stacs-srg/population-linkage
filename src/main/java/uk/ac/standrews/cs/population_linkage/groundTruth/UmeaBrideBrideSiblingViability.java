package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;

import java.io.IOException;
import java.nio.file.Path;

/* Performs linkage analysis on data from marriages.
 * It compares the brides' parents' names on two marriage records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the brides on two marriage records.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaBrideBrideSiblingViability extends UmeaBrideBrideSibling {

    private UmeaBrideBrideSiblingViability(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, number_of_records_to_be_checked, number_of_runs);
    }

    public boolean isViableLink( RecordPair proposedLink) {
        return BrideBrideSiblingLinkageRecipe.isViable(proposedLink);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaBrideBrideSiblingViability(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
