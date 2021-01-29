/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.runners;

import uk.ac.standrews.cs.population_linkage.EndtoEnd.MetaMarriage;
import uk.ac.standrews.cs.population_linkage.EndtoEnd.experiments.DisplayMethods;
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

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthGroomIdentityLinkageRecipe.trueMatch;

public class BitBlasterSubsetOfDataEndtoEndBirthOwnMarriageAsGroomLinkageRunner extends BitBlasterLinkageRunner {

    HashMap<Long, MetaMarriage> familyBundles = new HashMap();  // maps from id on birth record to MetaMarriage

    private static final int NUMBER_OF_BIRTHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;


    @Override
    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        // NOTE - cannot use numberOfGroundTruthTrueLinks - not been initialised properly.

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if (!pre_filter) {
            throw new RuntimeException("This code only works with filtering - need to fix selection of sub region");
        }

        // This is alternative to the code in LinkageRunner which requires the whole set to be manifested.
        // This only manifests the first REQUIRED fields.

        ArrayList<LXP> filtered_birth_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords(), linkageRecipe.getLinkageFields());
        ArrayList<LXP> filtered_marriage_records = filter(prefilterRequiredFields, EVERYTHING, linkageRecipe.getQueryRecords(), linkageRecipe.getQueryMappingFields()); // do not filter these - will reduce what we can find otherwise!

        linker.addRecords(filtered_birth_records, filtered_marriage_records);

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

        createMarriagesFromLinks();

        showFamilies();

        numberOfGroundTruthTrueLinks = countTrueLinks(filtered_birth_records,filtered_marriage_records);

        System.out.println( "Num GT true links = " + numberOfGroundTruthTrueLinks );
        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return null; // TODO FIX THIS
    }

    /**
     * Takes the links in MetaMarriages and turns them into sets of births and Marriages
     */
    private void createMarriagesFromLinks() throws BucketException {
        for (MetaMarriage f : familyBundles.values()) {
            f.createMarriagesFromGroomLinks();
        }
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

    /**
     *
     * @param id - birth id of a groom in this family
     * @return a set of LXPs which are Birth Records
     * @throws BucketException
     */
    private Set<LXP> getGroomBirths(Long id) throws BucketException {
        MetaMarriage f = familyBundles.get(id);
        return f.getBirthsOfGrooms();
    }


    /**
     *
     * @param id - birth id of a potential family member.
     * @return a set of LXPs which are Marriage Records of the potential grooms in the family
     * @throws BucketException
     */
    private Set<LXP> getfamilyGroomMarriages(Long id) throws BucketException {
        MetaMarriage f = familyBundles.get(id);
        return f.getMarriageRecords();
    }

    private ArrayList<LXP> filter(int prefilterRequiredFields, int REQUIRED, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        int count_rejected = 0;
        int count_accepted = 0;
        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);
        for (LXP record : records_to_filter) {
            if (linkageRecipe.passesFilter(record, linkageFields, prefilterRequiredFields)) {
                filtered_source_records.add(record);
                count_accepted++;
            } else {
                count_rejected++;
            }
            if (filtered_source_records.size() >= REQUIRED) {
                break;
            }
        }
        System.out.println( "Filtering: accepted: " + count_accepted + " rejected: " + count_rejected + " from " + ( count_rejected + count_accepted ) );
        return filtered_source_records;
    }

    private void showFamilies() throws BucketException {

        int family_count = 0;
        for (Long id : familyBundles.keySet()) {
            Set<LXP> births = getGroomBirths(id);
            Set<LXP> marriages = getfamilyGroomMarriages(id);
            family_count++;
            showLXPMarriage(marriages);
            showLXPBirths(births,marriages);
        }
        System.out.println("Grooms and children matched formed: " + family_count);
        System.out.println( "Groom errors = " + groom_errors );
    }

    private void showLXPMarriage(Set<LXP> marriages) throws BucketException {
        System.out.println("MetaMarriage:");
        for (LXP marriage : marriages) {
            System.out.println("Marriage:");
            DisplayMethods.showMarriage(marriage);
        }
    }

    private static int groom_errors = 0;

    private void showLXPBirths(Set<LXP> births, Set<LXP> marriages) throws BucketException {
        System.out.println("Births:");

        for (LXP birth : births) {
            String child_id = birth.getString(Birth.CHILD_IDENTITY);
            System.out.println( "Multiple marriages: " + marriages.size() );
            for (LXP marriage : marriages) {
                String groom_id = marriage.getString(Marriage.GROOM_IDENTITY);
                boolean groom_matches_birth = groom_id.equals(child_id);
                DisplayMethods.showLXPBirth(birth, groom_matches_birth);
                if (!groom_matches_birth) {
                    groom_errors++;
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

        addLinktoFamily(id_1, link);
    }

    private void addLinktoFamily(long id, Link link) {
        MetaMarriage f = familyBundles.get(id);
        if (f == null) {
            f = new MetaMarriage();
        }
        f.addBirthMarriageGroomLink(link);
        familyBundles.put(id, f);
    }

}
