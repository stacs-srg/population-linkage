/*
 * ***********************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 * Copyright 2019 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************
 */
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BitBlasterLinkageRunner extends LinkageRunner{

    @Override
    public LinkageRecipe getLinkageRecipe(String links_persistent_name, String source_repository_name, String results_repository_name, RecordRepository record_repository) {
        return null;
    }

    @Override
    SearchStructureFactory<LXP> getSearchFactory(Metric<LXP> composite_metric) {
        return new BitBlasterSearchStructureFactory<>(composite_metric, LinkageConfig.numberOfROs);
    }
}
