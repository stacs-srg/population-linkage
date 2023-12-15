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
package uk.ac.standrews.cs.population_linkage.FelligiSunter;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static uk.ac.standrews.cs.population_linkage.supportClasses.Constants.TRUE_METRICS;

// copies from characterisation MetricSpaceChecks

public class MetricSpaceChecks {

    private static final int DUMP_COUNT_INTERVAL = 1000000;
    private static final long SEED = 34553543456223L;
    private static final double DELTA = 0.0000001;

    private final List<LXPMeasure> combined_metrics;

    private MetricSpaceChecks() {

        combined_metrics = getCombinedMeasures();
    }

    public void run() throws Exception {

        checkTriangleInequality();
    }

    private List<LXPMeasure> getCombinedMeasures() {

        List<LXPMeasure> result = new ArrayList<>();

        for (StringMeasure base_measure : TRUE_METRICS) {
            result.add(new LXPMeasure(BirthSiblingLinkageRecipe.LINKAGE_FIELDS, BirthSiblingLinkageRecipe.LINKAGE_FIELDS, base_measure));
        }
        return result;
    }

    private void checkTriangleInequality() {

        Random random = new Random(SEED);
        try (RecordRepository record_repository = new RecordRepository(Umea.REPOSITORY_NAME)) {

            final List<LXP> birth_records = new ArrayList<>();
            for (Birth birth : record_repository.getBirths()) {
                birth_records.add(birth);
            }

            long counter = 0;

            //noinspection InfiniteLoopStatement
            while (true) {

                int size = birth_records.size();

                LXP b1 = birth_records.get(random.nextInt(size));
                LXP b2 = birth_records.get(random.nextInt(size));
                LXP b3 = birth_records.get(random.nextInt(size));

                for (LXPMeasure metric : combined_metrics) {

                    double distance1 = metric.distance(b1, b2);
                    double distance2 = metric.distance(b1, b3);
                    double distance3 = metric.distance(b2, b3);

                    if (violatesTriangleInequality(distance1, distance2, distance3)) {

                        System.out.println("violation of triangle inequality for " + metric);
                        System.out.println(b1);
                        System.out.println(b2);
                        System.out.println(b3);
                        System.out.println(distance1);
                        System.out.println(distance2);
                        System.out.println(distance3);
                        System.out.println();
                    }
                }

                if (counter % DUMP_COUNT_INTERVAL == 0) {
                    System.out.println("checked: " + counter);
                }

                counter++;
            }
        }
    }

    private boolean violatesTriangleInequality(final double distance1, final double distance2, final double distance3) {

        return distance1 > distance2 + distance3 + DELTA || distance2 > distance1 + distance3 + DELTA || distance3 > distance1 + distance2 + DELTA;
    }

    public static void main(String[] args) throws Exception {

        new MetricSpaceChecks().checkTriangleInequality();
    }
}
