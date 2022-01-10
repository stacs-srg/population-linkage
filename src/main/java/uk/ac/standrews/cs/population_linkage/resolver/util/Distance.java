/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver.util;

public class Distance {
    public final long startNodeId;
    public final long endNodeId;
    public final double distance;

    public Distance(long startNodeId, long endNodeId, double distance) {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance)) return false;

        Distance distance1 = (Distance) o;

        if (startNodeId != distance1.startNodeId) return false;
        if (endNodeId != distance1.endNodeId) return false;
        return Double.compare(distance1.distance, distance) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (startNodeId ^ (startNodeId >>> 32));
        result = 31 * result + (int) (endNodeId ^ (endNodeId >>> 32));
        temp = Double.doubleToLongBits(distance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
