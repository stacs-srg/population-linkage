/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.memorylogger;

import java.lang.management.ManagementFactory;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.PreEmptiveOutOfMemoryWarning;

public class MemoryLogger {

    private static long maxSimUsage = 0L;

    private static final double threshold = 0.975;

    public static void update() {
        long currentUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        if (currentUsage > maxSimUsage) {
            maxSimUsage = currentUsage;
        }

        long mM = Runtime.getRuntime().maxMemory();

        if (mM * threshold < currentUsage) {
            throw new PreEmptiveOutOfMemoryWarning();
        }
    }

    public static long getMax() {
        update();
        return maxSimUsage;
    }

    public static void reset() {
        // runs GC to ensure no object left in memory from previous linkages that may skew memory usage logging
        System.gc();
        maxSimUsage = 0L;
    }
}
