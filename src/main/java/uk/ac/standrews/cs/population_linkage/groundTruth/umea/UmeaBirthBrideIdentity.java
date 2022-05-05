/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.groundTruth.TwoSourcesLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.io.IOException;
import java.util.List;

/**
 * Performs linkage analysis on data from births and marriages.
 * It compares the baby's and parents' names on a birth record with the bride's and her parents' names on a marriage record.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby on a birth record and the bride on a marriage record.
 * The ground truth is listed in isTrueLink.
 */
public class UmeaBirthBrideIdentity extends TwoSourcesLinkageAnalysis {

    // Cutoff record distance for field distance measures that aren't intrinsically normalised;
    // all distances at or above the cutoff will be normalised to 1.0.
    private static final double NORMALISATION_CUTOFF = 30;

    UmeaBirthBrideIdentity(final String repo_name, final String[] args) throws IOException {
        super(repo_name, args, getLinkageResultsFilename(), getDistanceResultsFilename(), false);
    }

    @Override
    public Iterable<LXP> getSourceRecords(final RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2(final RecordRepository record_repository) {
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
    protected double getNormalisationCutoff() {
        return NORMALISATION_CUTOFF;
    }

    @Override
    public LinkStatus isTrueMatch(final LXP record1, final LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(final LXP record1, final LXP record2) {
        return BirthBrideIdentityLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink(final RecordPair proposed_link) {
        return BirthBrideIdentityLinkageRecipe.isViable(proposed_link);
    }

    @Override
    public String getDatasetName() {
        return Umea.REPOSITORY_NAME;
    }

    @Override
    public String getLinkageType() {
        return "identity linkage between baby on birth record and bride on marriage record";
    }

    public static void main(String[] args) throws Exception {

        new UmeaBirthBrideIdentity(Umea.REPOSITORY_NAME, args).run();
    }
}
