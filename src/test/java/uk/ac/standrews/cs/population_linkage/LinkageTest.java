package uk.ac.standrews.cs.population_linkage;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.linkage.BirthLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Metadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;

@SuppressWarnings("WeakerAccess")
public abstract class LinkageTest {

    private static final double DELTA = 0.0000001;

    final LXP birth1 = new DummyLXP("john", "smith");
    final LXP birth2 = new DummyLXP("janet", "smith");
    final LXP birth3 = new DummyLXP("jane", "smyth");
    final LXP birth4 = new DummyLXP("janet", "smith");

    final LXP death1 = new DummyLXP("janet", "smythe");
    final LXP death2 = new DummyLXP("john", "stith");
    final LXP death3 = new DummyLXP("jane", "smyth");
    final LXP death4 = new DummyLXP("anthony", "aardvark");
    final LXP death5 = new DummyLXP("tony", "armdadillo");

    final List<LXP> birth_records = Arrays.asList(birth1, birth2, birth3, birth4);
    final List<LXP> death_records = Arrays.asList(death1, death2, death3, death4, death5);

    Linker linker;

    protected abstract Linker getLinker();

    protected abstract boolean equal(RecordPair pair1, RecordPair pair2);

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

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records);

        // By default, assume links are asymmetric, so we want to consider record pair (a,b) as well as (b,a), but not (a,a).
        assertEquals((birth_records.size() - 1) * birth_records.size(), count(pairs));

        assertTrue(containsPair(pairs, birth1, birth2));
        assertTrue(containsPair(pairs, birth1, birth3));
        assertTrue(containsPair(pairs, birth1, birth4));
        assertTrue(containsPair(pairs, birth2, birth1));
        assertTrue(containsPair(pairs, birth2, birth3));
        assertTrue(containsPair(pairs, birth2, birth4));
        assertTrue(containsPair(pairs, birth3, birth1));
        assertTrue(containsPair(pairs, birth3, birth2));
        assertTrue(containsPair(pairs, birth3, birth4));
        assertTrue(containsPair(pairs, birth4, birth1));
        assertTrue(containsPair(pairs, birth4, birth2));
        assertTrue(containsPair(pairs, birth4, birth3));

        assertFalse(containsPair(pairs, birth1, birth1));
    }

    @Test
    public void checkAllRecordPairsWithTwoDataSets() {

        linker.setThreshold(Double.MAX_VALUE);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records, death_records);

        assertEquals(birth_records.size() * death_records.size(), count(pairs));

        for (LXP birth_record : birth_records) {
            for (LXP death_record : death_records) {

                assertTrue(containsPair(pairs, birth_record, death_record));
            }
        }

        assertFalse(containsPair(pairs, birth1, birth1));
        assertFalse(containsPair(pairs, death1, death1));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.0);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records);

        assertEquals(2, count(pairs));
        assertTrue(containsPair(pairs, birth2, birth4));
        assertTrue(containsPair(pairs, birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceZeroWithTwoDataSets() {

        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.0);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records, death_records);

        assertEquals(1, count(pairs));
        assertTrue(containsPair(pairs, birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistanceNoughtPointFiveWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"

        linker.setThreshold(0.5);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records);

        assertEquals(2, count(pairs));
        assertTrue(containsPair(pairs, birth2, birth4));
        assertTrue(containsPair(pairs, birth4, birth2));
    }

    @Test
    public void checkRecordPairsWithinDistanceNoughtPointFiveWithTwoDataSets() {

        // "john smith" distance 0.5 from "john stith"
        // "jane smyth" distance 0 from "jane smyth"

        linker.setThreshold(0.5);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records, death_records);

        assertEquals(2, count(pairs));
        assertTrue(containsPair(pairs, birth1, death2));
        assertTrue(containsPair(pairs, birth3, death3));
    }

    @Test
    public void checkRecordPairsWithinDistanceOneWithSingleDataSet() {

        // "janet smith" distance 0 from "janet smith"
        // "jane smyth" distance 1.0 from "janet smith"
        // "jane smyth" distance 1.0 from "janet smith"

        linker.setThreshold(1.0);

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records);

        assertEquals(6, count(pairs));
        assertTrue(containsPair(pairs, birth2, birth3));
        assertTrue(containsPair(pairs, birth3, birth2));
        assertTrue(containsPair(pairs, birth2, birth4));
        assertTrue(containsPair(pairs, birth4, birth2));
        assertTrue(containsPair(pairs, birth3, birth4));
        assertTrue(containsPair(pairs, birth4, birth3));
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

        Iterable<RecordPair> pairs = linker.getMatchingRecordPairs(birth_records, death_records);

        assertEquals(7, count(pairs));
        assertTrue(containsPair(pairs, birth1, death2));
        assertTrue(containsPair(pairs, birth2, death1));
        assertTrue(containsPair(pairs, birth2, death3));
        assertTrue(containsPair(pairs, birth3, death1));
        assertTrue(containsPair(pairs, birth3, death3));
        assertTrue(containsPair(pairs, birth4, death1));
        assertTrue(containsPair(pairs, birth4, death3));
    }

    boolean containsPair(Iterable<RecordPair> record_pairs, LXP record1, LXP record2) {

        RecordPair record_pair = new RecordPair(record1, record2, -1);

        for (RecordPair p : record_pairs) {
            if (equal(p, record_pair))
                return true;
        }
        return false;
    }

    int count(Iterable<RecordPair> record_pairs) {

        int count = 0;
        for (RecordPair ignored : record_pairs) {
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

    class BirthLXP extends StaticLXP {

        String rep = "";

        BirthLXP(String father_forename, String father_surname, String mother_forename, String mother_maiden_surname,
                 String parents_place_of_marriage, String parents_day_of_marriage, String parents_month_of_marriage, String parents_year_of_marriage) {

            put(BirthLinkageSubRecord.FATHER_FORENAME, father_forename);
            put(BirthLinkageSubRecord.FATHER_SURNAME, father_surname);
            put(BirthLinkageSubRecord.MOTHER_FORENAME, mother_forename);
            put(BirthLinkageSubRecord.MOTHER_MAIDEN_SURNAME, mother_maiden_surname);
            put(BirthLinkageSubRecord.PARENTS_PLACE_OF_MARRIAGE, parents_place_of_marriage);
            put(BirthLinkageSubRecord.PARENTS_DAY_OF_MARRIAGE, parents_day_of_marriage);
            put(BirthLinkageSubRecord.PARENTS_MONTH_OF_MARRIAGE, parents_month_of_marriage);
            put(BirthLinkageSubRecord.PARENTS_YEAR_OF_MARRIAGE, parents_year_of_marriage);

            rep = mother_forename + " " + mother_maiden_surname + " " + father_forename + " " + father_surname;
        }

        @Override
        public Metadata getMetaData() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof BirthLXP && ((BirthLXP) o).id == id;
        }

        public String toString() {
            return rep;
        }
    }
}
