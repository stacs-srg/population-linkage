/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.storr.impl.LXP;

public class LXPHelper {
    public static LXP convertToOtherRecordType(LXP recordToConvert, LinkageRecipe linkageRecipe) {
        // here we are going to convert from the search type to the stored type - e.g. death to marriage (according to the role)

        // first make sure that the recordToConvert is of the appropriate type
        if (!(recordToConvert.getClass().equals(linkageRecipe.getSearchType()))) {
            throw new RuntimeException("Wrong record type to convert:" + recordToConvert.getClass().getName());
        }

        LXP resultingRecord;

        try {
            resultingRecord = linkageRecipe.getStoredType().newInstance(); // create an instance of the stored type
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (linkageRecipe.getLinkageFields().size() != linkageRecipe.getSearchMappingFields().size()) {
            throw new RuntimeException("Mismatched size for linkage fields and mapping fields");
        }

        // this pulls out the linkage fields from the search type and assigns them to the corresponding fields in the stored type
        // we do this so that when we pass records to the metric search they will always appear to be of the same type as that stored in the search structure
        for (int i = 0; i < linkageRecipe.getLinkageFields().size(); i++) {
            resultingRecord.put(linkageRecipe.getLinkageFields().get(i), recordToConvert.get(linkageRecipe.getSearchMappingFields().get(i)));
        }

        return resultingRecord;
    }
}
