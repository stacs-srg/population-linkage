/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

public class BitBlasterLinkageRunner extends LinkageRunner {

    @Override
    public LinkageRecipe getLinkageRecipe(String links_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return linkage_recipe;
    }

    public Linker getLinker(LinkageRecipe linkageRecipe) {
        Metric<LXP> compositeMetric = linkageRecipe.getCompositeMetric();
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric), compositeMetric, linkageRecipe.getThreshold(), getNumberOfProgressUpdates(),
                linkageRecipe.getLinkageType(), "threshold match at ", linkageRecipe.getStoredRole(), linkageRecipe.getQueryRole(), linkageRecipe::isViableLink, linkageRecipe);
    }

    public SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric);
    }

    protected List<LXP> getReferencePoints() {
        List<LXP> candidates = filter(linkage_recipe.getLinkageFields().size(), LinkageRecipe.EVERYTHING, linkage_recipe.getStoredRecords(), linkage_recipe.getLinkageFields());
        return BitBlasterSearchStructure.chooseRandomReferencePoints(candidates, LinkageConfig.numberOfROs);
    }

    public LinkageResult link(MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persist_links) throws Exception {

        System.out.println("Adding records into linker @ " + LocalDateTime.now());
        ((SimilaritySearchLinker) linker).addRecords(linkage_recipe.getStoredRecords(), linkage_recipe.getQueryRecords(), getReferencePoints());
        System.out.println("Constructing link iterable @ " + LocalDateTime.now());

        Iterable<Link> links = linker.getLinks();

        return processLinks(make_persistent, evaluate_quality, persist_links, links);
    }

    @Override
    public LinkageResult linkLists(MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persist_links, boolean isIdentityLinkage) throws Exception {
        System.out.println("Adding records into linker @ " + LocalDateTime.now());
        ((SimilaritySearchLinker) linker).addRecords(linkage_recipe.getStoredRecords(), linkage_recipe.getQueryRecords(), getReferencePoints());
        System.out.println("Constructing link iterable @ " + LocalDateTime.now());

        List<Link> linked_pairs = new ArrayList<>();

        for (List<Link> list_of_links : linker.getListsOfLinks() ) {
            if( list_of_links.size() > 0 ) {
                if( ! isIdentityLinkage ) {   // for non identity add all of then for now - TODO EXPLORE THIS.
                    linked_pairs.addAll( list_of_links );
                } else  if( list_of_links.size() == 1 ) { // No choice of links here so add it to the links.
                    linked_pairs.add( list_of_links.get(0) );
                } else {
                    // Only add the closest for now! TODO EXPLORE THIS.
                    addAllEqualToClosest( linked_pairs,list_of_links );
                    showAltDistances( list_of_links );
                }
            }

        }

        return processLinks(make_persistent, evaluate_quality, persist_links, linked_pairs);
    }

    /**
     * Adds all same distance as closest to the result set - some will be wrong but cannot differentiate.
     * @param results - the result set being returned by the query
     * @param list_of_links - the candidates for potential addition to the results
     */
    private void addAllEqualToClosest(List<Link> results, List<Link> list_of_links) {
        double closest_dist = list_of_links.get(0).getDistance();
        for( Link link : list_of_links ) {
            if( link.getDistance() == closest_dist ) {
                results.add( link );
            } else {
                return;
            }
        }
    }

    private void showAltDistances(List<Link> list_of_links) {
        StringBuilder sb = new StringBuilder();
        sb.append( "Dists: " );
        for( Link l : list_of_links) {
            sb.append( doesGTSayIsTrue(l) ? "TP:" : "FP:" );
            sb.append( l.getDistance() + "," );
        }
        System.out.println( sb );
    }


    private LinkageResult processLinks(MakePersistent make_persistent, boolean evaluate_quality, boolean persist_links, Iterable<Link> links) {
        long tp = 0;
        long fp = 0;

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now());

        if (persist_links) {
            for (Link linkage_says_true_link : links) {
                make_persistent.makePersistent(linkage_recipe, linkage_says_true_link);
            }
        }

        if (evaluate_quality) {
            for (Link linkage_says_true_link : links) {
                if (doesGTSayIsTrue(linkage_says_true_link)) {
                    tp++;
                } else {
                    fp++;
                }
            }
        }

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now());

        if (evaluate_quality) {
            LinkageQuality lq = getLinkageQuality(evaluate_quality, tp, fp);
            return new LinkageResult(lq,links);
        } else {
            return new LinkageResult(null, null); // TODO What should this return in this case?
        }
    }

    private LinkageQuality getLinkageQuality(boolean evaluate_quality, long tp, long fp) {
        long numberOfGroundTruthTrueLinks;
        System.out.println("Evaluating ground truth @ " + LocalDateTime.now());
        numberOfGroundTruthTrueLinks = linkage_recipe.getNumberOfGroundTruthTrueLinks();
        System.out.println("Number of GroundTruth true Links = " + numberOfGroundTruthTrueLinks);
        LinkageQuality lq = getLinkageQuality(evaluate_quality, numberOfGroundTruthTrueLinks, tp, fp);
        lq.print(System.out);
        return lq;
    }
}
