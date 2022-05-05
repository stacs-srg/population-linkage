/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.recipes;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesWithZeroForMissingFields;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;

public class BDLinkageRecipeMissingZero extends BirthDeathIdentityLinkageRecipe {

    public BDLinkageRecipeMissingZero(String source_repository_name, String number_of_records, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, number_of_records, links_persistent_name, bridge);
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new MeanOfFieldDistancesWithZeroForMissingFields(getBaseMeasure(), getLinkageFields());
    }
}
