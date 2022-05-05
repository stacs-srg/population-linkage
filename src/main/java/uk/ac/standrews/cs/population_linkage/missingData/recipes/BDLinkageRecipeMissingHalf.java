/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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
