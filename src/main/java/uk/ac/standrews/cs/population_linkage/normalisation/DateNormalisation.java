/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module linkage-java.
 *
 * linkage-java is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * linkage-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with linkage-java. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.normalisation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DateNormalisation {

    public static List<String> NORMALISED_MONTH_NAMES = Arrays.asList("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");

    public static List<Set<String>> MONTH_NAMES = Arrays.asList(
            new HashSet<>(Arrays.asList("january")),
            new HashSet<>(Arrays.asList("february")),
            new HashSet<>(Arrays.asList("march")),
            new HashSet<>(Arrays.asList("april")),
            new HashSet<>(Arrays.asList("may")),
            new HashSet<>(Arrays.asList("june")),
            new HashSet<>(Arrays.asList("july")),
            new HashSet<>(Arrays.asList("august")),
            new HashSet<>(Arrays.asList("september", "sept")),
            new HashSet<>(Arrays.asList("october")),
            new HashSet<>(Arrays.asList("november")),
            new HashSet<>(Arrays.asList("december")));

    static {
        // Add the normalised names and month indices to the recognised month names.
        for (int month = 0; month < NORMALISED_MONTH_NAMES.size(); month++) {

            Set<String> this_month_names = MONTH_NAMES.get(month);

            this_month_names.add(NORMALISED_MONTH_NAMES.get(month));

            String index_as_string = String.valueOf(month + 1);
            this_month_names.add(index_as_string);

            if (index_as_string.length() == 1) {
                this_month_names.add("0" + index_as_string);
            }
        }
    }

    /**
     * @param input the text to normalise
     * @return that text representation of the month in a standard form
     */
    public static String normaliseMonth(String input) {

        input = stripRubbish(input).toLowerCase();

        for (int month = 0; month < NORMALISED_MONTH_NAMES.size(); month++) {

            if (MONTH_NAMES.get(month).contains(input)) {
                return NORMALISED_MONTH_NAMES.get(month);
            }
        }

        return input;
    }

    /**
     * @param input the text to normalise
     * @return that text representation of the day of week in a standard form
     */
    public static String normaliseDayOfWeek(String input) {

        input = stripRubbish(input).toLowerCase();

        if (input.equals("monday") || input.equals("1") || input.equals("mon"))
            return "mon";
        if (input.equals("tuesday") || input.equals("2") || input.equals("tue"))
            return "tue";
        if (input.equals("wednesday") || input.equals("3") || input.equals("wed"))
            return "wed";
        if (input.equals("thursday") || input.equals("4") || input.equals("thu"))
            return "thu";
        if (input.equals("friday") || input.equals("5") || input.equals("fri"))
            return "fri";
        if (input.equals("saturday") || input.equals("6") || input.equals("sat"))
            return "sat";
        if (input.equals("sunday") || input.equals("7") || input.equals("sun"))
            return "sun";

        throw new RuntimeException("Unrecognized day: " + input);
    }

    private static String stripRubbish(String input) {

        input = input.trim();
        if (input.contains(" ")) {
            input = input.substring(0, input.indexOf(" "));
        }
        if (input.contains("[")) {
            input = input.substring(0, input.indexOf("["));
        }
        if (input.contains("(")) {
            input = input.substring(0, input.indexOf("("));
        }
        return input;
    }

    public static String cleanDate(String day, String month, String year) {

        return cleanDay(day) + DATE_SEPARATOR + cleanMonth(month) + DATE_SEPARATOR + cleanYear(year);
    }

    private static String cleanDay(final String day) {

        if (notGiven(day)) {
            return BLANK_DAY;
        }

        try {
            String d = String.valueOf(Integer.parseInt(day));

            if (d.length() == 1) {
                return "0" + d;
            }
            return d;
        } catch (NumberFormatException e) {
            return BLANK_DAY;
        }
    }

    private static String cleanMonth(final String month) {

        if (notGiven(month)) {
            return BLANK_MONTH;
        }

        if (month.length() > 3) {
            return month.substring(0, 3);
        }
        return month;
    }

    private static String cleanYear(final String year) {

        if (notGiven(year)) {
            return BLANK_YEAR;
        }

        try {
            int i = Integer.parseInt(year);
            if (i > 10) {
                i += 1800;
            } else {
                i += 1900;
            }
            return String.valueOf(i);
        } catch (NumberFormatException e) {
            return BLANK_YEAR;
        }
    }

    private static boolean notGiven(final String field) {

        return field.equals("") || field.equals("na") || field.equals("ng");
    }

    private static final String DATE_SEPARATOR = "/";

    private static final String BLANK_DAY = "--";
    private static final String BLANK_MONTH = "---";
    private static final String BLANK_YEAR = "----";
}
