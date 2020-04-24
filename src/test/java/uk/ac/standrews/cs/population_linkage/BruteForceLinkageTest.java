/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkers.BruteForceLinker;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BruteForceLinkageTest extends LinkageTest {

    @Override
    public Linker getLinker() {

        return new TestLinker(Double.MAX_VALUE, metric);
    }

    @Override
    protected boolean equal(final Link link, final IStoreReference id1, final IStoreReference id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        IStoreReference link_id1 = link.getRecord1();
        IStoreReference link_id2 = link.getRecord2();

        return (link_id1.equals(id1) && link_id2.equals(id2) || link_id2.equals(id1) && link_id1.equals(id2) );
    }

    class TestLinker extends BruteForceLinker {

        TestLinker(double threshold, final Metric<LXP> metric) {

            super(metric, 0.67, 0, "link type", "provenance", "role1", "role2", (r)-> true);
            setThreshold(threshold);
        }

    }
}
