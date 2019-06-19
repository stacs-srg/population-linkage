package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.BruteForceLinker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
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

        IStoreReference link_id1 = link.getRole1().getRecordId();
        IStoreReference link_id2 = link.getRole2().getRecordId();

        return (link_id1.equals(id1) && link_id2.equals(id2));
    }

    class TestLinker extends BruteForceLinker {

        TestLinker(double threshold, final Metric<LXP> metric) {

            super(metric, 0);
            setThreshold(threshold);
        }

        @Override
        protected String getLinkType() {
            return "link type";
        }

        @Override
        protected String getProvenance() {
            return "provenance";
        }

        @Override
        protected String getRoleType1() {
            return "role1";
        }

        @Override
        protected String getRoleType2() {
            return "role2";
        }

        @Override
        public IStoreReference getIdentifier1(LXP record) throws PersistentObjectException {
            return record.getThisRef();
        }

        @Override
        public IStoreReference getIdentifier2(LXP record) throws PersistentObjectException {
            return record.getThisRef();
        }
    }
}
