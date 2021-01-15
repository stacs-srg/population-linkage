/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.time.LocalDateTime;
import java.util.*;

public class AlBitBlasterEndtoEndLinkageRunner extends BitBlasterLinkageRunner {

    HashMap<Long, List<Link>> familyBundles = new HashMap();  // maps from id to all links in same family.

    private static int NUMBER_OF_BIRTHS = 10000;
    private HashMap<String, List<LXP>> gt;


    @Override
    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if( ! pre_filter ) {
            throw new RuntimeException( "This code only works with filtering - need to fix selection of sub region" );
        }

        // This is alternative to the code in LinkageRunner which requires the whole set to be manifested.
        // This only manifests the first REQUIRED fields.

        ArrayList<LXP> filtered_source_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords()); // TODO Are these just the same?
        // ArrayList<LXP> filtered_search_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getSearchRecords()); // TODO - yes for Birth-Birth but not in general

        linker.addRecords(filtered_source_records, filtered_source_records);
        gt = formGroundTruth(filtered_source_records);

        System.out.println("Records added records to linker @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

            for (Link linkage_says_true_link : links) {

                bundleLinks( linkage_says_true_link );
            }

            mergeFamilies(); // families are keyed by each family member - this eliminates duplicates
            showFamilies();
            AnalyseRecall();

            return null; // TODO FIX THIS
    }

    private void AnalyseRecall() throws BucketException {
        int fn = 0;            // count the siblings that are missing
        int tp = 0;                 // count the number of siblings that are in correct groups
        int gt_count_births = 0;    // sanity check - count total number of individuals
        int singletons = 0; // these are all the single child families.

        for( Long id : familyBundles.keySet() ) {          // Looks through all the families that we have discovered
            Set<LXP> found_siblings = getfamilyBirths(id);
            LXP first_child = (LXP) found_siblings.toArray()[0];                 // the first child selected from the group
            gt_count_births++; // always count the fist child
            String papa_id = first_child.getString(Birth.FATHER_IDENTITY);
            List<LXP> real_siblings = gt.get(papa_id);                          // these are the siblings of the first child from ground truth
            if (real_siblings.size() > 1) {   // Only look at families with at least 2 children

                for (LXP real_sibling : real_siblings) {

                    gt_count_births++;

                    if (found_siblings.contains(real_sibling)) {  // count the found siblings that are in the ground truth family group
                        tp++;

                    } else {
                        fn++;  // count the missing siblings.
                    }
                }

            } else {
                singletons++; // these are all the single child families.
            }
        }
        System.out.println( "AnalyseRecall:" );
        System.out.println( gt_count_births + " gt births counted, " + (gt_count_births - NUMBER_OF_BIRTHS) + " assigned to more than 1 family" );
        System.out.println( tp + " siblings found from tp + fn = " + (tp + fn ) + " Recall = " + ( ( tp * 1.0 ) / ( (tp + fn ) * 1.0 ) ) + "%" );
        System.out.println( singletons + " individuals not in any family" );
    }

    /**
     * @param source_records
     * @return a list of families (lists of birth records indexed by father id
     */
    private HashMap<String, List<LXP>> formGroundTruth(ArrayList<LXP> source_records) {
        gt = new HashMap<>();
        for( LXP birth : source_records ) {
            String papa_id = birth.getString( Birth.FATHER_IDENTITY );
            addBirthToGT( papa_id,birth );
        }
        return gt;
    }

    private void addBirthToGT(String id, LXP birth) {
        List<LXP> list = gt.get(id);
        if( list == null ) {
            list = new ArrayList<LXP>();
        }
        list.add( birth );
        gt.put( id,list );
    }

    private void mergeFamilies() throws BucketException {
        List<Long> to_remove = new ArrayList<>();
        List<Long> processed = new ArrayList<>();
        for( Long id : familyBundles.keySet() ) {
            Set<LXP> births = getfamilyBirths(id);
            processed.add(id);
            for( LXP sibling : births ) {
                if( ! processed.contains( sibling.getId() ) ) { // don't look at siblings already processed - they are in.
                    if( familiesAreSame( getfamilyBirths( sibling.getId() ), births ) ) {
                            to_remove.add(sibling.getId());
                            System.out.println( "Removed 1 family" );
                    } else {
                        System.out.println( "Families are different" ); // TODO What to do.
                    }
                }
            }
        }
        for( Long id : to_remove ) {
            familyBundles.remove(id);
        }
    }

    private boolean familiesAreSame(Set<LXP> births1, Set<LXP> births2) {
        return births1.containsAll(births2) && births2.containsAll(births1);
    }

    private Set<LXP> getfamilyBirths(Long id) throws BucketException {
        List<Link> links = familyBundles.get(id);
        return unpair(links);
    }


    private ArrayList<LXP> filter(int prefilterRequiredFields, int REQUIRED, Iterable<LXP> records_to_filter) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
        for( LXP birth : records_to_filter) {
            if( linkageRecipe.passesFilter( birth, linkageRecipe.getLinkageFields(), prefilterRequiredFields) ) {
                filtered_source_records.add(birth);
            }
            if( filtered_source_records.size() >= REQUIRED) {
                break;
            }
        }
        return filtered_source_records;
    }

    static int father_errors = 0;   // hack
    static int mother_errors = 0;   // hack

    private void showFamilies() throws BucketException {

        int count = 0;
        int singles_count = 0;
        int family_count = 0;
        int person_count = 0;

        for( Long id : familyBundles.keySet() ) {
            Set<LXP> births = getfamilyBirths(id);
            if( births.size() == 1 ) {
                singles_count++;
            } else {
                family_count++;
            }
            person_count += births.size();
            showFamily(births);
            count++;
        }
        System.out.println( "Show Families formed:" );
        System.out.println( "father_errors = " + father_errors + " mother_errors = " + mother_errors );
        System.out.println( "Number of people found = " + person_count + "/" + NUMBER_OF_BIRTHS + " duplicates (assigned to more than 1 family) = " + ( person_count - NUMBER_OF_BIRTHS ) );
        System.out.println( "No families formed = " + count );
        System.out.println( "Number births in families = " + family_count );
    }

    private Set<LXP> unpair(List<Link> links) throws BucketException {
        Set<LXP> births = new TreeSet<>();
        for( Link link : links ) {
            births.add( link.getRecord1().getReferend() );
            births.add( link.getRecord2().getReferend() );

        }
        return births;
    }

    private void showFamily(Set<LXP> births) throws BucketException {
        System.out.println("Family:");
        String family_father_id = "";
        String family_mother_id = "";

        for (LXP birth : births) {
            String this_father_id = birth.getString( Birth.FATHER_IDENTITY );
            String this_mother_id = birth.getString( Birth.MOTHER_IDENTITY );
            if( family_father_id.equals("") ) {
                family_father_id = this_father_id;  // saves the first mama and pappa
                family_mother_id = this_mother_id;
                showBirth(birth,true,true);
            } else {
                showBirth( birth,family_father_id.equals(family_father_id),family_mother_id.equals(this_mother_id) );
                if( ! family_father_id.equals(family_father_id) ) {
                    father_errors++;
                }
                if( ! family_father_id.equals(family_father_id) ) {
                    mother_errors++;
                }
            }
        }
        System.out.println("===");
    }

    private void bundleLinks(Link link) throws BucketException {
        Birth rec_1 = (Birth) link.getRecord1().getReferend();
        Birth rec_2 = (Birth) link.getRecord2().getReferend();

        long id_1 = rec_1.getId();
        long id_2 = rec_2.getId();

        addLinktoBundle( id_1,link );
        addLinktoBundle( id_2,link );
    }

    private void addLinktoBundle(long id, Link link) {
        List<Link> list = familyBundles.get(id);
        if( list == null ) {
            list = new ArrayList<Link>();
        }
        list.add( link );
        familyBundles.put( id,list );
    }

    public void showBirth(LXP birth, boolean father_matches, boolean mother_matches) throws BucketException {

        String firstname = birth.getString( Birth.FORENAME );
        String surname = birth.getString( Birth.SURNAME );
        String father_id = birth.getString( Birth.FATHER_IDENTITY );
        String mother_id = birth.getString( Birth.MOTHER_IDENTITY );
        String parental_match = "  " + ( father_matches ? "YES" : "NO" ) + "/" + ( father_matches ? "YES" : "NO" );
        long oid = birth.getId();
        System.out.println( oid + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + parental_match );
    }

}
