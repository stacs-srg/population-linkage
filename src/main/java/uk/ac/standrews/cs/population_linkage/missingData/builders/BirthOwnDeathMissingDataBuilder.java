/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.missingData.builders;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.missingData.recipes.*;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which aggregate metric to use.
 */
public class BirthOwnDeathMissingDataBuilder implements MakePersistent {

    private static void runExperiment(String source_repo, String number_of_records, String mode, int linkage_fields, NeoDbCypherBridge bridge) throws Exception {

        // TODO add post filtering too.

        System.out.println(mode + " option selected with " + linkage_fields + " fields required to be non-empty");
        switch (mode) {

            case "missing-zero": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMissingZero(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe, new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "missing-one": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMissingOne(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "max": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMax(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "missing-half": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMissingHalf(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "mean": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMissingMean(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "mean-of-present": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BDLinkageRecipeMeanOfPresent(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "standard": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);
                new BitBlasterLinkageRunner().run(linkage_recipe, new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            default: {
                throw new RuntimeException("Error mode: " + mode + " unrecognised ");
            }
        }
    }

    public static void main(String[] args) throws BucketException {

        String source_repo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results

        String[] modes = new String[]{  "missing-zero", "missing-one", "max", "missing-half", "mean", "mean-of-present", "standard" };

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {

            BirthDeathIdentityLinkageRecipe recipe_unused_in_expt = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getCanonicalName(), bridge);

            final int all_fields = recipe_unused_in_expt.ALL_LINKAGE_FIELDS;
            final int half_fields = all_fields - (all_fields / 2) + 1;

            for (String mode : modes) {

                runExperiment(source_repo, number_of_records, mode, all_fields, bridge);

                // runExperiment(source_repo, number_of_records, mode, 0, bridge); // First run with no requirement on the number of fields

                //for( int linkage_fields = all_fields; linkage_fields >= half_fields; linkage_fields-- ) {
                //    runExperiment(source_repo, number_of_records, mode, linkage_fields, bridge);
                //    linkage_fields--;
                //}

            }
        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

    @Override
    public void makePersistent(LinkageRecipe linkage_recipe, Link link) {
        throw new RuntimeException( "makePersistent unimplemented and unneeded in this code" );
    }
}
