/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.io.IOException;
import java.nio.file.Path;

/**
 **
 **/
public class UmeaMarriageSiblingExamineRecordsAlExperiment extends UmeaBirthSiblingExamineRecordsAlExperiment {

    UmeaMarriageSiblingExamineRecordsAlExperiment() throws IOException {
        super();
    }

    @Override
    public Iterable<LXP> getSourceRecords(RecordRepository record_repository) {
        System.out.println( "umea Marriages" );
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    protected void run() {
        for( LXP record : records ) {

            String groom_father_surname = record.getString(Marriage.GROOM_FATHER_SURNAME);
            String groom_father_forname = record.getString(Marriage.GROOM_FATHER_FORENAME);
            String groom_mother_surname = record.getString(Marriage.GROOM_MOTHER_MAIDEN_SURNAME);
            String groom_mother_forname = record.getString(Marriage.GROOM_FATHER_FORENAME);

            String bride_father_surname = record.getString(Marriage.BRIDE_FATHER_SURNAME);
            String bride_father_forname = record.getString(Marriage.BRIDE_FATHER_FORENAME);
            String bride_mother_surname = record.getString(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME);
            String bride_mother_forname = record.getString(Marriage.BRIDE_FATHER_FORENAME);

            addToCounts( groom_father_surname,groom_father_forname,groom_mother_surname,groom_mother_forname );
            addToCounts( bride_father_surname,bride_father_forname,bride_mother_surname,bride_mother_forname );

            if( bothGroomsParentsKnown(record)) {
                parents_known_counter++;
                addToMap(combined_both_known_parental_map, groom_father_forname + groom_father_surname + groom_mother_forname + groom_mother_surname );
            }
            if( bothBridesParentsKnown(record)) {
                parents_known_counter++;
                addToMap(combined_both_known_parental_map, bride_father_forname + bride_father_surname + bride_mother_forname + bride_mother_surname );
            }
            record_counter++;
        }
        printAnalysis();
    }

    @Override
    protected void printAnalysis() {
        super.printAnalysis();
        System.out.println( "Number of unique combined (Mother and Father) names = " + combined_parental_map.keySet().size() );
        System.out.println( "Number of unique combined (Mother and Father) names (where both known) = " + combined_both_known_parental_map.keySet().size() );
    }

    public static boolean bothBridesParentsKnown(LXP record) {

        final String b1_mother_id = record.getString(Marriage.BRIDE_MOTHER_IDENTITY);
        final String b1_father_id = record.getString(Marriage.BRIDE_FATHER_IDENTITY);

        return !b1_mother_id.isEmpty() && b1_father_id.isEmpty();
    }

    public static boolean bothGroomsParentsKnown(LXP record) {

        final String b1_mother_id = record.getString(Marriage.GROOM_MOTHER_IDENTITY);
        final String b1_father_id = record.getString(Marriage.GROOM_FATHER_IDENTITY);

        return !b1_mother_id.isEmpty() && b1_father_id.isEmpty();
    }

    @Override
    protected void addToCounts(String father_surname, String father_forname, String mother_surname, String mother_forname) {
        super.addToCounts(father_surname, father_forname, mother_surname, mother_forname);
        addToMap(combined_parental_map, father_forname + father_surname + mother_forname + mother_surname );
    }


    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repo_name = "umea";

        new UmeaMarriageSiblingExamineRecordsAlExperiment().runAll();
    }
}
