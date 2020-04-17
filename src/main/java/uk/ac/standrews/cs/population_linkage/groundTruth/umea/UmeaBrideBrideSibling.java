package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.groundTruth.SymmetricSingleSourceLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Performs linkage analysis on data from marriages.
 * It compares the brides' parents' names on two marriage records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the brides on two marriage records.
 */
public class UmeaBrideBrideSibling extends SymmetricSingleSourceLinkageAnalysis {

    UmeaBrideBrideSibling(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return BrideBrideSiblingLinkageRecipe.COMPARISON_FIELDS;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return BrideBrideSiblingLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink( RecordPair proposedLink) {
        return BrideBrideSiblingLinkageRecipe.isViable(proposedLink);
    }

    @Override
    public String getDatasetName() {
        return "Umea";
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between brides on marriage records";
    }

    @Override
    public String getSourceType() {
        return "marriages";
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaBrideBrideSibling(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
