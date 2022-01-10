/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import java.time.Period;
import java.time.temporal.ChronoUnit;

public class LinkageConfig {

    public static int birthCacheSize = 10000;
    public static int deathCacheSize = 10000;
    public static int marriageCacheSize = 10000;

    public static int numberOfROs = 70;

    public static final double AVERAGE_DAYS_IN_YEAR = 365.25;
    public static final double AVERAGE_DAYS_IN_MONTH = 30.4;
    public static final int AVERAGE_GESTATION_IN_MONTHS = 9;

    public static Integer MIN_AGE_AT_MARRIAGE = 15;                    // Minimum age of bride or groom at marriage.
    public static Integer MAX_SIBLING_AGE_DIFFERENCE = 40;             // Maximum difference in age between siblings, in years.
    public static Integer MAX_MARRIAGE_BIRTH_DIFFERENCE = 40;          // Maximum time between marriage and child being born, in years.
    public static Integer MIN_MARRIAGE_BIRTH_DIFFERENCE = -5;          // Minimum time between marriage and child being born, in years.
    public static Integer MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE = 15;  // Minimum time between parents' marriage and child's marriage, in years.
    public static Integer MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE = 100; // Maximum time between parents' marriage and child's marriage, in years.
    public static Integer MIN_PARENT_AGE_AT_BIRTH = 15;                // Minimum age of parent at child birth.
    public static Integer MAX_MOTHER_AGE_AT_BIRTH = 50;                // Maximum age of mother at child birth.
    public static Integer MAX_FATHER_AGE_AT_BIRTH = 70;                // Maximum age of father at child birth.
    public static Integer MAX_ALLOWABLE_AGE_DISCREPANCY = 4;           // Maximum tolerated discrepancy in years between ages calculated from different records in potential identity link.
    public static Integer MAX_AGE_AT_DEATH = 120;

    public static Integer MIN_SIBLING_AGE_DIFFERENCE_IN_DAYS = (int)(AVERAGE_GESTATION_IN_MONTHS * AVERAGE_DAYS_IN_MONTH);
    public static Integer MAX_SIBLING_AGE_DIFFERENCE_IN_DAYS = (int)(MAX_SIBLING_AGE_DIFFERENCE * AVERAGE_DAYS_IN_YEAR);
    public static Integer MAX_TWIN_AGE_DIFFERENCE_IN_DAYS = 2;
    public static Integer MAX_MARRIAGE_BIRTH_DIFFERENCE_IN_DAYS = (int)(MAX_MARRIAGE_BIRTH_DIFFERENCE * AVERAGE_DAYS_IN_YEAR);
    public static Integer MIN_MARRIAGE_BIRTH_DIFFERENCE_IN_DAYS = (int)(MIN_MARRIAGE_BIRTH_DIFFERENCE * AVERAGE_DAYS_IN_YEAR);
    public static Integer MAX_AGE_AT_DEATH_IN_DAYS = (int)(MAX_AGE_AT_DEATH * AVERAGE_DAYS_IN_YEAR);
    public static Integer MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE_IN_DAYS = (int)(MIN_CHILD_PARENTS_MARRIAGE_DIFFERENCE * AVERAGE_DAYS_IN_YEAR);
    public static Integer MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE_IN_DAYS = (int)(MAX_CHILD_PARENTS_MARRIAGE_DIFFERENCE * AVERAGE_DAYS_IN_YEAR);
}
