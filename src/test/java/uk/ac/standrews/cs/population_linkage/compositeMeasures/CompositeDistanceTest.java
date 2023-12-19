/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.LinkageTest;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;

@SuppressWarnings("WeakerAccess")
public class CompositeDistanceTest {

    private static final double DELTA = 0.0000001;

    // Link on first two fields, id from last field.
    final LXP birth1 = new LinkageTest.DummyLXP("john", "smith");
    final LXP birth2 = new LinkageTest.DummyLXP("janet", "smith");
    final LXP birth3 = new LinkageTest.DummyLXP("jane", "smyth");
    final LXP birth4 = new LinkageTest.DummyLXP("jane", "");

    final LXP death1 = new LinkageTest.DummyLXP("mrs", "janet", "smythe");
    final LXP death2 = new LinkageTest.DummyLXP("mr", "john", "stith");
    final LXP death3 = new LinkageTest.DummyLXP("dr", "jane", "smyth");
    final LXP death4 = new LinkageTest.DummyLXP("prof", "anthony", "aardvark");
    final LXP death5 = new LinkageTest.DummyLXP("", "tony", "armadillo");


    // To test: in isolation: different field indices; imputers; aggregators; cut-offs; normalisation.
    // different field comparators.
    //
    // In combination: normalisation vs cut-offs vs base measures


    @Before
    public void init() {

    }

    @Test
    public void basic() {

        final LXPMeasure measure = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());

        assertEquals(0.0, measure.distance(birth1, birth1), DELTA);
        assertEquals(4.0, measure.distance(birth1, birth2), DELTA);
        assertEquals(4.0, measure.distance(birth1, birth3), DELTA);
        assertEquals(0.0, measure.distance(birth2, birth2), DELTA);
        assertEquals(2.0, measure.distance(birth2, birth3), DELTA);
        assertEquals(0.0, measure.distance(birth3, birth3), DELTA);
    }

    @Test
    public void differentFields() {

        final LXPMeasure measure = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(1, 2), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());

        assertEquals(1.0, measure.distance(birth1, death2), DELTA);
    }

    @Test
    public void imputers() {

        final LXPMeasure measure1 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure2 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.ONE, new AggregatorSum());
        final LXPMeasure measure3 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.MAX_DOUBLE, new AggregatorSum());

        assertEquals(2.0, measure1.distance(birth2, birth4), DELTA);
        assertEquals(2.0, measure2.distance(birth2, birth4), DELTA);
        assertEquals(Double.MAX_VALUE, measure3.distance(birth2, birth4), DELTA);
    }

    @Test
    public void aggregators() {

        final LXPMeasure measure1 = new LXPMeasure(Arrays.asList(0, 1, 2), Arrays.asList(0, 1, 2), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure2 = new LXPMeasure(Arrays.asList(0, 1, 2), Arrays.asList(0, 1, 2), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorMean());

        List<Double> weights = Arrays.asList(0.2, 0.4, 0.4);
        final LXPMeasure measure3 = new LXPMeasure(Arrays.asList(0, 1, 2), Arrays.asList(0, 1, 2), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorMean(weights));
        final LXPMeasure measure4 = new LXPMeasure(Arrays.asList(0, 1, 2), Arrays.asList(0, 1, 2), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorMedian());

        // 2, 1, 1
        assertEquals(4.0, measure1.distance(death1, death3), DELTA);
        assertEquals(4.0/3.0, measure2.distance(death1, death3), DELTA);
        assertEquals(1.2, measure3.distance(death1, death3), DELTA);
        assertEquals(1.0, measure4.distance(death1, death3), DELTA);
    }

    @Test
    public void cutOffs() {

        final LXPMeasure measure1 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure2 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 0.1, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure3 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 1.5, false, Imputer.RECORD_MEAN, new AggregatorSum());

        assertEquals(2.0, measure1.distance(birth2, birth3), DELTA);
        assertEquals(0.2, measure2.distance(birth2, birth3), DELTA);
        assertEquals(2.0, measure3.distance(birth2, birth3), DELTA);

    }

    @Test
    public void normalisation() {

        final LXPMeasure measure1 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure2 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, true, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure3 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure4 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, Double.MAX_VALUE, true, Imputer.RECORD_MEAN, new AggregatorSum());

        assertEquals(2.0, measure1.distance(birth2, birth3), DELTA);
        assertEquals(0, measure2.distance(birth2, birth3), DELTA);

        // Jaccard distance "Janet" - "Jane" = (7-4)/7 = 3/7.
        // Jaccard distance "Smith" - "Smyth" = (8-4)/8 = 1/2.

        assertEquals(3.0/7.0 + 1.0/2.0, measure3.distance(birth2, birth3), DELTA);
        assertEquals(3.0/7.0 + 1.0/2.0, measure4.distance(birth2, birth3), DELTA);
    }

    @Test
    public void combinations() {

        final LXPMeasure measure1 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure2 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 0.1, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure3 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 1.5, false, Imputer.RECORD_MEAN, new AggregatorSum());

        final LXPMeasure measure4 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, Double.MAX_VALUE, true, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure5 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 0.1, true, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure6 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.LEVENSHTEIN, 1.5, true, Imputer.RECORD_MEAN, new AggregatorSum());

        final LXPMeasure measure7 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, Double.MAX_VALUE, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure8 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, 0.1, false, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure9 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, 1.5, false, Imputer.RECORD_MEAN, new AggregatorSum());

        final LXPMeasure measure10 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, Double.MAX_VALUE, true, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure11 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, 0.1, true, Imputer.RECORD_MEAN, new AggregatorSum());
        final LXPMeasure measure12 = new LXPMeasure(Arrays.asList(0, 1), Arrays.asList(0, 1), Constants.JACCARD, 1.5, true, Imputer.RECORD_MEAN, new AggregatorSum());

        assertEquals(2.0, measure1.distance(birth2, birth3), DELTA);
        assertEquals(0.2, measure2.distance(birth2, birth3), DELTA);
        assertEquals(2.0, measure3.distance(birth2, birth3), DELTA);

        assertEquals(0, measure4.distance(birth2, birth3), DELTA);
        assertEquals(2.0, measure5.distance(birth2, birth3), DELTA);
        assertEquals(4.0/3.0, measure6.distance(birth2, birth3), DELTA);

        // Jaccard distance "Janet" - "Jane" = (7-4)/7 = 3/7.
        // Jaccard distance "Smith" - "Smyth" = (8-4)/8 = 1/2.

        assertEquals(3.0/7.0 + 1.0/2.0, measure7.distance(birth2, birth3), DELTA);
        assertEquals(0.2, measure8.distance(birth2, birth3), DELTA);
        assertEquals(3.0/7.0 + 1.0/2.0, measure9.distance(birth2, birth3), DELTA);

        assertEquals(3.0/7.0 + 1.0/2.0, measure10.distance(birth2, birth3), DELTA);
        assertEquals(0.2, measure11.distance(birth2, birth3), DELTA);
        assertEquals(3.0/7.0 + 1.0/2.0, measure12.distance(birth2, birth3), DELTA);
    }
}
