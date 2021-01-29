/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.runners;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.MetaMarriage;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe.trueMatch;

public class HackedBitBlasterSubsetOfDataEndtoEndBirthParentsMarriageLinkageRunner extends BitBlasterLinkageRunner {

    HashMap<Long, MetaMarriage> familyBundles = new HashMap();  // maps from id on birth record to MetaMarriage

    private static final int NUMBER_OF_BIRTHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;

    @Override
    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        // NOTE - cannot use numberOfGroundTruthTrueLinks - not been initialised properly.

        if (!pre_filter) {
            throw new RuntimeException("This code only works with filtering - need to fix selection of sub region");
        }

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        ArrayList<LXP> birth_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords(), linkageRecipe.getLinkageFields());
        ArrayList<LXP> marriage_records = filter(prefilterRequiredFields, EVERYTHING, linkageRecipe.getQueryRecords(), linkageRecipe.getQueryMappingFields()); // do not filter these - will reduce what we can find otherwise!

        linker.addRecords(birth_records, marriage_records);

        System.out.println("Records added records to linker @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        int tp = 0;
        int fp = 0;
        int unknown = 0;

        for (Link link : links) {

            LXP rec1 = link.getRecord1().getReferend();
            LXP rec2 = link.getRecord2().getReferend();
            final LinkStatus linkStatus = trueMatch(rec1,rec2);

            switch (linkStatus) {
                case TRUE_MATCH:
                    tp++;
                    break;
                case NOT_TRUE_MATCH:
                    fp++;
                    break;
                default:
                    unknown++;
            }
            BundleFamilies(link);
        }

        createMetaMarriageFromBirthAndFathersMarriage();

        createMarriagesFromLinks();

        mergeFamilies();            // families are keyed by each family member - this eliminates duplicates and makes them keyed in familyBundles by ID of one (arbitrary) member
                                    // This is not what you would want in a real system - would want them keyed by all family members (probably).
        showFamilies();

        numberOfGroundTruthTrueLinks = countTrueLinks(birth_records, marriage_records);

        System.out.println( "Num GT true links = " + numberOfGroundTruthTrueLinks );
        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return null; // TODO FIX THIS
    }

    private void createMarriagesFromLinks() throws BucketException {
        for (MetaMarriage f : familyBundles.values()) {
            f.createMarriagesFromBirthAndFathersMarriageLinks();
            f.createMarriagesFromBirthAndMothersMarriageLinks();
        }
    }

    /**
     * Takes the links in families and turns them into sets of births and Marriages
     */
    private void createMetaMarriageFromBirthAndFathersMarriage() throws BucketException {
        for (MetaMarriage f : familyBundles.values()) {
            f.createMarriagesFromBirthAndFathersMarriageLinks();
        }
    }

    private List<Link> dedupSymmetricAndSelfPairs(Iterable<Link> links) throws BucketException {

        List<Link> result = new ArrayList<>();

        for (Link link : links) {

            LXP rec1 = link.getRecord1().getReferend();
            LXP rec2 = link.getRecord2().getReferend();

            if( !rec1.equals(rec2) && ! reverseIsIn( rec1,rec2,result) ) {
                result.add( link );
            }
        }

        return result;
    }

    private boolean reverseIsIn(LXP rec1, LXP rec2, List<Link> list) throws BucketException {
        for (Link link : list) {
            if( link.getRecord1().getReferend().equals(rec2) && link.getRecord2().getReferend().equals(rec1) ) {
                return true;
            }
        }
        return false;
    }

    private int countTrueLinks(ArrayList<LXP> birth_records,ArrayList<LXP> marriage_records) {
        int num_links = 0;
        for ( LXP birth : birth_records ) {
            for (LXP marriage : marriage_records) {
                final LinkStatus linkStatus = trueMatch(birth, marriage);
                if (linkStatus == LinkStatus.TRUE_MATCH) {
                    num_links++;
                }
            }
        }
        return num_links;
    }


    private void mergeFamilies() throws BucketException {
        System.out.println("No merge yet");
    }

    private ArrayList<LXP> filter(int requiredFields, int number_of_fields_required, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        int count_rejected = 0;
        int count_accepted = 0;
        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(requiredFields);
        for (LXP record : records_to_filter) {
            if (linkageRecipe.passesFilter(record, linkageFields, requiredFields)) {
                filtered_source_records.add(record);
                count_accepted++;
            } else {
                count_rejected++;
            }
            if (filtered_source_records.size() >= number_of_fields_required) {
                break;
            }
        }
        System.out.println( "Filtering: accepted: " + count_accepted + " rejected: " + count_rejected + " from " + ( count_rejected + count_accepted ) );
        return filtered_source_records;
    }

    static int father_errors = 0;   // hack
    static int mother_errors = 0;   // hack

    private void showFamilies() throws BucketException {

        int family_count = 0;
        int fathers_count = 0;

        for (Long id : familyBundles.keySet()) {
            MetaMarriage m = familyBundles.get(id);
            Set<LXP> births = m.getBirthSiblings();
            Set<LXP> marriages = m.getMarriageRecords();
            family_count++;
            fathers_count += births.size();
            showLXPMarriage(marriages);
            showLXPBirths(births);
        }
        System.out.println("Families formed:");
        System.out.println("father_errors = " + father_errors + " mother_errors = " + mother_errors);
        System.out.println("Number of birth keys = " + familyBundles.keySet().size() );
        System.out.println("Number of fathers' births found = " + fathers_count);
        System.out.println("No families formed = " + family_count);
    }

    private void showLXPMarriage(Set<LXP> marriages) throws BucketException {
        System.out.println("MetaMarriage:");
        for (LXP marriage : marriages) {
            System.out.println("Marriage:");
            showLXPMarriage(marriage);
        }
    }

    private void showLXPBirths(Set<LXP> births) throws BucketException {
        System.out.println("Births:");
        String family_father_id = "";
        String family_mother_id = "";

        for (LXP birth : births) {
            String this_father_id = birth.getString(Birth.FATHER_IDENTITY);
            String this_mother_id = birth.getString(Birth.MOTHER_IDENTITY);
            if (family_father_id.equals("")) {
                family_father_id = this_father_id;  // saves the first mama and pappa
                family_mother_id = this_mother_id;
                showLXPBirth(birth, true, true);
            } else {
                showLXPBirth(birth, family_father_id.equals(family_father_id), family_mother_id.equals(this_mother_id));
                if (!family_father_id.equals(family_father_id)) {
                    father_errors++;
                }
                if (!family_father_id.equals(family_father_id)) {
                    mother_errors++;
                }
            }
        }
        System.out.println("===");
    }

    private void BundleFamilies(Link link) throws BucketException {
        Birth rec_1 = (Birth) link.getRecord1().getReferend();
        Marriage rec_2 = (Marriage) link.getRecord2().getReferend();

        long id_1 = rec_1.getId();
        long id_2 = rec_2.getId();

        addBirthFathersMarriageLinktoFamily(id_1, link);
    }

    private void addBirthFathersMarriageLinktoFamily(long id, Link link) {
        MetaMarriage f = familyBundles.get(id);
        if (f == null) {
            f = new MetaMarriage();
        }
        f.addBirthFathersMarriageLink(link);
        familyBundles.put(id, f);
    }

    public void showLXPBirth(LXP birth, boolean father_matches, boolean mother_matches) throws BucketException {

        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);

        String parental_match = "  " + (father_matches ? "YES" : "NO") + "/" + (mother_matches ? "YES" : "NO");
        long oid = birth.getId();
        String std_id = birth.getString(Birth.STANDARDISED_ID);
        System.out.println(oid + "/" + std_id + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + parental_match);
    }

    public void showLXPMarriage(LXP marriage) throws BucketException {

        String gfirstname = marriage.getString(Marriage.GROOM_FORENAME);
        String gsurname = marriage.getString(Marriage.GROOM_SURNAME);
        String gid = marriage.getString(Marriage.GROOM_IDENTITY);

        String bfirstname = marriage.getString(Marriage.BRIDE_FORENAME);
        String bsurname = marriage.getString(Marriage.BRIDE_SURNAME);
        String bid = marriage.getString(Marriage.BRIDE_IDENTITY);

        long oid = marriage.getId();
        String std_id = marriage.getString( Marriage.STANDARDISED_ID );

        System.out.println(oid + "/" + std_id + ": G: " + gfirstname + "," + gsurname + " GID: " + gid + " B: " + bfirstname + "," + bsurname + " BID: " + bid );
    }

}
