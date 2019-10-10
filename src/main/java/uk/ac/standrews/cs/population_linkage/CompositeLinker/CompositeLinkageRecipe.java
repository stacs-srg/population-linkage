package uk.ac.standrews.cs.population_linkage.CompositeLinker;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.*;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class CompositeLinkageRecipe {

    public static void main(String[] args) throws BucketException, PersistentObjectException {

        StringMetric metric = new JensenShannon(4096);

        String links_persistent_name = "";
        String results_repository_name = "";

        String source_repository_name = "synthetic-scotland_13k_1_clean";
        double match_threshold = 0.8;

        RecordRepository recordRepository = new RecordRepository(ApplicationProperties.getStorePath(), source_repository_name);

        Map<Integer, Collection<Link>> deathGroomLinks = getLinks(new DeathGroomOwnMarriageIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository);
        Map<Integer, Collection<Link>> groomBirthLinks = getLinks(new GroomBirthIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository);

        Map<Integer, Collection<Link>> deathBrideLinks = getLinks(new DeathBrideOwnMarriageIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository);
        Map<Integer, Collection<Link>> brideBirthLinks = getLinks(new BrideBirthIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository);

        Map<Integer, Collection<DoubleLink>> deathBirthLinksViaGroom = combineLinks(deathGroomLinks, groomBirthLinks);
        Map<Integer, Collection<DoubleLink>> deathBirthLinks = combineLinks(deathBrideLinks, brideBirthLinks);
        deathBirthLinks.putAll(deathBirthLinksViaGroom); // the combine works as the male and female death records share the same unique ID space - thus no classes on combining maps (remember the prefilter checks for sex in the used linkers)

        LinkageQuality lq = selectAndAssessLinks(deathBirthLinks, new BirthDeathIdentityLinkageRecipe(links_persistent_name, source_repository_name, results_repository_name, recordRepository), true);

    }

    private static LinkageQuality selectAndAssessLinks(Map<Integer, Collection<DoubleLink>> indirectLinks, LinkageRecipe directLinkageForGT, boolean directReversed) throws BucketException, PersistentObjectException {

        int numberOfGroundTruthTrueLinks = directLinkageForGT.numberOfGroundTruthTrueLinks();

        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        for(Collection<DoubleLink> links : indirectLinks.values()) {
            Link link = chooseLink(links);
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

    private static Link chooseLink(Collection<DoubleLink> links) throws BucketException, PersistentObjectException {
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

    private static Map<Integer, Collection<DoubleLink>> combineLinks(Map<Integer, Collection<Link>> firstLinks, Map<Integer, Collection<Link>> secondLinks) throws BucketException {

        Map<Integer, Collection<DoubleLink>> doubleLinksByFirstRecordID = new HashMap<>();

        for(Integer record1ID : firstLinks.keySet()) {

            Collection<Link> firstLinksByID = firstLinks.get(record1ID);
            for(Link link1 : firstLinksByID) {

                Integer record2ID = Utilities.originalIdField(link1.getRecord2().getReferend());
                for(Link link2 : secondLinks.get(record2ID)) {
                    doubleLinksByFirstRecordID.computeIfAbsent(record1ID, o ->
                            new ArrayList<>()).add(new DoubleLink(link1, link2, "death-birth-via-groom-id"));
                }
            }
        }

        return doubleLinksByFirstRecordID;
    }

    public static Map<Integer, Collection<Link>> getLinks(LinkageRunner linkageRunner, final String links_persistent_name, final String source_repository_name,
                                                          final String results_repository_name, double match_threshold, StringMetric baseMetric, RecordRepository recordRepository) throws BucketException {

        LinkageRecipe linkageRecipe = linkageRunner.getLinkageRecipe(
                links_persistent_name, source_repository_name, results_repository_name,
                recordRepository);

        linkageRunner.setBaseMetric(baseMetric);

        Linker linker = linkageRunner.getLinker(match_threshold, linkageRecipe);
        linker.addRecords(linkageRecipe.getPreFilteredSourceRecords1(), linkageRecipe.getPreFilteredSourceRecords2());

        Map<Integer, Collection<Link>> linksByRecord1ID = new HashMap<>();

        for(Link link : linker.getLinks()) {
            linksByRecord1ID.computeIfAbsent(Utilities.originalIdField(link.getRecord1().getReferend()),
                    o -> new ArrayList<>()).add(link);
        }

        return linksByRecord1ID;
    }



}
