/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.builders.failureinvestigation;

import uk.ac.standrews.cs.neoStorr.impl.DynamicLXP;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.compositeMetrics.SigmaMissingZero;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

/**
 * This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which aggregate metric to use.
 */
public class TestJS3 {

    /**
     * performs conversion from death to birth and is tolerant of DynamicLXPs which are created during linkage.
     * @param recipe - the recipe being used
     * @param death - a record to convert
     * @return a death record
     */
    private static LXP convert(LinkageRecipe recipe, LXP death) throws Exception {
        if( death instanceof Death ) {
            return recipe.convertToOtherRecordType(death);
        } else if( death instanceof DynamicLXP) {
            LXP result = new Birth();
            for (int i = 0; i < recipe.getLinkageFields().size(); i++) {
                result.put(recipe.getLinkageFields().get(i), death.get(recipe.getQueryMappingFields().get(i)));
            }
            return result;
        } else {
            throw new Exception( "convert encountered an unexpected LXP type: " );
        }
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results

        LXP b1 = null;
        LXP b2 = null;
        LXP compare = null;

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthDeathIdentityLinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, number_of_records, TestJS3.class.getCanonicalName(), bridge);

            // StringMetric metric = linkageRecipe.getMetric();
            SigmaMissingZero metric = new SigmaMissingZero(linkageRecipe.getBaseMetric(), linkageRecipe.getLinkageFields(), Birth.STANDARDISED_ID);

            Iterable<LXP> recs = linkageRecipe.getStoredRecords();
            for (LXP birth : recs) {
                b1 = birth;

                for (LXP death : linkageRecipe.getQueryRecords()) {

                    b2 = death;
                    compare = convert(linkageRecipe, death);

                    double d = metric.distance(b1, compare);
                    // System.out.println(d);
                }
            }
        } catch ( BucketException e ) {
            System.out.println( "Bucket exception" );
            System.exit(0);
        } catch ( RepositoryException e ) {
            System.out.println( "Repo exception" );
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Runtime exception:");
            System.out.println("STANDARDISED_ID 1 = " + b1.getString(Death.STANDARDISED_ID));
            System.out.println("STANDARDISED_ID 2 = " + b2.getString(Death.STANDARDISED_ID));
            System.out.println("b1 = " + b1 );
            System.out.println("b2 = " + b2 );
            System.out.println("Compare = " + compare );
            e.printStackTrace();
            System.exit(0);
        } finally {
            System.out.println("Run finished successfully");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
