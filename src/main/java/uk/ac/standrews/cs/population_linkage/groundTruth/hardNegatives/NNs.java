/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.hardNegatives;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * This class finds nearest neighbours for births using BitBlaster.
 * It uses the BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS to find NNs that might be close matches (false positives) for
 * birth death linkage
 */
public class NNs {

    public static final double DISTANCE_THRESHOLD = 0.45;
    public static final double THRESHOLD_DELTA = 0.05;
    private final BitBlasterSearchStructure<Birth> birth_bb;
    private final BitBlasterSearchStructure<Death> death_bb;

    public NNs(IBucket<Birth> birth_records, IBucket<Death> death_records) throws BucketException {

        LXPMeasure composite_measure = getCompositeBirthMeasure(Constants.JENSEN_SHANNON);
        
        birth_bb = new BitBlasterSearchStructure(composite_measure, birth_records.getInputStream());
        death_bb = new BitBlasterSearchStructure(composite_measure, death_records.getInputStream());
    }

    public List<Birth> getBirthNNs(Birth search_record, int number_nns) {
        return getNNs(birth_bb, search_record, number_nns);
    }

    public List<Death> getDeathNNs(Death search_record, int number_nns) {
        return getNNs(death_bb, search_record, number_nns);
    }

    private <T extends LXP> List<T> getNNs(BitBlasterSearchStructure<T> bb, T search_record, int number_nns) {

        List<DataDistance<T>> nns = new ArrayList<>();
        double distance = DISTANCE_THRESHOLD;
        int tries = 0;
        while (nns.size() < number_nns + 1 && tries < 5) { // +1 is because the search record is returned too
            nns = bb.findWithinThreshold(search_record, distance); // Finds matching births
            distance += THRESHOLD_DELTA;
            tries++;
        }
        List<T> result = new ArrayList<>();
        // filter out the search record
        for (DataDistance<T> dd : nns) {
            if (dd.value.getId() != search_record.getId()) {
                result.add(dd.value);
            }
            if (result.size() == number_nns) {
                return result;
            }
        }
        return result;
    }

    private static LXPMeasure getCompositeBirthMeasure(StringMeasure base_measure) {
        return new SumOfFieldDistances(base_measure, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS);
    }
}
