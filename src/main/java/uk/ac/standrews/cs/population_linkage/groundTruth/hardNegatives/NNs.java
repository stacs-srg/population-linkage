/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.hardNegatives;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.util.ArrayList;
import java.util.List;

/**
 * This class finds nearest neighbours for births using BitBlaster.
 * It uses the BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS to find NNs that might be close matches (false positives) for
 * birth death linkage
 */
public class NNs {

    private static final double THRESHOLD = 0.0000001;
    private static final double COMBINED_AVERAGE_DISTANCE_THRESHOLD = 0.2;
    public static final double DISTANCE_THRESHOLD = 0.45;
    public static final double THRESHOLD_DELTA = 0.05;
    private final BitBlasterSearchStructure<Birth> birth_bb;
    private final BitBlasterSearchStructure<Death> death_bb;

    public NNs(IBucket<Birth> birth_records, IBucket<Death> death_records ) throws BucketException {

        StringMetric base_metric = new JensenShannon(2048);
        Metric<LXP> composite_metric = getCompositeBirthMetric(base_metric);
        birth_bb = new BitBlasterSearchStructure(composite_metric, birth_records.getInputStream());
        death_bb = new BitBlasterSearchStructure(composite_metric, death_records.getInputStream());
    }

    public List<Birth> getBirthNNs(Birth search_record, int number_nns ) {
        return getNNs( birth_bb, search_record, number_nns );
    }

    public List<Death> getDeathNNs(Death search_record, int number_nns ) {
        return getNNs( death_bb, search_record, number_nns );
    }

    private <T extends LXP> List<T> getNNs(BitBlasterSearchStructure<T> bb, T search_record, int number_nns) {

        List<DataDistance<T>> nns = new ArrayList<>();
        double distance = DISTANCE_THRESHOLD;
        int tries = 0;
        while( nns.size() < number_nns + 1 && tries < 5 ) { // +1 is because the search record is returned too
            nns = bb.findWithinThreshold(search_record, distance); // Finds matching births
            distance += THRESHOLD_DELTA;
            tries++;
        }
        List<T> result = new ArrayList<>();
        // filter out the search record
        for( DataDistance<T> dd : nns ) {
            if( dd.value.getId() != search_record.getId() ) {
               result.add( dd.value );
            }
            if( result.size() == number_nns ) {
                return result;
            }
        }
        return result;
    }

    private static Metric<LXP> getCompositeBirthMetric(StringMetric base_metric ) {
        return new Sigma(base_metric, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, 0);
    }

    private static Metric<LXP> getCompositeDeathMetric(StringMetric base_metric ) {
        return new Sigma(base_metric, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS, 0);
    }

}
