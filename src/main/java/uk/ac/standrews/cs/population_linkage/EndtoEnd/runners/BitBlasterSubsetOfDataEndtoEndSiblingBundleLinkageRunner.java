/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.runners;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments.DisplayMethods;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.time.LocalDateTime;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.EndtoEnd.runners.Util.getBirthSiblings;
import static uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe.trueMatch;

public class BitBlasterSubsetOfDataEndtoEndSiblingBundleLinkageRunner extends BitBlasterLinkageRunner {

    public static boolean VERBOSE = false;

    HashMap<Long, List<Link>> familyBundles = new HashMap();  // maps from id on birth record to MetaMarriage

    private static final int NUMBER_OF_BIRTHS = 10000;
//    private HashMap<String, List<LXP>> gt;


    @Override
    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        // NOTE - cannot use numberOfGroundTruthTrueLinks - not been initialised properly.

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if (!pre_filter) {
            throw new RuntimeException("This code only works with filtering - need to fix selection of sub region");
        }

        // This is alternative to the code in LinkageRunner which requires the whole set to be manifested.
        // This only manifests the first REQUIRED fields.

        ArrayList<LXP> filtered_source_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords(), linkageRecipe.getLinkageFields());    // TODO Are these just the same?
        // ArrayList<LXP> filtered_search_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getSearchRecords()); // TODO - yes for Birth-Birth but not in general

        linker.addRecords(filtered_source_records, filtered_source_records);

        System.out.println("Records added records to linker @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        int tp = 0;
        int fp = 0;
        int unknown = 0;

        links = dedupSymmetricAndSelfPairs(links);

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

        mergeFamilies();            // families are keyed by each family member - this eliminates duplicates and makes them keyed in familyBundles by ID of one (arbitrary) member
                                    // This is not what you would want in a real system - would want them keyed by all family members (probably).
        if( VERBOSE ) {
            showFamilies();
        }

        numberOfGroundTruthTrueLinks = countTrueLinks(filtered_source_records);

        report( "Num GT true links = " + numberOfGroundTruthTrueLinks );
        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return null; // TODO FIX THIS
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

    private int countTrueLinks(ArrayList<LXP> records) {
        Object[] arrai = records.toArray();
        int num_links = 0;
        for (int i = 0; i < arrai.length - 1; i++) {
            for (int j = i + 1; j < arrai.length; j++) {
                if( ! arrai[i].equals(arrai[j]) ) {
                    final LinkStatus linkStatus = trueMatch((LXP) arrai[i], (LXP) arrai[j]);
                    if (linkStatus == LinkStatus.TRUE_MATCH) {
                        num_links++;
                    }
                }
            }
        }
        return num_links;
    }


    private void mergeFamilies() throws BucketException {
        HashMap<Long, List<Link>> processed = new HashMap<>();
        for (Long primary_sibling_id : familyBundles.keySet()) {
            List<Link> this_family = familyBundles.get(primary_sibling_id);

            Set<LXP> siblings = getBirthSiblings( this_family );
            for (LXP sibling : siblings) {
                final long this_sibling_id = sibling.getId();

                List<Link> sibling_links = familyBundles.get(this_sibling_id);
                Set<LXP> siblings_siblings = getBirthSiblings( sibling_links );
                boolean siblings_family_subset_of_this = siblings_siblings.containsAll(siblings);
                boolean this_family_subset_of_siblings = siblings_siblings.containsAll(siblings);
                boolean families_are_the_same = siblings_family_subset_of_this && this_family_subset_of_siblings;

                if (families_are_the_same) {
                    this_family.addAll( sibling_links );
                    processed.put(this_sibling_id, this_family);            // replace the siblings family with this one in processed.
                    processed.put(primary_sibling_id, this_family);         // save this family
                    report("Removed 1 duplicated family");
                } else if (this_family_subset_of_siblings) {
                    sibling_links.addAll( this_family );
                    processed.put(primary_sibling_id, sibling_links);  // move this id into the siblings family
                    report("Removed 1 overlapping primary family");
                } else if (siblings_family_subset_of_this) {
                    this_family.addAll( sibling_links );
                    processed.put(this_sibling_id, this_family);                    // replace the siblings family with this one in processed.
                    report("Removed 1 overlapping sibling's family");
                } else {
                    if( VERBOSE ) {
                        report("Families are different ********* TODO!! ");
                        // TODO What to do.
                        // Options -
                        // Could combine and get a few potentially too big families
                        // Could split into 2 - move the intersecting records into one of the other - could perhaps keep track of (potentialy) related families
                        // Could split into 3 - Union plus two others.
                        // Could do something with distances - k-means style
                        report("partial overlap:");
                        DisplayMethods.showFamily(siblings);
                        report("family2:");
                        DisplayMethods.showFamily(siblings_siblings);
                    }
                }
            }
        }
        familyBundles = processed; // finaly replaced familyBundles with the merged family map.
    }

    static int father_errors = 0;   // hack
    static int mother_errors = 0;   // hack

    private void showFamilies() throws BucketException {

        int count = 0;
        int family_count = 0;
        int person_count = 0;

        for (List<Link> s: familyBundles.values()) {
            Set<LXP> births = getBirthSiblings( s );
            family_count++;
            person_count += births.size();
            DisplayMethods.showFamily(births);
            count++;
        }
        report("Births in families = " + familyBundles.keySet().size() );
        report("Families formed:");
        report("father_errors = " + father_errors + " mother_errors = " + mother_errors);
        report("Number of people found = " + person_count);
        report("No families formed = " + family_count);
    }

    private void BundleFamilies(Link link) throws BucketException {
        Birth rec_1 = (Birth) link.getRecord1().getReferend();
        Birth rec_2 = (Birth) link.getRecord2().getReferend();

        long id_1 = rec_1.getId();
     //   String id1_1 = rec_1.getString( Birth.ORIGINAL_ID ); Thinking about changing to this to make consistent with mapoflinks
        long id_2 = rec_2.getId();
    //    String id1_1 = rec_1.getString( Birth.ORIGINAL_ID );

        addLinktoFamily(id_1, link);
        addLinktoFamily(id_2, link);
    }

    private void addLinktoFamily(long id, Link link) {
        List<Link> links = familyBundles.get(id);
        if (links == null) {
            links = new ArrayList<>();
        }
        links.add(link);
        familyBundles.put(id, links);
    }

    public HashMap<Long, List<Link>> getFamilyBundles() {
        return familyBundles;
    }

    public static void report( String s ) {
        if( VERBOSE ) {
            System.out.println( s );
        }
    }
}
