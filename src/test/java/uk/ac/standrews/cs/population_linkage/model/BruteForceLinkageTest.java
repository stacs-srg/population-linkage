package uk.ac.standrews.cs.population_linkage.model;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkage.BruteForceLinker;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Arrays;

import static junit.framework.TestCase.*;

public class BruteForceLinkageTest extends LinkageTest {

    @Override
    public Linker getLinker() {

        NamedMetric<LXP> metric = new WeightedAverageLevenshtein<>(Arrays.asList(0, 1));

        return new TestLinker(Double.MAX_VALUE, metric);
    }

    @Override
    protected boolean equal(RecordPair pair1, RecordPair pair2) {

        // Since the linker may be configured for symmetric links, in which case record pair (a,b) should
        // not be considered if (b,a) is considered, the order of the records within a pair is significant.
        // So record pairs are only considered equal if they contain equal records in the same order.

        return pair1.record1.equals(pair2.record1) && pair1.record2.equals(pair2.record2);
    }

    @Override
    protected boolean equal(final Link link, final String id1, final String id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        String link_id1 = link.getRole1().getRecordId();
        String link_id2 = link.getRole2().getRecordId();

        return (link_id1.equals(id1) && link_id2.equals(id2));
    }

    @Test
    public void checkAllRecordPairsWithSymmetricalLinks() {

        // If links are symmetric then we don't want to consider record pair (a,b) as well as (b,a).

        ((BruteForceLinker) linker).setSymmetricalLinks(true);
        linker.setThreshold(Double.MAX_VALUE);

        linker.addRecords(birth_records);
        Links links = linker.link();

        assertEquals(((birth_records.size() - 1) * birth_records.size()) / 2, links.size());

        assertTrue(containsPair(links, birth1, birth2));
        assertTrue(containsPair(links, birth1, birth3));
        assertTrue(containsPair(links, birth2, birth3));

        assertFalse(containsPair(links, birth2, birth1));
        assertFalse(containsPair(links, birth3, birth1));
        assertFalse(containsPair(links, birth2, birth2));
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
