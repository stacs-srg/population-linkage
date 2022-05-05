/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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
