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
package uk.ac.standrews.cs.population_linkage.missingData.recipes;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesWithMeanForMissingFields;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;

public class BDLinkageRecipeMissingHalf extends BirthDeathIdentityLinkageRecipe {

    // Both of these should really be based on profiling the dataset in use.

    // Field distance to be used for missing fields, for non-normalised base measures.
    public static final double MEAN_DISTANCE = 50d;

    // Field distance to be used for missing fields, for normalised base measures.
    public static final double MEAN_DISTANCE_NORMALISED = 0.5d;

    public BDLinkageRecipeMissingHalf(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, number_of_records, links_persistent_name, bridge);
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new MeanOfFieldDistancesWithMeanForMissingFields(getBaseMeasure(), getLinkageFields(), getBaseMeasure().maxDistanceIsOne() ? MEAN_DISTANCE_NORMALISED : MEAN_DISTANCE);
    }
}
