/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.LXPMetaData;
import uk.ac.standrews.cs.neoStorr.impl.LXPReference;
import uk.ac.standrews.cs.neoStorr.impl.StaticLXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.interfaces.IStoreReference;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;

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

    final LXPMeasure measure = new SumOfFieldDistances(Constants.LEVENSHTEIN, Arrays.asList(0, 1));

    final List<LXP> birth_records = Arrays.asList(birth1, birth2, birth3, birth4);
    final List<LXP> death_records = Arrays.asList(death1, death2, death3, death4, death5);

    Linker linker;

    static int lxp_id = 0;

    protected abstract Linker getLinker();

    protected abstract boolean equal(final Link link, final IStoreReference<LXP> id1, final IStoreReference<LXP> id2);

    @Before
    public void init() {

        linker = getLinker();
    }

    @Test
    public void distancesCorrect() {

        assertEquals(0.0, linker.getMeasure().distance(birth1, birth1), DELTA);
        assertEquals(4.0, linker.getMeasure().distance(birth1, birth2), DELTA);
        assertEquals(4.0, linker.getMeasure().distance(birth1, birth3), DELTA);
        assertEquals(0.0, linker.getMeasure().distance(birth2, birth2), DELTA);
        assertEquals(2.0, linker.getMeasure().distance(birth2, birth3), DELTA);
        assertEquals(0.0, linker.getMeasure().distance(birth3, birth3), DELTA);
    }

    @Test
    public void checkAllRecordPairsWithSingleDataSet() throws Exception {

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
    public void checkAllRecordPairsWithTwoDataSets() throws Exception {

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
    public void checkRecordPairsWithinDistanceZeroWithSingleDataSet() throws Exception {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records, birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithTwoDataSets() throws Exception {

        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.0);
        linker.addRecords(birth_records, death_records);

        assertEquals(1, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistance035WithSingleDataSet() throws Exception {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.35);
        linker.addRecords(birth_records, birth_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistance1WithTwoDataSets() throws Exception {

        // "john smith" distance 1.0 from "john stith"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(1.0);
        linker.addRecords(birth_records, death_records);

        assertEquals(2, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth1, death2));
        assertTrue(containsPair(linker.getLinks(), birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistance4WithSingleDataSet() throws Exception {

        // "janet smith" distance 0 from "janet smith"
        // "john smith" distance 4.0 from "janet smith"
        // "janet smith" distance 2.0 from "jane smyth"

        linker.setThreshold(4.0);
        linker.addRecords(birth_records, birth_records);

        assertEquals(12, count(linker.getLinks()));
        assertTrue(containsPair(linker.getLinks(), birth2, birth3));
        assertTrue(containsPair(linker.getLinks(), birth3, birth2));
        assertTrue(containsPair(linker.getLinks(), birth3, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth3));
        assertTrue(containsPair(linker.getLinks(), birth1, birth2));
        assertTrue(containsPair(linker.getLinks(), birth2, birth1));
        assertTrue(containsPair(linker.getLinks(), birth1, birth3));
        assertTrue(containsPair(linker.getLinks(), birth3, birth1));
        assertTrue(containsPair(linker.getLinks(), birth1, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth1));
        assertTrue(containsPair(linker.getLinks(), birth2, birth4));
        assertTrue(containsPair(linker.getLinks(), birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistance2WithTwoDataSets() throws Exception {

        // "john smith" distance 1.0 from "john stith"
        // "janet smith" distance 2.0 from "janet smythe"
        // "janet smith" distance 2.0 from "jane smyth"
        // "jane smyth" distance 2.0 from "janet smythe"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(2.0);
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

    boolean containsPair(final Iterable<Link> record_pairs, final LXP record1, final LXP record2) throws BucketException, RepositoryException {

        for (final Link link : record_pairs) {

            final LXP link_record1 = link.getRecord1().getReferend();
            final LXP link_record2 = link.getRecord2().getReferend();

            if (link_record1.equals(record1) && link_record2.equals(record2) || link_record1.equals(record2) && link_record2.equals(record1))
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

    static class DummyLXP extends StaticLXP {

        String rep = "";
        int number_of_fields;
        IStoreReference store_reference = null;

        DummyLXP(String... values) {

            number_of_fields = values.length;

            int i = 0;
            for (String value : values) {
                put(i++, value);
                rep += value + " ";
            }
        }

        @Override
        public LXPMetaData getMetaData() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DummyLXP && ((DummyLXP) o).getId() == getId();
        }

        @Override
        public IStoreReference getThisRef() {

            if (store_reference == null) {
                final LXP this_lxp = this;
                store_reference = new LXPReference("dummy-repo", "dummy-bucket", lxp_id++) {
                    public LXP getReferend() {
                        return this_lxp;
                    }
                };
            }
            return store_reference;
        }

        public String toString() {
            return rep;
        }
    }
}
