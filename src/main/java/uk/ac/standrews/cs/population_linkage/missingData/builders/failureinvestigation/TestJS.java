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
package uk.ac.standrews.cs.population_linkage.missingData.builders.failureinvestigation;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.MeanOfFieldDistancesWithZeroForMissingFields;
import uk.ac.standrews.cs.population_records.record_types.Birth;

/**
 * This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which composite measure to use.
 */
public class TestJS {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String number_of_records = args[1]; // e.g. synth_results

        String s1 = null;
        String s2 = null;

        LXP b1 = null;

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthDeathIdentityLinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, number_of_records, TestJS.class.getName(), bridge);

            MeanOfFieldDistancesWithZeroForMissingFields measure = new MeanOfFieldDistancesWithZeroForMissingFields(linkageRecipe.getBaseMeasure(), linkageRecipe.getLinkageFields());

            Iterable<LXP> recs = linkageRecipe.getStoredRecords();
            for (LXP rec : recs) {
                if (rec.getString(Birth.STANDARDISED_ID).equals("921486")) {
                    b1 = rec;
                }
            }
            s1 = b1.getString(Birth.FATHER_SURNAME);

            for (LXP rec : recs) {

                s2 = rec.getString(Birth.FATHER_SURNAME);

                double d = measure.distance(b1, rec);
                System.out.println(d);
            }

        } catch (RuntimeException e) {
            System.out.println("Runtime exception:");
            System.out.println("String 1 = " + s1);
            System.out.println("String 2 = " + s2);
            e.printStackTrace();
        } catch ( Exception e ) {
            System.out.println("Regular exception");
            System.exit(-1);
        } finally {
            System.out.println("Run finished successfully");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
