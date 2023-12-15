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
package uk.ac.standrews.cs.population_linkage.profiling.umea;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesIgnoringMissingFields;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Samples record pair distances for birth sibling linkage and various distance measures.
 */
public class DistanceProfilingBirthSibling {

    // Assume that there won't be any record pairs with a greater distance than this, even for non-normalised measures,
    // so any results involving a greater distance can be ignored as they're to do with missing fields.
    private static final double MAX_PLAUSIBLE_NON_NORMALISED_DISTANCE = 1000d;
    public static final int NUMBER_OF_RECORDS_TO_SAMPLE = 1000;

    protected final String repo_name;

    private DistanceProfilingBirthSibling(final String repo_name) {

        this.repo_name = repo_name;
    }

    public void run() {

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final List<LXP> birth_records = Utilities.permute(Utilities.getBirthRecords(record_repository)).subList(0, NUMBER_OF_RECORDS_TO_SAMPLE);

        for (final StringMeasure measure : Constants.BASE_MEASURES) {
            sampleDistances(birth_records, new LXPMeasure(BirthSiblingLinkageRecipe.LINKAGE_FIELDS, BirthSiblingLinkageRecipe.LINKAGE_FIELDS, measure));
        }
    }

    private void sampleDistances(final List<LXP> birth_records, final LXPMeasure measure) {

        double max = 0d;
        long count = 0;
        double sum = 0d;

        for (final LXP record1 : birth_records) {
            for (final LXP record2 : birth_records) {
                final double distance = measure.distance(record1, record2);

                if (distance < MAX_PLAUSIBLE_NON_NORMALISED_DISTANCE) {

                    sum += distance;
                    count++;

                    max = Math.max(max, distance);
                }
            }
        }

        final double mean = sum / count;

        System.out.println(measure + ": " + max + ", " + mean);
    }

    public static void main(String[] args) {

        new DistanceProfilingBirthSibling(Umea.REPOSITORY_NAME).run();
    }
}
