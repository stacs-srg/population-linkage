/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.LinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

public class BitBlasterLinkageRunnerSpecial extends LinkageRunner {

    @Override
    public LinkageRecipe getLinkageRecipe(String links_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return linkage_recipe;
    }

    public Linker getLinker(LinkageRecipe linkageRecipe, List<LXP> reference_points ) {
        Metric<LXP> compositeMetric = getCompositeMetric(linkageRecipe);
        return new SimilaritySearchLinker(getSearchFactory(compositeMetric,reference_points), compositeMetric, linkageRecipe.getThreshold(), getNumberOfProgressUpdates(),
                linkageRecipe.getLinkageType(), "threshold match at ", linkageRecipe.getStoredRole(), linkageRecipe.getQueryRole(), linkageRecipe::isViableLink, linkageRecipe);
    }

    public SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric, List<LXP> reference_points) {
        return new BitBlasterSearchStructureFactory<LXP>(composite_metric, reference_points);
    }

    @Override
    protected List<LXP> getReferencePoints() {

        List<LXP> candidates = filter(linkage_recipe.getLinkageFields().size(), LinkageRecipe.EVERYTHING, linkage_recipe.getStoredRecords(), linkage_recipe.getLinkageFields());
        List<LXP> result = BitBlasterSearchStructure.chooseRandomReferencePoints(candidates, LinkageConfig.numberOfROs);
        return result;
    }

    public LinkageResultSpecial link(MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persist_links) throws Exception {

        System.out.println("Adding records into linker @ " + LocalDateTime.now());

        ((SimilaritySearchLinker)linker).addRecords(linkage_recipe.getStoredRecords(), linkage_recipe.getQueryRecords(),getReferencePoints());

        System.out.println("Constructing link iterable @ " + LocalDateTime.now());

        Iterable<Link> all_links = linker.getLinks();

        List<Link> remaining_links = new ArrayList<>();
        List<Link> zero_distance_links = new ArrayList<>();

        filterDistanceLinks( all_links, zero_distance_links, remaining_links, linkage_recipe.getThreshold() );

        long tp = 0;
        long fp = 0;

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now());

        for (Link linkage_says_true_link : zero_distance_links) {

            if (persist_links) {
                make_persistent.makePersistent(linkage_recipe, linkage_says_true_link);
            }
            if (evaluate_quality) {
                if (doesGTSayIsTrue(linkage_says_true_link)) {
                    tp++;
                } else {
                    fp++;
                }
            }
        }

        System.out.println("Exiting persist and evaluate loop @ " + LocalDateTime.now());

        if (evaluate_quality) {
            System.out.println("Evaluating ground truth @ " + LocalDateTime.now());
            numberOfGroundTruthTrueLinks = linkage_recipe.getNumberOfGroundTruthTrueLinks();
            System.out.println("Number of GroundTruth true Links = " + numberOfGroundTruthTrueLinks);
            LinkageQuality lq = getLinkageQuality(evaluate_quality, numberOfGroundTruthTrueLinks, tp, fp);
            lq.print(System.out);
            return new LinkageResultSpecial(lq,zero_distance_links,remaining_links);
        } else {
            return new LinkageResultSpecial(null,zero_distance_links,remaining_links); // TODO What should this return in this case?
        }
    }

    private void filterDistanceLinks(Iterable<Link> all_links, List<Link> zero_distance_links, List<Link> remainder_links, double distance) {
        System.out.println( "Filtering links" );
        for( Link link : all_links ) {
            if( link.getDistance() < distance ) {
                zero_distance_links.add( link );
            } else {
                remainder_links.add( link );
            }
        }
    }
}
