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
package uk.ac.standrews.cs.population_linkage.characterisation;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MeasureCosts {

    protected final String repo_name;

    private MeasureCosts(String repo_name) {

        this.repo_name = repo_name;
    }

    public void run() {

        final RecordRepository record_repository = new RecordRepository(repo_name);

        final List<LXP> birth_records = Utilities.permute(Utilities.getBirthRecords(record_repository)).subList(0, 1000);

        for (StringMeasure measure : Constants.BASE_MEASURES) {

            calculateAllDistances(birth_records, new SumOfFieldDistances(measure, BirthSiblingLinkageRecipe.LINKAGE_FIELDS));
        }
    }

    private void calculateAllDistances(final List<LXP> birth_records, LXPMeasure measure) {

        LocalDateTime start = LocalDateTime.now();

        for (LXP record1 : birth_records) {
            for (LXP record2 : birth_records) {
                double distance = measure.distance(record1, record2);
            }
        }

        System.out.println("elapsed for " + measure.getMeasureName() + ": " + Duration.between(start, LocalDateTime.now()).toMillis()/1000);
    }

    public static void main(String[] args) {

        new MeasureCosts(Umea.REPOSITORY_NAME).run();
    }
}
