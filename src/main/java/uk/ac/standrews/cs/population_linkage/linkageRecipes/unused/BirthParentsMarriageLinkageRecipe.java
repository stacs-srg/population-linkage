/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.unused;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

import java.util.*;

// This class is a confusion to me (Tom)
public class BirthParentsMarriageLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthParentsMarriageLinkageRecipe(sourceRepo, resultsRepo,
                LINKAGE_TYPE + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048),
                        0.67, true, 5, false, false, true, false
                );
    }

    public static final String LINKAGE_TYPE = "birth-parents-marriage-identity";

    public BirthParentsMarriageLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_MOTHER; //return Birth.ROLE_PARENTS;
    } // mother and father

    @Override
    public String getSearchRole() {
        return Marriage.ROLE_BRIDES_MOTHER;  // return Marriage.ROLE_PARENTS;  // bride and groom
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return true;
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME,
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.PLACE_OF_MARRIAGE,
            Marriage.MARRIAGE_DAY,
            Marriage.MARRIAGE_MONTH,
            Marriage.MARRIAGE_YEAR
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        for (LXP marriage_record : record_repository.getMarriages()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage_record );

            for (LXP birth_record : birth_records) {

                String birth_key_from_marriage = toKeyFromBirth( birth_record );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
                    try {
                        Link l = new Link(marriage_record, Marriage.ROLE_BRIDES_MOTHER, birth_record, Birth.ROLE_MOTHER, 1.0f, "ground truth", -1);
                        // Link l = new Link(marriage_record, Marriage.ROLE_PARENTS, birth_record, Birth.ROLE_PARENTS, 1.0f, "ground truth");
                        links.put(l.toString(), l);
                    } catch (PersistentObjectException e) {
                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                    }
                }
            }
        }

        return links;
    }

    private static String toKeyFromBirth(LXP birth_record) {    // TODO check all of these! easy to get wrong.
        return  birth_record.getString(Birth.FATHER_IDENTITY ) +
                "-" + birth_record.getString(Birth.MOTHER_IDENTITY );
    }

    private static String toKeyFromMarriage(LXP marriage_record) {
        return  marriage_record.getString(Marriage.GROOM_IDENTITY ) +
                "-" + marriage_record.getString(Marriage.BRIDE_IDENTITY );
    }

    public int getNumberOfGroundTruthTrueLinks() {

        int count = 0;

        for(LXP marriage : record_repository.getMarriages()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage );

            for (LXP birth : record_repository.getBirths()) {

                String birth_key_from_marriage = toKeyFromBirth( birth );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return 0;
    }

    ////// AL HERE

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {

        Collection<LXP> filteredMarriageRecords = new HashSet<>();

        for(LXP record : marriage_records) {


            String groomForename = record.getString(Marriage.GROOM_FORENAME).trim();
            String groomSurname = record.getString(Marriage.GROOM_SURNAME).trim();
            String brideForename = record.getString(Marriage.BRIDE_FORENAME).trim();
            String brideSurname = record.getString(Marriage.BRIDE_SURNAME).trim();

            String pom = record.getString(Marriage.PLACE_OF_MARRIAGE).trim();
            String dom = record.getString(Marriage.MARRIAGE_DAY).trim();
            String mom = record.getString(Marriage.MARRIAGE_MONTH).trim();
            String yom = record.getString(Marriage.MARRIAGE_YEAR).trim();


            int populatedFields = 0;

            if (!(groomForename.equals("") || groomForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(groomSurname.equals("") || groomSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideForename.equals("") || brideForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideSurname.equals("") || brideSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(pom.equals("") || pom.equals("missing"))) {
                populatedFields++;
            }
            if (!(dom.equals("") || dom.equals("missing"))) {
                populatedFields++;
            }
            if (!(mom.equals("") || mom.equals("missing"))) {
                populatedFields++;
            }
            if (!(yom.equals("") || yom.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredMarriageRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredMarriageRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 5;
    }


    @Override
    public Iterable<LXP> getPreFilteredSearchRecords() {

        HashSet<LXP> filteredBirthRecords = new HashSet<>();

        for (LXP record : birth_records) {

            String fatherForename = record.getString(Birth.FATHER_FORENAME).trim();
            String fatherSurname = record.getString(Birth.FATHER_SURNAME).trim();
            String motherForename = record.getString(Birth.MOTHER_FORENAME).trim();
            String motherSurname = record.getString(Birth.MOTHER_SURNAME).trim();

            String pom = record.getString(Birth.PARENTS_PLACE_OF_MARRIAGE).trim();
            String dom = record.getString(Birth.PARENTS_DAY_OF_MARRIAGE).trim();
            String mom = record.getString(Birth.PARENTS_MONTH_OF_MARRIAGE).trim();
            String yom = record.getString(Birth.PARENTS_YEAR_OF_MARRIAGE).trim();

            int populatedFields = 0;

            if (!(fatherForename.equals("") || fatherForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(fatherSurname.equals("") || fatherSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(motherForename.equals("") || motherForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(motherSurname.equals("") || motherSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(pom.equals("") || pom.equals("missing"))) {
                populatedFields++;
            }
            if (!(dom.equals("") || dom.equals("missing"))) {
                populatedFields++;
            }
            if (!(mom.equals("") || mom.equals("missing"))) {
                populatedFields++;
            }
            if (!(yom.equals("") || yom.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredBirthRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredBirthRecords;
    }

    public String toKey(LXP record1, LXP record2) {
        String s1= record1.getString(Birth.ORIGINAL_ID);
        String s2 = record2.getString(Marriage.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        if(     record1.getString( Birth.FATHER_IDENTITY ).isEmpty()  ||
                record1.getString( Birth.MOTHER_IDENTITY ).isEmpty()  ||
                record2.getString(Marriage.GROOM_IDENTITY ).isEmpty() ||
                record2.getString(Marriage.BRIDE_IDENTITY ).isEmpty() ) {

                    return LinkStatus.UNKNOWN;

        }
        String marriage_key_from_marriage = toKeyFromMarriage( record1 );
        String birth_key_from_marriage = toKeyFromBirth( record2 );

        if (marriage_key_from_marriage.equals( birth_key_from_marriage ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
