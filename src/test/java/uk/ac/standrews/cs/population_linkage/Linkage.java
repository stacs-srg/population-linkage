package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.model.InvalidWeightsException;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public abstract class Linkage {

    final DummyLXP birth1 = new DummyLXP("john", "smith");
    final DummyLXP birth2 = new DummyLXP("janet", "smith");
    final DummyLXP birth3 = new DummyLXP("jane", "smyth");

    final DummyLXP death1 = new DummyLXP("janet", "smythe");
    final DummyLXP death2 = new DummyLXP("john", "stith");

    final List<LXP> birth_records = Arrays.asList(birth1, birth2, birth3);
    final List<LXP> death_records = Arrays.asList(death1, death2);

    Linker linker;

    protected abstract Linker getLinker();
    protected abstract boolean equal(RecordPair pair1, RecordPair pair2);

    @Before
    public void init() {

        linker = getLinker();
    }

    @Test
    public void numberOfRecordPairsForSingleDataSetIsCorrect() throws InvalidWeightsException {

        // By default, assume links are asymmetric, so we want to consider record pair (a,b) as well as (b,a), but not (a,a).
        assertEquals((birth_records.size() - 1) * birth_records.size(), getNumberOfPairs(linker, birth_records));
    }

    @Test
    public void recordPairsForSingleDataSetContainExpectedValues() throws InvalidWeightsException {

        // By default, assume links are asymmetric, so we want to consider record pair (a,b) as well as (b,a), but not (a,a).
        assertTrue(containsPair(linker, birth1, birth2));
        assertTrue(containsPair(linker, birth2, birth3));
        assertTrue(containsPair(linker, birth2, birth1));
        assertTrue(containsPair(linker, birth3, birth1));

        assertFalse(containsPair(linker, birth2, birth2));
    }

    @Test
    public void numberOfRecordPairsForTwoDataSetsIsCorrect() throws InvalidWeightsException {

        assertEquals(birth_records.size() * death_records.size(), getNumberOfPairs(linker, birth_records, death_records));
    }

    @Test
    public void recordPairsForTwoDataSetsContainExpectedValues() throws InvalidWeightsException {

        Iterable<RecordPair> pairs = linker.getRecordPairs(birth_records, death_records);

        assertTrue(containsPair(pairs, birth1, death2));
        assertTrue(containsPair(pairs, birth3, death1));
    }

    int getNumberOfPairs(Linker linker, List<LXP> records) throws InvalidWeightsException {

        return getNumberOfPairs(linker, records, records);
    }

    boolean containsPair(Iterable<RecordPair> record_pairs, DummyLXP record1, DummyLXP record2) {

        RecordPair record_pair = new RecordPair(record1, record2, -1);

        for (RecordPair p : record_pairs) {
            if (equal(p, record_pair))
                return true;
        }
        return false;
    }

    boolean containsPair(Linker linker, DummyLXP record1, DummyLXP record2) throws InvalidWeightsException {

        return containsPair(linker.getRecordPairs(birth_records), record1, record2);
    }

    private int getNumberOfPairs(Linker linker, List<LXP> records1, List<LXP> records2) throws InvalidWeightsException {

        int count = 0;
        for (RecordPair ignored : linker.getRecordPairs(records1, records2)) count++;
        return count;
    }

    void printPairs(Iterable<RecordPair> recordPairs) {

        for (RecordPair pair : recordPairs) {
            System.out.println(pair);
        }
        System.out.println("----------");
    }

    class DummyLXP extends StaticLXP {

        String rep = "";

        DummyLXP(String... values) {

            int i = 0;
            for (String value : values) {
                put(i++, value);
                rep += value + " ";
            }
        }

        @Override
        public Metadata getMetaData() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DummyLXP && ((DummyLXP) o).id == id;
        }

        public String toString() {
            return rep;
        }
    }
}
