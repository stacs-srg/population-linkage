/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.groundTruth.TwoSourcesLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Performs linkage analysis on data from births and deaths.
 * It compares the baby's and parents' names on a birth record with the deceased's and deceased's parents' names on a death record.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby on a birth record and the deceased on a death record.
 */
public class UmeaBirthDeathIdentity extends TwoSourcesLinkageAnalysis {

    UmeaBirthDeathIdentity(String repo_name, int number_of_records_to_be_checked, int number_of_runs) throws IOException {
        super(repo_name, getLinkageResultsFilename(), getDistanceResultsFilename(), number_of_records_to_be_checked, number_of_runs, false);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        return Utilities.getDeathRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return Arrays.asList(
                Death.FORENAME,
                Death.SURNAME,
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public int getIdFieldIndex() {
        return BirthDeathIdentityLinkageRecipe.ID_FIELD_INDEX1;
    }

    @Override
    public int getIdFieldIndex2() {
        return BirthDeathIdentityLinkageRecipe.ID_FIELD_INDEX2;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return BirthDeathIdentityLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink( RecordPair proposedLink) {
        return BirthDeathIdentityLinkageRecipe.isViable(proposedLink);
    }

    @Override
    public String getDatasetName() {
        return "Umea";
    }

    @Override
    public String getLinkageType() {
        return "identity linkage between baby on birth record and deceased on death record";
    }

    @Override
    public String getSourceType() {
        return "births";
    }

    @Override
    protected String getSourceType2() {
        return "deaths";
    }

    public static void main(String[] args) throws Exception {

        String repo_name = "Umea";

        int NUMBER_OF_RUNS = 1;

        new UmeaBirthDeathIdentity(repo_name, DEFAULT_NUMBER_OF_RECORDS_TO_BE_CHECKED, NUMBER_OF_RUNS).run();
    }
}
