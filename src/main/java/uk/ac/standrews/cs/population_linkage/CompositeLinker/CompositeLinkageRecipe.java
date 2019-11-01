package uk.ac.standrews.cs.population_linkage.CompositeLinker;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.*;
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

        String source_repository_name = "synthetic-scotland_13k_1_corrupted_A";
        double match_threshold = 0.6;

        runIndirectDeathBirthLinkage(metric, links_persistent_name, results_repository_name, source_repository_name, match_threshold);

//        TreeMap<Double, LinkageQuality> thresholdResults = new BirthDeathIdentityLinkageRunner().evaluateThresholds(source_repository_name, metric, true, 5, 0.0, 0.1, 1.0);

//        print(thresholdResults);

    }

    private static void print(TreeMap<Double, LinkageQuality> thresholdResults) {

        for(Map.Entry<Double, LinkageQuality> thresholdResult : thresholdResults.entrySet()) {
            System.out.println(thresholdResult.getKey() + "," + thresholdResult.getValue().toCSV());
        }
    }

    private static LinkageQuality runIndirectDeathBirthLinkage(StringMetric metric, String links_persistent_name, String results_repository_name, String source_repository_name, double match_threshold) throws BucketException, PersistentObjectException {
        LinkageConfig.birthCacheSize = 15000;
        LinkageConfig.marriageCacheSize = 15000;
        LinkageConfig.deathCacheSize = 15000;
        LinkageConfig.numberOfROs = 60;

        RecordRepository recordRepository = new RecordRepository(ApplicationProperties.getStorePath(), source_repository_name);

        Map<String, Collection<Link>> deathGroomLinks = new DeathGroomOwnMarriageIdentityLinkageRunner().run(source_repository_name, 0.575, metric, true, 5, true, false).getMapOfLinks();
        Map<String, Collection<Link>> groomBirthLinks = new GroomBirthIdentityLinkageRunner().run(source_repository_name, 0.4, metric, true, 3, true, false).getMapOfLinks();

        Map<String, Collection<Link>> deathBrideLinks = new DeathBrideOwnMarriageIdentityLinkageRunner().run(source_repository_name, 0.375, metric, true, 3, true, false).getMapOfLinks();
        Map<String, Collection<Link>> brideBirthLinks = new BrideBirthIdentityLinkageRunner().run(source_repository_name, 0.4, metric, true, 5, true, false).getMapOfLinks();

        Map<String, Collection<DoubleLink>> deathBirthLinksViaGroom = combineLinks(deathGroomLinks, groomBirthLinks);
        Map<String, Collection<DoubleLink>> deathBirthLinks = combineLinks(deathBrideLinks, brideBirthLinks);
        deathBirthLinks.putAll(deathBirthLinksViaGroom); // the combine works as the male and female death records share the same unique ID space - thus no clashes on combining maps (remember the prefilter checks for sex in the used linkers)

        return selectAndAssessIndirectLinks(deathBirthLinks, new BirthDeathIdentityLinkageRecipe(links_persistent_name, source_repository_name, results_repository_name, recordRepository), true);
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
                if(bestLink.getDistance() > link.directLink().getDistance())
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

}
