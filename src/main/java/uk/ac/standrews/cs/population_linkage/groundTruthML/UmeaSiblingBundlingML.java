/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruthML;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This class performs sibling bundling linkage analysis on data from births.
 * It compares two sets of parental marriage information from two birth certificates
 * The fields used for comparison are listed in getComparisonFields().
 * The ground truth is listed in isTrueLink.
 **/

public class UmeaSiblingBundlingML extends AllPairsSameSourceLinkageAnalysisML {

    protected static final int EVERYTHING = Integer.MAX_VALUE;
    public int ALL_LINKAGE_FIELDS = 8;

    public UmeaSiblingBundlingML(Path store_path, String repo_name, final String distance_results_filename) throws IOException {
        super(store_path,repo_name, distance_results_filename);
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        // return Utilities.getBirthRecords( record_repository );
        return filter(ALL_LINKAGE_FIELDS, EVERYTHING, Utilities.getBirthRecords( record_repository ), getComparisonFields());
    }

    @Override
    protected LinkStatus isTrueLink(LXP record1, LXP record2) {

        final String b1_parent_id = record1.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);
        final String b2_parent_id = record2.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY);

        if (b1_parent_id.isEmpty() || b2_parent_id.isEmpty()) return LinkStatus.UNKNOWN;

        return b1_parent_id.equals(b2_parent_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    protected String getSourceType() {
        return "births";
    }

    @Override
    public List<Integer> getComparisonFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.PARENTS_PLACE_OF_MARRIAGE,
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE);
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "Umea";

        new UmeaSiblingBundlingML(store_path, repo_name, "UmeaSiblingBundlingMLDistances").run();
    }


}
