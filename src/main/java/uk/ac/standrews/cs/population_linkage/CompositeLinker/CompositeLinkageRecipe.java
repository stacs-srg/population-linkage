package uk.ac.standrews.cs.population_linkage.CompositeLinker;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.*;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.*;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class CompositeLinkageRecipe {

    public static void main(String[] args) throws BucketException, PersistentObjectException {

        StringMetric metric = new JensenShannon(4096);

        String links_persistent_name = "";
        String results_repository_name = "";

        String source_repository_name = "synthetic-scotland_13k_1_clean";
        double match_threshold = 0.75;

        LinkageConfig.birthCacheSize = 15000;
        LinkageConfig.marriageCacheSize = 15000;
        LinkageConfig.deathCacheSize = 15000;
        LinkageConfig.numberOfROs = 60;

        RecordRepository recordRepository = new RecordRepository(ApplicationProperties.getStorePath(), source_repository_name);

        Map<String, Collection<Link>> deathGroomLinks = getLinks(new DeathGroomOwnMarriageIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository, 5);
        Map<String, Collection<Link>> groomBirthLinks = getLinks(new GroomBirthIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository, Integer.MAX_VALUE);

        Map<String, Collection<Link>> deathBrideLinks = getLinks(new DeathBrideOwnMarriageIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository, Integer.MAX_VALUE);
        Map<String, Collection<Link>> brideBirthLinks = getLinks(new BrideBirthIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository, Integer.MAX_VALUE);

        Map<String, Collection<DoubleLink>> deathBirthLinksViaGroom = combineLinks(deathGroomLinks, groomBirthLinks);
        Map<String, Collection<DoubleLink>> deathBirthLinks = combineLinks(deathBrideLinks, brideBirthLinks);
        deathBirthLinks.putAll(deathBirthLinksViaGroom); // the combine works as the male and female death records share the same unique ID space - thus no clashes on combining maps (remember the prefilter checks for sex in the used linkers)

        LinkageQuality lq = selectAndAssessIndirectLinks(deathBirthLinks, new BirthDeathIdentityLinkageRecipe(links_persistent_name, source_repository_name, results_repository_name, recordRepository), true);

    }

    private static LinkageQuality selectAndAssessIndirectLinks(Map<String, Collection<DoubleLink>> indirectLinks, LinkageRecipe directLinkageForGT, boolean directReversed) throws BucketException, PersistentObjectException {

        int numberOfGroundTruthTrueLinks = directLinkageForGT.getNumberOfGroundTruthTrueLinks();

        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        for(Collection<DoubleLink> links : indirectLinks.values()) {
            Link link = chooseIndirectLink(links);
            if (trueMatch(link, directLinkageForGT, directReversed)) {
                tp++;
            } else {
                fp++;
            }
        }

        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return lq;
    }



    private static boolean trueMatch(Link link, LinkageRecipe directLinkageForGT, boolean directReversed) throws BucketException {
        if(directReversed) {
            return directLinkageForGT.isTrueMatch(link.getRecord2().getReferend(), link.getRecord1().getReferend()).equals(TRUE_MATCH);
        } else {
            return directLinkageForGT.isTrueMatch(link.getRecord1().getReferend(), link.getRecord2().getReferend()).equals(TRUE_MATCH);
        }
    }

    private static Link chooseIndirectLink(Collection<DoubleLink> links) throws BucketException, PersistentObjectException {
        Link bestLink = null;

        for(DoubleLink link : links) {
            if(bestLink == null) {
                bestLink = link.directLink();
            } else {
                if(bestLink.getConfidence() < link.directLink().getConfidence())
                    bestLink = link.directLink();
            }
        }
        return bestLink;
    }



    private static Map<String, Collection<DoubleLink>> combineLinks(Map<String, Collection<Link>> firstLinks, Map<String, Collection<Link>> secondLinks) throws BucketException {

        Map<String, Collection<DoubleLink>> doubleLinksByFirstRecordID = new HashMap<>();

        for(String record1ID : firstLinks.keySet()) {

            Collection<Link> firstLinksByID = firstLinks.get(record1ID);
            for(Link link1 : firstLinksByID) {

                String record2ID = Utilities.originalId(link1.getRecord2().getReferend());
                if(secondLinks.get(record2ID) != null) {
                    for (Link link2 : secondLinks.get(record2ID)) {
                        doubleLinksByFirstRecordID.computeIfAbsent(record1ID, o ->
                                new ArrayList<>()).add(new DoubleLink(link1, link2, "death-birth-via-groom-id"));
                    }
                }
            }
        }

        return doubleLinksByFirstRecordID;
    }

    public static Map<String, Collection<Link>> getLinks(LinkageRunner linkageRunner, final String links_persistent_name, final String source_repository_name,
                                                          final String results_repository_name, double match_threshold, StringMetric baseMetric, RecordRepository recordRepository, int prefilterRequiredFields) throws BucketException {

        LinkageRecipe linkageRecipe = linkageRunner.getLinkageRecipe(
                links_persistent_name, source_repository_name, results_repository_name,
                recordRepository);

        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(prefilterRequiredFields);

        linkageRunner.setBaseMetric(baseMetric);

        Linker linker = linkageRunner.getLinker(match_threshold, linkageRecipe);
        linker.addRecords(linkageRecipe.getPreFilteredStoredRecords(), linkageRecipe.getPreFilteredSearchRecords());

        Map<String, Collection<Link>> linksByRecord1ID = new HashMap<>();

        for(Link link : linker.getLinks()) {
            String origonalID = Utilities.originalId(link.getRecord1().getReferend());
            linksByRecord1ID.computeIfAbsent(origonalID, k -> new LinkedList<>());
            linksByRecord1ID.get(origonalID).add(link);

//            linksByRecord1ID.computeIfAbsent(Utilities.originalId(link.getRecord1().getReferend()),
//                    o -> new HashSet<>()).add(link);
        }

        return linksByRecord1ID;
    }



}
