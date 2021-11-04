/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.builders;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.subsetRecipes.BirthDeathSubsetIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.missingData.linkageRunners.BBLinkageRunnerIntolerant;
import uk.ac.standrews.cs.population_linkage.missingData.linkageRunners.BBLinkageRunnerMean;
import uk.ac.standrews.cs.population_linkage.missingData.linkageRunners.BBLinkageRunnerTolerant;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which aggregate metric to use.
 */
public class BirthOwnDeathMissingDataBuilder {

    private static void runExperiment(String mode, BirthDeathSubsetIdentityLinkageRecipe linkageRecipe, int linkage_fields) throws Exception {
        linkageRecipe.setNumberLinkageFieldsRequired(linkage_fields);

        // TODO add post filtering too.

        BitBlasterLinkageRunner runner;
        System.out.println(mode + " option selected with " + linkage_fields + " fields required to be non-empty");
        switch (mode) {

            // tolerant, intolerant, standard, mean
            case "tolerant": {
                runner = new BBLinkageRunnerTolerant();
                break;
            }
            case "intolerant": {
                runner = new BBLinkageRunnerIntolerant();
                break;
            }
            case "standard": {
                runner = new BitBlasterLinkageRunner();
                break;
            }
            case "mean": {
                runner = new BBLinkageRunnerMean();
                break;
            }
            default: {
                throw new RuntimeException("Error mode: " + mode + " unrecognised ");
            }

        }
        runner.run(linkageRecipe, false, false, true, true);
    }

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results
        String mode = args[2]; // Choices are tolerant, intolerant, standard, mean

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
            BirthDeathSubsetIdentityLinkageRecipe linkageRecipe = new BirthDeathSubsetIdentityLinkageRecipe(sourceRepo, number_of_records, bridge, BirthOwnDeathMissingDataBuilder.class.getCanonicalName());

            // LinkageConfig.numberOfROs = 20;

            // runExperiment( mode, linkageRecipe, 0); // First run with no requirement on the number of fields

            int linkage_fields = linkageRecipe.ALL_LINKAGE_FIELDS;
            int half_fields = linkage_fields - (linkage_fields / 2) + 1;

            while (linkage_fields >= half_fields) {
                runExperiment( mode, linkageRecipe, linkage_fields);
                linkage_fields--;
            }

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
