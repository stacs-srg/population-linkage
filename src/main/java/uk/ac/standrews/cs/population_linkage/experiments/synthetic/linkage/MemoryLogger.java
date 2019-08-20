package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

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
