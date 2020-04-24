/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkers.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public abstract class SimilaritySearchLinkageTest extends LinkageTest {

    @Override
    protected boolean equal(final Link link, final IStoreReference id1, final IStoreReference id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        IStoreReference link_id1 = link.getRecord1();
        IStoreReference link_id2 = link.getRecord2();

        return (link_id1.equals(id1) && link_id2.equals(id2)) || (link_id1.equals(id2) && link_id2.equals(id1));
    }

     class TestLinker extends SimilaritySearchLinker {

        TestLinker(SearchStructureFactory<LXP> search_structure_factory, double threshold, Metric<LXP> metric, int number_of_progress_updates) {

            super(search_structure_factory, metric, 0.67, number_of_progress_updates, "link type", "provenance", "role1", "role2", (r)-> true, null);
            setThreshold(threshold);
        }
    }
}
