package uk.ac.standrews.cs.population_linkage.model;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;

@SuppressWarnings("WeakerAccess")
public abstract class LinkageTest {

    private static final double DELTA = 0.0000001;

    // Link on first two fields, id from last field.
    final LXP birth1 = new DummyLXP("john", "smith", "1");
    final LXP birth2 = new DummyLXP("janet", "smith", "2");
    final LXP birth3 = new DummyLXP("jane", "smyth", "3");
    final LXP birth4 = new DummyLXP("janet", "smith", "4");

    final LXP death1 = new DummyLXP("janet", "smythe", "5");
    final LXP death2 = new DummyLXP("john", "stith", "6");
    final LXP death3 = new DummyLXP("jane", "smyth", "7");
    final LXP death4 = new DummyLXP("anthony", "aardvark", "8");
    final LXP death5 = new DummyLXP("tony", "armdadillo", "9");

    final NamedMetric<LXP> metric = new WeightedAverageLevenshtein<>(Arrays.asList(0, 1));

    final List<LXP> birth_records = Arrays.asList(birth1, birth2, birth3, birth4);
    final List<LXP> death_records = Arrays.asList(death1, death2, death3, death4, death5);

    Linker linker;

    protected abstract Linker getLinker();

    protected abstract boolean equal(RecordPair pair1, RecordPair pair2);

    protected abstract boolean equal(final Link link, final String id1, final String id2);

    @Before
    public void init() {

        linker = getLinker();
    }

    @Test
    public void distancesCorrect() {

        assertEquals(0.0, linker.getMetric().distance(birth1, birth1), DELTA);
        assertEquals(2.0, linker.getMetric().distance(birth1, birth2), DELTA);
        assertEquals(2.0, linker.getMetric().distance(birth1, birth3), DELTA);
        assertEquals(0.0, linker.getMetric().distance(birth2, birth2), DELTA);
        assertEquals(1.0, linker.getMetric().distance(birth2, birth3), DELTA);
        assertEquals(0.0, linker.getMetric().distance(birth3, birth3), DELTA);
    }

    @Test
    public void checkAllRecordPairsWithSingleDataSet() {

        linker.setThreshold(Double.MAX_VALUE);

        linker.addRecords(birth_records);

        // By default, assume links are asymmetric, so we want to consider record pair (a,b) as well as (b,a), but not (a,a).
        assertEquals((birth_records.size() - 1) * birth_records.size(), count(linker.getLinks()));

        assertTrue(containsPair(linker.getLinks(), birth1, birth2));
        assertTrue(containsPair(linker.getLinks(), birth1, birth3));
        assertTrue(containsPair(linker.getLinks(), birth1, birth4));
        assertTrue(containsPair(linker.getLinks(), birth2, birth1));
        assertTrue(containsPair(linker.getLinks(), birth2, birth3));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth3, birth1));
        assertTrue(containsPair(linker.getLinks(), birth3, birth2));
        assertTrue(containsPair(linker.getLinks(), birth3, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth1));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
        assertTrue(containsPair(linker.getLinks(), birth4, birth3));

        assertFalse(containsPair(linker.getLinks(), birth1, birth1));
    }

    @Test
    public void checkAllRecordPairsWithTwoDataSets2() {

        linker.setThreshold(Double.MAX_VALUE);
        linker.addRecords(birth_records, death_records);

        assertEquals(birth_records.size() * death_records.size(), count(linker.getLinks()));

        for (LXP birth_record : birth_records) {
            for (LXP death_record : death_records) {

                assertTrue(containsPair(linker.getLinks(), birth_record, death_record));
            }
        }

        assertFalse(containsPair(linker.getLinks(), birth1, birth1));
        assertFalse(containsPair(linker.getLinks(), death1, death1));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithTwoDataSets() {

        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records, death_records);

        assertEquals(1, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistanceNoughtPointFiveWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.5);
        linker.addRecords(birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceNoughtPointFiveWithTwoDataSets() {

        // "john smith" distance 0.5 from "john stith"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.5);
        linker.addRecords(birth_records, death_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth1, death2));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistanceOneWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"
        // "jane smyth" distance 1.0 from "janet smith"
        // "jane smyth" distance 1.0 from "janet smith"

        linker.setThreshold(1.0);
        linker.addRecords(birth_records);

        assertEquals(6, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth3));
        assertTrue(containsPair(linker.getLinks(), birth3, birth2));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
        assertTrue(containsPair(linker.getLinks(), birth3, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth3));
    }

    @Test
    public void checkRecordPairsWithinDistanceOneWithTwoDataSets() {

        // "john smith" distance 0.5 from "john stith"
        // "janet smith" distance 1 from "janet smythe"
        // "janet smith" distance 1 from "jane smyth"
        // "jane smyth" distance 1 from "janet smythe"
        // "jane smyth" distance 0 from "jane smyth"
        // "janet smith" distance 1 from "janet smythe"
        // "janet smith" distance 1 from "jane smyth"

        linker.setThreshold(1.0);
        linker.addRecords(birth_records, death_records);

        assertEquals(7, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth1, death2));
        assertTrue(containsPair(linker.getLinks(), birth2, death1));
        assertTrue(containsPair(linker.getLinks(), birth2, death3));
        assertTrue(containsPair(linker.getLinks(), birth3, death1));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
        assertTrue(containsPair(linker.getLinks(), birth4, death1));
        assertTrue(containsPair(linker.getLinks(), birth4, death3));
    }

    boolean containsPair(Iterable<Link> record_pairs, LXP record1, LXP record2) {

        for (Link p : record_pairs) {
            if (equal(p, linker.getIdentifier1(record1), linker.getIdentifier2(record2)))
                return true;
        }
        return false;
    }

    boolean containsPair(Links record_pairs, LXP record1, LXP record2) {

        for (Link p : record_pairs) {

            if (equal(p, linker.getIdentifier1(record1), linker.getIdentifier2(record2)))
                return true;
        }
        return false;
    }

    <T> int count(Iterable<T> elements) {

        int count = 0;
        for (T ignored : elements) {
            count++;
        }
        return count;
    }

    private int count(final Links links) {

        return links.size();
    }

    void printPairs(Iterable<RecordPair> recordPairs) {

        for (RecordPair pair : recordPairs) {
            System.out.println(pair);
        }
        System.out.println("----------");
    }

    class DummyLXP extends StaticLXP {

        String rep = "";
        int number_of_fields;

        DummyLXP(String... values) {

            number_of_fields = values.length;

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
        public int getFieldCount() {
            return number_of_fields;
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
