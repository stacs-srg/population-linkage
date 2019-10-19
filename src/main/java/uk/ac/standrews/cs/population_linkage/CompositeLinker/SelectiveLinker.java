package uk.ac.standrews.cs.population_linkage.CompositeLinker;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipies.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.DeathGroomOwnMarriageIdentityLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.Collection;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class SelectiveLinker {

    public static void main(String[] args) throws BucketException, PersistentObjectException {

        StringMetric metric = new JensenShannon(4096);

        String links_persistent_name = "";
        String results_repository_name = "";

        String source_repository_name = "synthetic-scotland_13k_1_clean";
        double match_threshold = 0.89;

        LinkageConfig.birthCacheSize = 15000;
        LinkageConfig.marriageCacheSize = 15000;
        LinkageConfig.deathCacheSize = 15000;
        LinkageConfig.numberOfROs = 30;

        RecordRepository recordRepository = new RecordRepository(ApplicationProperties.getStorePath(), source_repository_name);

        Map<String, Collection<Link>> deathGroomLinks = CompositeLinkageRecipe.getLinks(new DeathGroomOwnMarriageIdentityLinkageRunner(), links_persistent_name, source_repository_name, results_repository_name, match_threshold, metric, recordRepository, 5);
        LinkageQuality lq = selectAndAssessLinks(deathGroomLinks, new DeathGroomOwnMarriageIdentityLinkageRecipe(links_persistent_name, source_repository_name, results_repository_name, recordRepository));
    }

    private static LinkageQuality selectAndAssessLinks(Map<String, Collection<Link>> links, LinkageRecipe linkageForGT) throws BucketException, PersistentObjectException {

        int numberOfGroundTruthTrueLinks = linkageForGT.getNumberOfGroundTruthTrueLinks();

        int tp = 0; // these are counters with which we use if evaluating
        int fp = 0;

        for(Collection<Link> linksForID : links.values()) {
            Link link = chooseLink(linksForID);

            if (trueMatch(link, linkageForGT)) {
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

    private static Link chooseLink(Collection<Link> links) throws BucketException, PersistentObjectException {
        Link bestLink = null;

        for(Link link : links) {
            if(bestLink == null) {
                bestLink = link;
            } else {
                if(bestLink.getConfidence() < link.getConfidence())
                    bestLink = link;
            }
        }
        return bestLink;
    }

    private static boolean trueMatch(Link link, LinkageRecipe directLinkageForGT) throws BucketException {
        return directLinkageForGT.isTrueMatch(link.getRecord1().getReferend(), link.getRecord2().getReferend()).equals(TRUE_MATCH);
    }
}
