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

    public static Integer MIN_AGE_AT_MARRIAGE = 15;                    // Minimum age of bride or groom at marriage.
    public static Integer MAX_SIBLING_AGE_DIFFERENCE = 40;             // Maximum difference in age between siblings, in years.
    public static Integer MAX_MARRIAGE_BIRTH_DIFFERENCE = 40;          // Maximum time between marriage and child being born, in years.
    public static Integer MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE = 15;  // Minimum time between parents' marriage and child's marriage, in years.
    public static Integer MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE = 100; // Maximum time between parents' marriage and child's marriage, in years.
    public static Integer MIN_PARENT_AGE_AT_BIRTH = 15;                // Minimum age of parent at child birth.
    public static Integer MAX_PARENT_AGE_AT_BIRTH = 50;                // Maximum age of parent at child birth.
    public static Integer MAX_ALLOWABLE_AGE_DISCREPANCY = 4;          // Maximum tolerated discrepancy in years between ages calculated from different records in potential identity link.
    public static Integer MAX_INTER_MARRIAGE_DIFFERENCE = 50;          // Maximum time in years between consecutive marriages of a given person.
    public static Integer MAX_AGE_AT_DEATH = 120;
}
