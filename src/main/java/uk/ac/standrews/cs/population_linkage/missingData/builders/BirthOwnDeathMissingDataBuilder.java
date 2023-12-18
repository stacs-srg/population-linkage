/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.missingData.builders;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.Imputer;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

/**
 *  This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which composite measure to use.
 */
public class BirthOwnDeathMissingDataBuilder implements MakePersistent {

    private static void runExperiment(String source_repo, String number_of_records, String mode, int linkage_fields, NeoDbCypherBridge bridge, StringMeasure base_measure, double threshold) throws Exception {

        // TODO add post filtering too.

        System.out.println(mode + " option selected with " + linkage_fields + " fields required to be non-empty");
        switch (mode) {

            case "missing-zero": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName());
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LXPMeasure record_distance_measure = new LXPMeasure(linkage_recipe.getLinkageFields(), linkage_recipe.getQueryMappingFields(), base_measure, Imputer.ZERO);
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner(record_distance_measure, threshold);

                runner.run(linkage_recipe, new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "missing-one": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName());
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LXPMeasure record_distance_measure = new LXPMeasure(linkage_recipe.getLinkageFields(), linkage_recipe.getQueryMappingFields(), base_measure, base_measure.getMaxDistance() == 1 ? Imputer.ONE : Imputer.MAX_DOUBLE);
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner(record_distance_measure, threshold);

                runner.run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "max": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName());
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LXPMeasure record_distance_measure = new LXPMeasure(linkage_recipe.getLinkageFields(), linkage_recipe.getQueryMappingFields(), base_measure, Imputer.RECORD_MAX);
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner(record_distance_measure, threshold);

                runner.run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "mean-of-present": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName());
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LXPMeasure record_distance_measure = new LXPMeasure(linkage_recipe.getLinkageFields(), linkage_recipe.getQueryMappingFields(), base_measure, Imputer.RECORD_MEAN);
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner(record_distance_measure, threshold);

                runner.run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            case "standard": {
                BirthDeathIdentityLinkageRecipe linkage_recipe = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName());
                linkage_recipe.setNumberLinkageFieldsRequired(linkage_fields);

                LXPMeasure record_distance_measure = new LXPMeasure(linkage_recipe.getLinkageFields(), linkage_recipe.getQueryMappingFields(), base_measure);
                BitBlasterLinkageRunner runner = new BitBlasterLinkageRunner(record_distance_measure, threshold);

                runner.run(linkage_recipe,new BirthOwnDeathMissingDataBuilder(), true, false);
                break;
            }
            default: {
                throw new RuntimeException("Error mode: " + mode + " unrecognised ");
            }
        }
    }

    public static void main(String[] args) throws Exception {

        String source_repo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results
        StringMeasure base_measure = Constants.get(args[2]);
        double threshold = Double.parseDouble(args[3]);

        String[] modes = new String[]{  "missing-zero", "missing-one", "max", "missing-half", "mean", "mean-of-present", "standard" };

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();
            BirthDeathIdentityLinkageRecipe recipe_unused_in_expt = new BirthDeathIdentityLinkageRecipe(source_repo, number_of_records, BirthOwnDeathMissingDataBuilder.class.getName())) {

            final int all_fields = recipe_unused_in_expt.ALL_LINKAGE_FIELDS;
            final int half_fields = all_fields - (all_fields / 2) + 1;

            for (String mode : modes) {

                runExperiment(source_repo, number_of_records, mode, all_fields, bridge, base_measure, threshold);

                runExperiment(source_repo, number_of_records, mode, 0, bridge, base_measure, threshold); // First run with no requirement on the number of fields

                for (int linkage_fields = all_fields; linkage_fields >= half_fields; linkage_fields--) {
                    runExperiment(source_repo, number_of_records, mode, linkage_fields, bridge, base_measure, threshold);
                    linkage_fields--;
                }
            }
        }
    }

    @Override
    public void makePersistent(LinkageRecipe linkage_recipe, Link link) {
        throw new RuntimeException( "makePersistent unimplemented and unneeded in this code" );
    }
}
