/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.recipes;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesIgnoringMissingFields;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;

public class BDLinkageRecipeMeanOfPresent extends BirthDeathIdentityLinkageRecipe {

    // Distance to be returned if all fields are missing - ignored if the base measure is already normalised.
    public static final double MAX_DISTANCE = 100d;

    public BDLinkageRecipeMeanOfPresent(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, number_of_records, links_persistent_name, bridge);
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new MeanOfFieldDistancesIgnoringMissingFields(getBaseMeasure(), getLinkageFields(), getBaseMeasure().maxDistanceIsOne() ? 1d : MAX_DISTANCE);
    }
}
