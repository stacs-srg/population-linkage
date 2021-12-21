/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.interfaces.IRepository;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
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
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static uk.ac.standrews.cs.population_linkage.graph.NeoUtil.getByNeoId;
import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;
import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.passesFilter;
import static uk.ac.standrews.cs.population_linkage.supportClasses.DisplayMethods.*;

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
        List<Link> links_as_list = StreamSupport.stream(links.spliterator(), false).collect(Collectors.toList());

        return processLinks(make_persistent, evaluate_quality, persist_links, links_as_list );
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
                    Link match = list_of_links.get(0);
                    linked_pairs.add( match );
//                    print( match );
                } else {
                    // Only add the closest for now! TODO EXPLORE THIS.
                    addAllEqualToClosest(list_of_links, linked_pairs);
                    // showAltDistances( list_of_links );
                }
            }

        }

        return processLinks(make_persistent, evaluate_quality, persist_links, linked_pairs);
    }

    @Override
    protected LinkageResult linkLists2(MakePersistent make_persistent, boolean evaluateQuality, int numberOGroundTruthLinks, boolean persistLinks, boolean isIdentityLinkage, NeoDbCypherBridge bridge) throws Exception {
        System.out.println("Adding records into linker @ " + LocalDateTime.now());
        ((SimilaritySearchLinker) linker).addRecords(linkage_recipe.getStoredRecords(), linkage_recipe.getQueryRecords(), getReferencePoints());
        System.out.println("Constructing lists of lists @ " + LocalDateTime.now());
        System.out.println( "Threshold = " + linkage_recipe.getThreshold() );
//        Iterable<List<Link>> lol = linker.getListsOfLinks();
//        showlol( lol );
        List<Link> linked_pairs = processListsOfLists( linker.getListsOfLinks(),isIdentityLinkage );
        LinkageResult result = processLinks(make_persistent, true, false, linked_pairs); // params hacked TODO
        investigate( result.getLinks(),bridge );
        return result;
    }


    // Investigate non-links and why we missed them.
    private void investigate(Iterable<Link> links, NeoDbCypherBridge bridge) throws RepositoryException, BucketException {
        List<Relationship> gt_links = ((BirthDeathIdentityLinkageRecipe) linkage_recipe).getAllBirthDeathIdentityGTLinks(bridge);
        IRepository umea_repo = Store.getInstance().getRepository("umea"); // TODO HACK
        IBucket<Birth> births = umea_repo.getBucket("birth_records", Birth.class);
        IBucket<Death> deaths = umea_repo.getBucket("death_records", Death.class);

        for( Relationship gt_link : gt_links ) {
            long birth_neo_id = gt_link.startNodeId();
            long death_neo_id = gt_link.endNodeId();

            System.out.println( "Links not found by linker:" );
            if( notFound(links,birth_neo_id,death_neo_id) ) {

                Birth b = getByNeoId(birth_neo_id, births, bridge);
                Death d = getByNeoId(death_neo_id, deaths, bridge);

                long birth_storr_id = b.getId();
                long death_storr_id = d.getId();

                double distance = linkage_recipe.getCompositeMetric().distance(b, d);
                System.out.println( "No match for pair: " + birth_storr_id + " " + death_storr_id + " distance =" + distance );
                Birth birth = births.getObjectById(birth_storr_id);
                Death death = deaths.getObjectById(death_storr_id);
                LXP death_birth = linkage_recipe.convertToOtherRecordType(death);
                showBirth(birth);
                showDeath(death);
                showMatchFields(birth,death_birth,linkage_recipe.getLinkageFields());
                System.out.println("---");
            }
        }
    }

    private boolean notFound(Iterable<Link> links, long birth_neo_id, long death_neo_id) {
        for( Link link : links ) {
            if( link.getRecord1().getObjectId() == birth_neo_id || link.getRecord2().getObjectId() == death_neo_id ) {
                return false;
            }
        }
        return true;
    }

    private void showlol(Iterable<List<Link>> lol) {
        System.out.printf("List of lists:");
        int count = 0;
        for ( List<Link> ll : lol ) {
            if( ll.size() > 0 ) {
                System.out.println("entry " + count + " size: " + ll.size());
                System.out.println("Entries: ");
                for (Link l : ll) {
                    print(l);
                }
            }
            count++;
        }
        System.out.println( "Number of lists   = " + count);
    }

    /**
     * Adds all same distance as closest to the result set - some will be wrong but cannot differentiate.
     * @param list_of_links - the candidates for potential addition to the results
     * @param results - the result set being returned by the query
     */
    private void addAllEqualToClosest(List<Link> list_of_links, List<Link> results) {
        double closest_dist = list_of_links.get(0).getDistance();
        for( Link link : list_of_links ) {
            if( link.getDistance() == closest_dist ) {
                results.add( link );
//                print( link );
            } else {
                return;
            }
        }
    }

    private List<Link> processListsOfLists(Iterable<List<Link>> lists_of_list_of_links, boolean isIdentityLinkage) throws BucketException, RepositoryException {

        // TODO fix isIdentityLinkage if this works! - some code in other linkage linkLists

        List<Link> linked_pairs = new ArrayList<>();
        List<LXP> stored_matched = new ArrayList<>();

        // Link.getRecord1 is the stored record - Birth in test case - BirthBrideIdentity
        // Link.getRecord2 is the query record - Marriage in test

        Map<Long,List<Link>> map_of_links = new HashMap<>();
        for( List<Link> list_of_links : lists_of_list_of_links ) {
            if( list_of_links.size() != 0 ) {
                long query_id = list_of_links.get(0).getRecord2().getReferend().getId();
                map_of_links.put(query_id, list_of_links);
            }
        }

//        showMap( map_of_links );

        double max_t = linkage_recipe.getThreshold();
        int all_fields = linkage_recipe.getLinkageFields().size();
        final int half_fields = all_fields - (all_fields / 2) + 1;

        for (int required_fields = all_fields; required_fields >= half_fields; required_fields--) {
//            System.out.println( "Fields = " + required_fields );
            for (double threshold = 0.0; threshold <= max_t; threshold += (max_t / 10)) {
//                System.out.println( "Thresh = " + threshold );
                for (Long key : map_of_links.keySet()) {
                    List<Link> list_of_links = map_of_links.get(key);
//                    System.out.println( "Find closest in list of size " + list_of_links.size() );
                    int index = getClosestAcceptable(list_of_links, threshold, required_fields, map_of_links, stored_matched);
//                   System.out.println( " index = " + index );
                    if (index != -1) {
                        addAllEqualToClosest(list_of_links, index, linked_pairs, threshold, required_fields, map_of_links, stored_matched);
                    }
                }
            }
        }
        return linked_pairs;
    }

    private void showMap(Map<Long, List<Link>> map) {
        System.out.println( "Map size = " + map.keySet().size() );
        for( Long key : map.keySet() ) {
            List<Link> list = map.get(key);
            System.out.println( key + " size=" + list.size() + " entries: ");
            for( Link link : list ) {
                print( link );
            }
        }
    }

    private void print(Link link) { // ONLY works with birth marriage pairs write properly TODO!
        try {
            LXP ref1 = link.getRecord1().getReferend();
            LXP ref2 = link.getRecord2().getReferend();

            String role1 = link.getRole1();
            String role2 = link.getRole2();

            System.out.println( "Dist=" + link.getDistance() + " Role1 =" + role1 + " Role2 =" + role2 + " rec1id=" + ref1.getId() + " rec2id=" + ref2.getId() );
        } catch (Exception e) {
            System.out.println( "Exception");
        }
    }

    private int getClosestAcceptable(List<Link> list_of_links, double threshold, int fields, Map<Long, List<Link>> search_matched, List<LXP> stored_matched) throws BucketException, RepositoryException {
        int index = 0;
        for( Link link : list_of_links ) {

            if( acceptable( link,threshold,fields,stored_matched ) ) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private boolean acceptable( Link link, double threshold, int required_fields, List<LXP> stored_matched) throws BucketException, RepositoryException {
        LXP rec1 = link.getRecord1().getReferend();
        LXP rec2 = link.getRecord2().getReferend();

        boolean ltt = link.getDistance() <= threshold;
        boolean f1 = passesFilter(rec1, linkage_recipe.getLinkageFields(), required_fields);
        boolean f2 = passesFilter(rec2, linkage_recipe.getQueryMappingFields(), required_fields);  // Not really good enough - fields must match - see samePopulated below
        boolean smc = !stored_matched.contains(rec1);

        boolean result = ltt && f1 && f2 && smc;

        String status = result ? "accept" : "reject";
        if( doesGTSayIsTrue(link) ) {
            System.out.println( "accept= " + result + " **GT True**  " + "ltt=" + ltt + "  " + " filt_res=" + f1 + " " + " filt_q=" + f2 + " " + " not before=" + smc + " dist= " + link.getDistance() + " thresh= " + threshold + " fields=" + required_fields);
        } else {
            System.out.println( "accept= " + result + "   GT False    " + "ltt=" + ltt + "  " + " filt_res=" + f1 + " " + " filt_q=" + f2 + " " + " not before=" + smc + " dist= " + link.getDistance() + " thresh= " + threshold + " fields=" + required_fields);
        }
        return result;
    }

    public static boolean samePopulated(LXP record1, List<Integer> filterOn1, LXP record2, List<Integer> filterOn2, long reqPopulatedFields) {

        int same_populated = 0;

        for ( int i = 0; i < filterOn1.size(); i++ ) {

            String value1 = record1.getString(filterOn1.get(i)).toLowerCase().trim();
            String value2 = record2.getString(filterOn2.get(i)).toLowerCase().trim();

            boolean value1_present = ! ( value1.equals("") || value1.contains("missing") || value1.equals("--") || value1.equals("----") );
            boolean value2_present = ! ( value2.equals("") || value2.contains("missing") || value2.equals("--") || value2.equals("----") );

            if( value1_present && value2_present ) {
                same_populated++;
            }
        }

        return same_populated >= reqPopulatedFields;
    }



    private void addResult(Link match, List<Link> linked_pairs, Map<Long, List<Link>> map, List<LXP> stored_matched) throws BucketException, RepositoryException {
        linked_pairs.add( match );
//        System.out.println( ( doesGTSayIsTrue(match) ? "TP:" : "FP:" ) + match.getDistance() );
        stored_matched.add( match.getRecord1().getReferend() );
        map.remove( match.getRecord1().getReferend().getId() );
    }

    /**
     * Adds all same distance as closest to the result set - some will be wrong but cannot differentiate.
     * @param list_of_links - the candidates for potential addition to the results
     * @param results - the result set being returned by the query
     * @param stored_matched
     */
    private void addAllEqualToClosest(List<Link> list_of_links, int index, List<Link> results, double threshold, int required_fields, Map<Long, List<Link>> map, List<LXP> stored_matched) throws BucketException, RepositoryException {
        double closest_dist = list_of_links.get(index).getDistance();
        System.out.print( "** " + list_of_links.size() + " ** " );
        for( Link link : list_of_links.subList(index,list_of_links.size()) ) {
            if( link.getDistance() == closest_dist ) {
                if( acceptable( link,threshold,required_fields,stored_matched ) ) {
                    addResult(link, results, map, stored_matched);
               }
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


    private LinkageResult processLinks(MakePersistent make_persistent, boolean evaluate_quality, boolean persist_links, List<Link> links) {

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now());

        if (persist_links) {
            for (Link linkage_says_true_link : links) {
                make_persistent.makePersistent(linkage_recipe, linkage_says_true_link);
            }
        }

        long tp = 0;
        long fp = 0;

        if (evaluate_quality) {
//            for (Link linkage_says_true_link : links) {
//                if (doesGTSayIsTrue(linkage_says_true_link)) {
//                    tp++;
//                } else {
//                    fp++;
//                }
//            }

            tp = links.parallelStream().filter(l -> doesGTSayIsTrue(l)).count(); // This should be much faster since can run in parallel.
            fp = links.size() - tp;
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
