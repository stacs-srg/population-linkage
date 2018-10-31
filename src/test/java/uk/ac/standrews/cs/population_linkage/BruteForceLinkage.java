package uk.ac.standrews.cs.population_linkage;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkage.BruteForceLinker;
import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;

import static junit.framework.TestCase.*;

public class BruteForceLinkage extends Linkage {

    @Override
    public Linker getLinker() {

        return new DummyBruteForceLinker();
    }

    @Override
    protected boolean equal(RecordPair pair1, RecordPair pair2) {

        // Since the linker may be configured for symmetric links, in which case record pair (a,b) should
        // not be considered if (b,a) is considered, the order of the records within a pair is significant.
        // So record pairs are only considered equal if they contain equal records in the same order.

        return pair1.record1.equals(pair2.record1) && pair1.record2.equals(pair2.record2);
    }

    @Test
    public void numberOfRecordPairsForSymmetricalLinksIsCorrect() throws InvalidWeightsException {

        // If links are symmetric then we don't want to consider record pair (a,b) as well as (b,a).

        setSymmetricalLinks();

        assertEquals(((birth_records.size() - 1) * birth_records.size()) / 2, getNumberOfPairs(linker, birth_records));
    }

    @Test
    public void recordPairsForSymmetricalLinksContainsExpectedValues() throws InvalidWeightsException {

        // If links are symmetric then we don't want to consider record pair (a,b) as well as (b,a).

        setSymmetricalLinks();

        assertTrue(containsPair(linker, birth1, birth2));
        assertTrue(containsPair(linker, birth2, birth3));

        assertFalse(containsPair(linker, birth2, birth1));
        assertFalse(containsPair(linker, birth3, birth1));
        assertFalse(containsPair(linker, birth2, birth2));
    }

    private void setSymmetricalLinks() {

        ((BruteForceLinker) linker).setSymmetricalLinks(true);
    }

    class DummyBruteForceLinker extends BruteForceLinker {

        DummyBruteForceLinker() {
            super(null, 0);
        }

        @Override
        protected String getLinkType() {
            return null;
        }

        @Override
        protected String getProvenance() {
            return null;
        }

        @Override
        protected String getRoleType1() {
            return null;
        }

        @Override
        protected String getRoleType2() {
            return null;
        }

        @Override
        protected String getIdentifier1(LXP record) {
            return null;
        }

        @Override
        protected String getIdentifier2(LXP record) {
            return null;
        }
    }
}
