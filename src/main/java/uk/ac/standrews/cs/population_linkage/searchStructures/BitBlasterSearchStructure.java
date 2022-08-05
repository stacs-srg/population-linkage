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
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;
import uk.al_richard.metricbitblaster.production.ParallelBitBlaster2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BitBlasterSearchStructure<T> implements SearchStructure<T> {

    private static final int DEFAULT_NUMBER_OF_REFERENCE_POINTS = 70;
    private static long SEED = 34258723425L;
    private ParallelBitBlaster2<T> bit_blaster;

    public BitBlasterSearchStructure(Measure<T> measure, Iterable<T> data) {
        this(measure, data, DEFAULT_NUMBER_OF_REFERENCE_POINTS);
    }

    public BitBlasterSearchStructure(Measure<T> measure, Iterable<T> data, int number_of_reference_objects) {

        List<T> copy_of_data = copyData(data);

        // Keep repeating with fewer reference objects if we cannot initialise bitblaster
        while (number_of_reference_objects >= 20) {
            int maxTries = 5;

            // Try several times with different seeds
            for (int tries = 0; tries < maxTries; tries++) {
                try {
                    init(measure, copy_of_data, chooseRandomReferencePoints(copy_of_data, number_of_reference_objects));
                    return;

                } catch (Exception e) {
                    SEED = SEED * 17 + 23; // These magic numbers were carefully chosen by Prof. al
                    System.out.println("Initilisation exception - trying again with different reference points - new seed: " + SEED);
                }
            }
            // Reduce number of ros if we cannot initialse
            number_of_reference_objects = number_of_reference_objects - 10;
            System.out.println("Reducing number of reference points to: " + number_of_reference_objects);
        }

        throw new RuntimeException("Failed to initialise BitBlaster");
    }

    public BitBlasterSearchStructure(Measure<T> measure, Iterable<T> data, List<T> reference_objects) {

        try {
            init(measure, copyData(data), reference_objects);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <X> List<X> copyData(final Iterable<X> data) {

        List<X> copy_of_data = new ArrayList<>();

        for (X x : data) {
            copy_of_data.add(x);
        }
        return copy_of_data;
    }

    public static <X> List<X> chooseRandomReferencePoints(final List<X> data, final int number_of_reference_objects) {

        Random random = new Random(SEED);
        List<X> reference_points = new ArrayList<>();

        if (number_of_reference_objects >= data.size()) {
            return data;
        }

        while (reference_points.size() < number_of_reference_objects) {
            X item = data.get(random.nextInt(data.size()));
            if (!reference_points.contains(item)) {
                reference_points.add(item);
            }
        }

        return reference_points;
    }

    public void terminate() {
        bit_blaster.terminate();
    }

    private void init(final Measure<T> distance_measure, final List<T> data, final List<T> reference_objects) throws Exception {

        boolean fourPoint = distance_measure.getMeasureName().equals(Constants.JENSEN_SHANNON.getMeasureName());

        bit_blaster = new ParallelBitBlaster2<>(distance_measure::distance, reference_objects, data, 2,
                Runtime.getRuntime().availableProcessors(), fourPoint, true);
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        try {
            return convertDataDistanceList(bit_blaster.rangeSearch(record, threshold));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<DataDistance<T>> convertDataDistanceList(List<uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance<T>> list) {

        return list.stream().map(x -> new DataDistance<>(x.value, x.distance)).collect(Collectors.toList());
    }
}
