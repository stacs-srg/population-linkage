/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.LXPMetadata;
import uk.ac.standrews.cs.storr.impl.LXPReference;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.Levenshtein;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.ArrayList;
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
    final LXP death5 = new DummyLXP("tony", "armadillo", "9");

    final Metric<LXP> metric = new Sigma(new Levenshtein(), Arrays.asList(0, 1), 0);

    final List<LXP> birth_records = Arrays.asList(birth1, birth2, birth3, birth4);
    final List<LXP> death_records = Arrays.asList(death1, death2, death3, death4, death5);

    Linker linker;

    static int lxp_id = 0;

    protected abstract Linker getLinker();

    protected abstract boolean equal(final Link link, final IStoreReference id1, final IStoreReference id2);

    @Before
    public void init() {

        linker = getLinker();
    }

    @Test
    public void distancesCorrect() {

        assertEquals(0.0, linker.getMetric().distance(birth1, birth1), DELTA);
        assertEquals(0.4444444, linker.getMetric().distance(birth1, birth2), DELTA);
        assertEquals(0.5555555, linker.getMetric().distance(birth1, birth3), DELTA);
        assertEquals(0.0, linker.getMetric().distance(birth2, birth2), DELTA);
        assertEquals(0.5, linker.getMetric().distance(birth2, birth3), DELTA);
        assertEquals(0.0, linker.getMetric().distance(birth3, birth3), DELTA);
    }

    @Test
    public void checkAllRecordPairsWithSingleDataSet() throws PersistentObjectException {

        linker.setThreshold(Double.MAX_VALUE);

        linker.addRecords(birth_records, birth_records);

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
        assertFalse(containsPair(linker.getLinks(), birth2, birth2));
        assertFalse(containsPair(linker.getLinks(), birth3, birth3));
        assertFalse(containsPair(linker.getLinks(), birth4, birth4));
    }

    @Test
    public void checkAllRecordPairsWithTwoDataSets() throws PersistentObjectException {

        linker.setThreshold(Double.MAX_VALUE);
        linker.addRecords(birth_records, death_records);

        assertEquals(birth_records.size() * death_records.size(), count(linker.getLinks()));

        for (LXP birth_record : birth_records) {
            for (LXP death_record : death_records) {

                Iterable<Link> links = linker.getLinks();
                boolean condition = containsPair(links, birth_record, death_record);
                assertTrue(condition);
            }
        }

        assertFalse(containsPair(linker.getLinks(), birth1, birth1));
        assertFalse(containsPair(linker.getLinks(), death1, death1));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithSingleDataSet() throws PersistentObjectException {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records, birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithTwoDataSets() throws PersistentObjectException {

        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records, death_records);

        assertEquals(1, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistance035WithSingleDataSet() throws PersistentObjectException {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.35);
        linker.addRecords(birth_records, birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistance035WithTwoDataSets() throws PersistentObjectException {

        // "john smith" distance 0.333 from "john stith"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.35);
        linker.addRecords(birth_records, death_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth1, death2));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistance05WithSingleDataSet() throws PersistentObjectException {

        // "janet smith" distance 0 from "janet smith"
        // "john smith" distance 0.444 from "janet smith"
        // "janet smith" distance 0.5 from "jane smyth"

        linker.setThreshold(0.5);
        linker.addRecords(birth_records, birth_records);

        assertEquals(10, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth3));
        assertTrue(containsPair(linker.getLinks(), birth3, birth2));
        assertTrue(containsPair(linker.getLinks(), birth3, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth3));
        assertTrue(containsPair(linker.getLinks(), birth1, birth2));
        assertTrue(containsPair(linker.getLinks(), birth2, birth1));
        assertTrue(containsPair(linker.getLinks(), birth1, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth1));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistance05WithTwoDataSets() throws PersistentObjectException {

        // "john smith" distance 0.333 from "john stith"
        // "janet smith" distance 0.4 from "janet smythe"
        // "janet smith" distance 0.5 from "jane smyth"
        // "jane smyth" distance 0.5 from "janet smythe"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.5);
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

    boolean containsPair(Iterable<Link> record_pairs, LXP record1, LXP record2) throws PersistentObjectException {

        for (Link p : record_pairs) {
            final IStoreReference identifier1 = linker.getIdentifier1(record1);
            final IStoreReference identifier2 = linker.getIdentifier2(record2);
            if (equal(p, identifier1, identifier2))
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

    void printPairs(Iterable<RecordPair> recordPairs) {

        for (RecordPair pair : recordPairs) {
            System.out.println(pair);
        }
        System.out.println("----------");
    }

    List<Link> getPairs(Iterable<Link> recordPairs) {

        List<Link> result = new ArrayList<>();
        for (Link pair : recordPairs) {
            result.add(pair);
        }
        return result;
    }

    class DummyLXP extends StaticLXP {

        String rep = "";
        int number_of_fields;
        IStoreReference store_reference = new LXPReference(null, "dummy-repo", "dummy-bucket", lxp_id++);

        DummyLXP(String... values) {

            number_of_fields = values.length;

            int i = 0;
            for (String value : values) {
                put(i++, value);
                rep += value + " ";
            }
        }

        @Override
        public LXPMetadata getMetaData() {
            return null;
        }

        @Override
        public int getFieldCount() {
            return number_of_fields;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DummyLXP && ((DummyLXP) o).getId() == getId();
        }

        @Override
        public IStoreReference getThisRef() {
            return store_reference;
        }

        public String toString() {
            return rep;
        }
    }
}
