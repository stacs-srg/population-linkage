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
package uk.ac.standrews.cs.population_linkage.resolvers;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;

import java.time.LocalDate;
import java.util.*;

public abstract class OpenTriangleCluster {
    public final String x;
    public List<List<String>> triangleChain = new ArrayList<>();
    protected Set<LXP> children = new HashSet<LXP>();
    protected Map<String, LocalDate> birthDays = new HashMap<String, LocalDate>();
    protected Map<String, Integer> birthplaceMap = new HashMap<String, Integer>();
    protected double yearTotal = 0;
    protected int ageRange;
    protected double yearAvg;
    protected int yearMedian;
    protected String mostCommonBirthplace = null;

    public OpenTriangleCluster(String x, List<List<String>>  triangleChain) {
        this.x = x;
        this.triangleChain = triangleChain;
    }

    public String toString() {
        return "X = " + x;
    }

    public List<List<String>> getTriangleChain() {
        return triangleChain;
    }

    public abstract void getYearStatistics() throws BucketException;

    public int getAgeRange() {
        return ageRange;
    }

    public double getYearAvg() {
        return yearAvg;
    }

    public double getNumOfChildren() {
        return children.size();
    }

    public Map<String, LocalDate> getBirthDays() {
        return birthDays;
    }

    public double getYearMedian() {
        return yearMedian;
    }

    public String getMostCommonBirthplace() {
        return mostCommonBirthplace;
    }
}
