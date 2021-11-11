/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.builders.failureinvestigation;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthDeathSubsetIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.missingData.compositeMetrics.SigmaTolerant;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

/**
 * This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which aggregate metric to use.
 */
public class TestJS {


    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results

        String s1 = null;
        String s2 = null;

        LXP b1 = null;

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthDeathSubsetIdentityLinkageRecipe linkageRecipe = new BirthDeathSubsetIdentityLinkageRecipe(sourceRepo, number_of_records, bridge, TestJS.class.getCanonicalName());

            // StringMetric metric = linkageRecipe.getMetric();
            SigmaTolerant metric = new SigmaTolerant(linkageRecipe.getMetric(), linkageRecipe.getLinkageFields(), Birth.STANDARDISED_ID);

            Iterable<LXP> recs = linkageRecipe.getStoredRecords();
            for (LXP rec : recs) {
                if (rec.getString(Birth.STANDARDISED_ID).equals("40770668")) {
                    b1 = rec;
                }
            }
            s1 = b1.getString(Birth.FATHER_SURNAME);

            for (LXP rec : recs) {

                s2 = rec.getString(Birth.FATHER_SURNAME);

//                System.out.println("String 1 = " + s1);
//                System.out.println("String 2 = " + s2);
                double d = metric.distance(b1, rec);
                System.out.println(d);
            }

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            System.out.println("String 1 = " + s1);
            System.out.println("String 2 = " + s2);
            e.printStackTrace();
        } finally {
            System.out.println("Run finished successfully");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
