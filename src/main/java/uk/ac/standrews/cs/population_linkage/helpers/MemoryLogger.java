/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.lang.management.ManagementFactory;

public class MemoryLogger {

    private static long maxSimUsage = 0L;

    public static void update() {
        long currentUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        if (currentUsage > maxSimUsage) {
            maxSimUsage = currentUsage;
        }
    }

    public static long getMax() {
        update();
        return maxSimUsage;
    }

}
