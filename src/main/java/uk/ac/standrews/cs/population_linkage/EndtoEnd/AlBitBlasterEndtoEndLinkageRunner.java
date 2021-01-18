/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd;

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

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe.trueMatch;

public class AlBitBlasterEndtoEndLinkageRunner extends BitBlasterLinkageRunner {

    HashMap<Long, Family> familyBundles = new HashMap();  // maps from id on birth record to Family

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

        ArrayList<LXP> filtered_source_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords());    // TODO Are these just the same?
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

        createFamilyBirths();
        mergeFamilies();            // families are keyed by each family member - this eliminates duplicates and makes them keyed in familyBundles by ID of one (arbitrary) member
                                    // This is not what you would want in a real system - would want them keyed by all family members (probably).
        showFamilies();

        numberOfGroundTruthTrueLinks = countTrueLinks(filtered_source_records);

        System.out.println( "Num GT true links = " + numberOfGroundTruthTrueLinks );
        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return null; // TODO FIX THIS
    }

    /**
     * Takes the links in families and turns them into sets of births.
     */
    private void createFamilyBirths() throws BucketException {
        for (Family f : familyBundles.values()) {
            f.createSiblings();
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
        List<Long> to_remove = new ArrayList<>();
        List<Long> processed = new ArrayList<>();
        for (Long id : familyBundles.keySet()) {
            Set<LXP> this_family = getfamilyBirths(id);
            processed.add(id);
            for (LXP sibling : this_family) {
                if (!processed.contains(sibling.getId())) { // don't look at siblings already processed - they are in.
                    Set<LXP> siblings_family = getfamilyBirths(sibling.getId());
                    boolean siblings_family_subset_of_this = siblings_family.containsAll(this_family);
                    boolean this_family_subset_of_siblings = this_family.containsAll(siblings_family);
                    boolean families_are_the_same = siblings_family_subset_of_this && this_family_subset_of_siblings;

                    if ( families_are_the_same ) {
                        to_remove.add(sibling.getId());
                        System.out.println("Removed 1 duplicated family");
                    } else if( this_family_subset_of_siblings ) {
                        to_remove.add(id);
                        System.out.println("Removed 1 overlapping primary family");
                    } else if( siblings_family_subset_of_this ) {
                        to_remove.add(sibling.getId());
                        System.out.println("Removed 1 overlapping sibling's family");
                    } else {
                        System.out.println("Families are different"); // TODO What to do.
                        System.out.println( "partial overlap:");
                        showFamily( this_family );
                        System.out.println( "family2:" );
                        showFamily( siblings_family );

                    }
                }
            }
        }
        for (Long id : to_remove) {
            familyBundles.remove(id);
        }
    }


    /**
     *
     * @param id - birth id of a potential family member.
     * @return a set of LXPs which are Borth Records
     * @throws BucketException
     */
    private Set<LXP> getfamilyBirths(Long id) throws BucketException {
        Family f = familyBundles.get(id);
        return f.getBirthSiblings();
    }


    private ArrayList<LXP> filter(int prefilterRequiredFields, int REQUIRED, Iterable<LXP> records_to_filter) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
        for (LXP birth : records_to_filter) {
            if (linkageRecipe.passesFilter(birth, linkageRecipe.getLinkageFields(), prefilterRequiredFields)) {
                filtered_source_records.add(birth);
            }
            if (filtered_source_records.size() >= REQUIRED) {
                break;
            }
        }
        return filtered_source_records;
    }

    static int father_errors = 0;   // hack
    static int mother_errors = 0;   // hack

    private void showFamilies() throws BucketException {

        int count = 0;
        int family_count = 0;
        int person_count = 0;

        for (Long id : familyBundles.keySet()) {
            Set<LXP> births = getfamilyBirths(id);
            family_count++;
            person_count += births.size();
            showFamily(births);
            count++;
        }
        System.out.println("Families formed:");
        System.out.println("father_errors = " + father_errors + " mother_errors = " + mother_errors);
        System.out.println("Number of people found = " + person_count);
        System.out.println("No families formed = " + family_count);
    }

    private void showFamily(Set<LXP> births) throws BucketException {
        System.out.println("Family:");
        String family_father_id = "";
        String family_mother_id = "";

        for (LXP birth : births) {
            String this_father_id = birth.getString(Birth.FATHER_IDENTITY);
            String this_mother_id = birth.getString(Birth.MOTHER_IDENTITY);
            if (family_father_id.equals("")) {
                family_father_id = this_father_id;  // saves the first mama and pappa
                family_mother_id = this_mother_id;
                showBirth(birth, true, true);
            } else {
                showBirth(birth, family_father_id.equals(family_father_id), family_mother_id.equals(this_mother_id));
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
        Birth rec_2 = (Birth) link.getRecord2().getReferend();

        long id_1 = rec_1.getId();
        long id_2 = rec_2.getId();

        addLinktoFamily(id_1, link);
        addLinktoFamily(id_2, link);
    }

    private void addLinktoFamily(long id, Link link) {
        Family f = familyBundles.get(id);
        if (f == null) {
            f = new Family();
        }
        f.addSiblingBirth(link);
        familyBundles.put(id, f);
    }

    public void showBirth(LXP birth, boolean father_matches, boolean mother_matches) throws BucketException {

        String firstname = birth.getString(Birth.FORENAME);
        String surname = birth.getString(Birth.SURNAME);
        String father_id = birth.getString(Birth.FATHER_IDENTITY);
        String mother_id = birth.getString(Birth.MOTHER_IDENTITY);
        String parental_match = "  " + (father_matches ? "YES" : "NO") + "/" + (father_matches ? "YES" : "NO");
        long oid = birth.getId();
        System.out.println(oid + ": " + firstname + "," + surname + " F: " + father_id + " M: " + mother_id + "\t" + parental_match);
    }

}
