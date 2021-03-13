/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.groundTruth.TwoSourcesLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.ViableLink;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;

/**
 * Performs linkage analysis on data from births and marriages.
 * It compares the baby's and parents' names on a birth record with the bride's and her parents' names on a marriage record.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby on a birth record and the bride on a marriage record.
 * The ground truth is listed in isTrueLink.
 */
public class UmeaBirthBrideIdentity extends TwoSourcesLinkageAnalysis {

    UmeaBirthBrideIdentity(Path store_path, String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(store_path, repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs, false);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return BirthBrideIdentityLinkageRecipe.LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return BirthBrideIdentityLinkageRecipe.SEARCH_FIELDS;
    }

    @Override
    public int getIdFieldIndex() {
        return Birth.STANDARDISED_ID;
    }

    @Override
    public int getIdFieldIndex2() {
        return Marriage.STANDARDISED_ID;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return Evaluation.trueMatch(record1, record2, BirthBrideIdentityLinkageRecipe.TRUE_MATCH_ALTERNATIVES, list());
    }

    @Override
    public boolean isViableLink( RecordPair proposedLink) {
        return ViableLink.spouseBirthIdentityLinkIsViable(proposedLink, true);
    }

    @Override
    public String getDatasetName() {
        return "Umea";
    }

    @Override
    public String getLinkageType() {
        return "identity linkage between baby on birth record and bride on marriage record";
    }

    @Override
    public String getSourceType() {
        return "births";
    }

    @Override
    protected String getSourceType2() {
        return "marriages";
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";
        int NUMBER_OF_RUNS = 1;

        new UmeaBirthBrideIdentity(store_path, repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
