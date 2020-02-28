package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;

import java.io.IOException;
import java.nio.file.Path;

/* Performs linkage analysis on data from marriages.
 * It compares the brides' parents' names on one marriage record with the groom's parents' names on another marriage record.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the bride and groom on two marriage records.
 * The ground truth is listed in isTrueLink.
 **/
public class UmeaGroomBrideSiblingViability extends UmeaGroomBrideSibling {

    private UmeaGroomBrideSiblingViability(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, number_of_records_to_be_checked, number_of_runs);
    }

    public boolean isViableLink(RecordPair proposedLink) {
        return GroomBrideSiblingLinkageRecipe.isViable(proposedLink);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaGroomBrideSiblingViability(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
