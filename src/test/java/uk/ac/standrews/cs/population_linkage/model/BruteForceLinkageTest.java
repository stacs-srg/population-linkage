package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.BruteForceLinker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class BruteForceLinkageTest extends LinkageTest {

    @Override
    public Linker getLinker() {

        return new TestLinker(Double.MAX_VALUE, metric);
    }

    @Override
    protected boolean equal(final Link link, final String id1, final String id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        String link_id1 = link.getRole1().getRecordId();
        String link_id2 = link.getRole2().getRecordId();

        return (link_id1.equals(id1) && link_id2.equals(id2));
    }

    class TestLinker extends BruteForceLinker {

        TestLinker(double threshold, final NamedMetric<LXP> metric) {

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
        public String getIdentifier1(LXP record) {
            return record.getString(2);
        }

        @Override
        public String getIdentifier2(LXP record) {
            return record.getString(2);
        }
    }
}
