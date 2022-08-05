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

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;

/**
 * This class attempts to find birth-death links: links a baby on a birth to the same person as the deceased on a death record.
 * It takes an extra parameter over standard Builders choosing which composite measure to use.
 */
public class TestJS4 {

    public static void main(String[] args) throws BucketException {

        String s1 = "PÄHRSSON";
        String s2 = "GISSLIN";

        try {

            double d = Constants.JENSEN_SHANNON.distance(s1, s2);
            System.out.println(d);

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
