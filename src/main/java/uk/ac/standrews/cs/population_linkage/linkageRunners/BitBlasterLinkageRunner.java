/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.ArrayList;
import java.util.List;

public class BitBlasterLinkageRunner extends LinkageRunner{

    @Override
    public LinkageRecipe getLinkageRecipe(String links_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return null;
    }

    @Override
    SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, LinkageConfig.numberOfROs);
    }

    protected ArrayList<LXP> filter(int requiredFields, int number_of_fields_required, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        int count_rejected = 0;
        int count_accepted = 0;
        linkageRecipe.setPreFilteringRequiredPopulatedLinkageFields(requiredFields);
        for (LXP record : records_to_filter) {
            if (linkageRecipe.passesFilter(record, linkageFields, requiredFields)) {
                filtered_source_records.add(record);
                count_accepted++;
            } else {
                count_rejected++;
            }
            if (filtered_source_records.size() >= number_of_fields_required) {
                break;
            }
        }
        System.out.println( "Filtering: accepted: " + count_accepted + " rejected: " + count_rejected + " from " + ( count_rejected + count_accepted ) );
        return filtered_source_records;
    }
}
