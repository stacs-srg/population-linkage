/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

public class LinkageConfig {

    public static int birthCacheSize = 10000;
    public static int deathCacheSize = 10000;
    public static int marriageCacheSize = 10000;

    public static int numberOfROs = 70;

    public static Integer MIN_AGE_AT_MARRIAGE = 15;
    public static Integer MAX_SIBLING_AGE_DIFF = 40;
    public static Integer MAX_MARRIAGE_BIRTH_DIFFERENCE = 50; // at most 50 years after marriage a birth
    public static Integer MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE = 100; // very conservative (possibly contradictory with above!)
    public static Integer MIN_PARENT_AGE_AT_BIRTH = 15;
    public static Integer MAX_PARENT_AGE_AT_BIRTH = 50;
    public static Integer MAX_ALLOWABLE_MARRIAGE_AGE_DIFFERENCE = 10;
    public static Integer MAX_INTER_MARRIAGE_DIFFERENCE = 50; // MAX TIME BETWEEN MARRIAGES

    public static Integer MAX_AGE_AT_DEATH = 120;
}
