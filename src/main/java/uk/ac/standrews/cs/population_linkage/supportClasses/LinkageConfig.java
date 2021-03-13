/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import java.nio.file.Path;

public class LinkageConfig {

    public static long seed = 12345L;

    public static int birthCacheSize = 10000;
    public static int deathCacheSize = 10000;
    public static int marriageCacheSize = 10000;

    public static int numberOfROs = 70;

    public static Integer MIN_AGE_AT_MARRIAGE = 15;
    public static Integer MAX_SIBLING_AGE_DIFF = 40;

    public static Integer MIN_PARENT_AGE_AT_BIRTH = 15;
    public static Integer MAX_PARENT_AGE_AT_BIRTH = 50;
    public static Integer MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY = 10;

    public static Integer MAX_AGE_AT_DEATH = 120;

    public static Path GT_COUNTS_FILE = null;
}
