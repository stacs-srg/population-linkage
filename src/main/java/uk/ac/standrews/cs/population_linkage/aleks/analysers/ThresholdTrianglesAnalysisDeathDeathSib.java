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
package uk.ac.standrews.cs.population_linkage.aleks.analysers;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.aleks.resolvers.PatternsCounter;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.DeathSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThresholdTrianglesAnalysisDeathDeathSib extends ThresholdTrianglesAnalysis {
    private static final String DEATH_DEATH_SIBLING_TPC = "MATCH (d1:Death)-[r:SIBLING {actors: \"Deceased-Deceased\"}]->(d2:Death) WHERE (d1)-[:GT_SIBLING {actors: \"Deceased-Deceased\"}]-(d2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String DEATH_DEATH_SIBLING_FPC = "MATCH (d1:Death)-[r:SIBLING {actors: \"Deceased-Deceased\"}]->(d2:Death) WHERE NOT (d1)-[:GT_SIBLING {actors: \"Deceased-Deceased\"}]-(d2) AND r.distance <= $threshold AND r.fields_populated >= $field return count(r)";
    private static final String DEATH_DEATH_SIBLING_FNC = "MATCH (d1:Death)-[r:GT_SIBLING {actors: \"Deceased-Deceased\"}]->(d2:Death) WHERE NOT (d1)-[:SIBLING {actors: \"Deceased-Deceased\"}]-(d2) return count(r)";
    private static final String DEATH_DEATH_SIBLING_FNC_T = "MATCH (d1:Death)-[r:GT_SIBLING {actors: \"Deceased-Deceased\"}]->(d2:Death), (d1)-[s:SIBLING]-(d2) WHERE s.distance > $threshold OR s.fields_populated < $field return count(r)";

    public static void main(String[] args) throws Exception {
        DeathSiblingLinkageRecipe linkageRecipe = new DeathSiblingLinkageRecipe("umea", "EVERYTHING", DeathSiblingBundleBuilder.class.getName());
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        final int MAX_FIELD = 4;
        final int MIN_FIELD = 2; //1 below target
        final double MAX_THRESHOLD = 2.01; //0.01 above target
        final double MIN_THRESHOLD = 0.0;

        linkageRecipe.setMaxThreshold(1);
        DeathSiblingBundleBuilder.runBuilder(linkageRecipe);

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_FIELD - MIN_FIELD);

        System.out.println("Analysing thresholds...");
        //loop through each linkage field options
        for (int fields = MAX_FIELD; fields > MIN_FIELD; fields--) {
            final int currentField = fields;

            executorService.submit(() -> {
                try (FileWriter fileWriter = new FileWriter("deathdeath" + currentField + ".csv");
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {

                    //write headers
                    printWriter.println("threshold,precision,recall,fmeasure,total,fnots");

                    try (NeoDbCypherBridge localBridge = new NeoDbCypherBridge()) {
                        for (double i = MIN_THRESHOLD; i < MAX_THRESHOLD; i += 0.01) {
                            double threshold = Math.round(i * 100.0) / 100.0;

                            long fpc = doQuery(DEATH_DEATH_SIBLING_FPC, threshold, currentField, localBridge);
                            long tpc = doQuery(DEATH_DEATH_SIBLING_TPC, threshold, currentField, localBridge);
                            long fnc = doQuery(DEATH_DEATH_SIBLING_FNC, threshold, currentField, localBridge)
                                    + doQuery(DEATH_DEATH_SIBLING_FNC_T, i, currentField, localBridge);

                            //print to csv
                            printWriter.printf("%.2f,%.5f,%.5f,%.5f,%d,%d%n",
                                    threshold,
                                    ClassificationMetrics.precision(tpc, fpc),
                                    ClassificationMetrics.recall(tpc, fnc),
                                    ClassificationMetrics.F1(tpc, fpc, fnc),
                                    PatternsCounter.countOpenTrianglesCumulative(bridge, "Death", "Death", i, currentField),
                                    PatternsCounter.countOpenTrianglesIsomorphicSiblings(bridge, "Birth", "Death", i, currentField));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(12, TimeUnit.HOURS);

        resetThreshold(bridge, linkageRecipe);
    }

    /**
     * Method to reset thresholds after maximising them for analysis
     *
     * @param bridge Neo4j bridge
     * @param recipe linkage recipe used
     */
    private static void resetThreshold(NeoDbCypherBridge bridge, DeathSiblingLinkageRecipe recipe) {
        String resetString = "MATCH (d1:Death)-[r:SIBLING {actors: \"Deceased-Deceased\"}]-(d2:Death) WHERE r.distance > $threshold AND r.fields_populated = $field DELETE r";

        int linkage_fields = recipe.ALL_LINKAGE_FIELDS;
        int half_fields = linkage_fields - (linkage_fields / 2 );

        while (linkage_fields >= half_fields) {
            recipe.setNumberLinkageFieldsRequired(linkage_fields);
            doQueryDel(resetString, recipe.getThreshold(), linkage_fields, bridge);
            linkage_fields--;
        }
    }
}
