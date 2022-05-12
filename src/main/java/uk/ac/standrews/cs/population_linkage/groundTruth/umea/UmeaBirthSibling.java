/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.groundTruth.SymmetricSingleSourceLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.io.IOException;
import java.util.List;

/**
 * Performs linkage analysis on data from births.
 * It compares the parents' names, date and place of marriage on two birth records.
 * The fields used for comparison are listed in getComparisonFields().
 * This is indirect sibling linkage between the babies on two birth records.
 */
public class UmeaBirthSibling extends SymmetricSingleSourceLinkageAnalysis {

    // Cutoff record distance for field distance measures that aren't intrinsically normalised;
    // all distances at or above the cutoff will be normalised to 1.0.
    private static final double NORMALISATION_CUTOFF = 30;

    UmeaBirthSibling(final String repo_name, final String[] args) throws IOException {
        super(repo_name, args, getLinkageResultsFilename(), getDistanceResultsFilename(), true);
    }

    @Override
    public Iterable<LXP> getSourceRecords1(final RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2(final RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return BirthSiblingLinkageRecipe.LINKAGE_FIELDS;
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
        return BirthSiblingLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return BirthSiblingLinkageRecipe.isViable(record1, record2);
    }

    @Override
    public String getDatasetName() {
        return Umea.REPOSITORY_NAME;
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between babies on birth records";
    }

    @Override
    protected boolean recordLinkDistances() {
        return false;
    }

    public static void main(String[] args) throws Exception {

        new UmeaBirthSibling(Umea.REPOSITORY_NAME, args).run();
    }
}
