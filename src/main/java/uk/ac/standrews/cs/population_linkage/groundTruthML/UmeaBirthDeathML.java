/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.*;

/**
 * This class performs sibling bundling linkage analysis on data from births and deaths
 * It compares the fields listed in getComparisonFields() and getComparisonFields2() the birth and death records:
 * The ground truth is listed in isTrueLink.
 **/

public class UmeaBirthDeathML extends AllPairsTwoSourcesLinkageAnalysisML {

    protected static final int EVERYTHING = Integer.MAX_VALUE;
    public int ALL_LINKAGE_FIELDS = 6;

    private Iterable<LXP> cached_source_records = null;
    private Iterable<LXP> cached_source_records2 = null;

    public UmeaBirthDeathML(String repo_name, final String distance_results_filename) throws IOException {
        super(repo_name, distance_results_filename);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        if (cached_source_records == null) {
            cached_source_records = filter(ALL_LINKAGE_FIELDS, EVERYTHING, Utilities.getBirthRecords(record_repository), getComparisonFields());
        }
        return cached_source_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2(RecordRepository record_repository) {
        if (cached_source_records2 == null) {
            cached_source_records2 = filter(ALL_LINKAGE_FIELDS, EVERYTHING, Utilities.getDeathRecords(record_repository), getComparisonFields());
        }
        return cached_source_records2;
    }

    public static final List<List<LinkageRecipe.Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Death.BIRTH_RECORD_IDENTITY)),
            list(pair(Birth.DEATH_RECORD_IDENTITY, Death.STANDARDISED_ID))
    );

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {
        return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    protected String getSourceType() {
        return "births";
    }

    @Override
    protected String getSourceType2() {
        return "deaths";
    }

    @Override
    protected int getIdFieldIndex2() {
        return Death.STANDARDISED_ID;
    }

    @Override
    public List<Integer> getComparisonFields() {
        return List.of(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME
        );
    }

    @Override
    public List<Integer> getComparisonFields2() {
        return List.of(
                Death.FORENAME,
                Death.SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME,
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME
        );
    }

    public static void main(String[] args) throws Exception {

        new UmeaBirthDeathML(Umea.REPOSITORY_NAME, "UmeaBirthDeathMLDistances").run();
    }
}
