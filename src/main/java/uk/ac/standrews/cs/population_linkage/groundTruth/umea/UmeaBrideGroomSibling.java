/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.groundTruth.AsymmetricSingleSourceLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.SpouseRole;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;

/**
 * Performs linkage analysis on data from marriages.
 * It compares the brides' parents' names on one marriage record with the groom's parents' names on another marriage record.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the bride and groom on two marriage records.
 */
public class UmeaBrideGroomSibling extends AsymmetricSingleSourceLinkageAnalysis {

    UmeaBrideGroomSibling(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs, true);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return BrideGroomSiblingLinkageRecipe.LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return BrideGroomSiblingLinkageRecipe.SEARCH_FIELDS;
    }

    @Override
    public int getIdFieldIndex() {
        return BrideGroomSiblingLinkageRecipe.ID_FIELD_INDEX1;
    }

    @Override
    public int getIdFieldIndex2() {
        return BrideGroomSiblingLinkageRecipe.ID_FIELD_INDEX2;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return Evaluation.trueMatch(record1, record2, BrideGroomSiblingLinkageRecipe.TRUE_MATCH_ALTERNATIVES, list());
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return ViableLink.spouseSpouseSiblingLinkIsViable(proposedLink, SpouseRole.BRIDE, SpouseRole.GROOM);
    }

    @Override
    public String getDatasetName() {
        return "Umea";
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between brides and grooms on marriage records";
    }

    @Override
    public String getSourceType() {
        return "marriages";
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaBrideGroomSibling(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
