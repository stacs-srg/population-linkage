/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IntegerExpression extends Expression<Integer> {

    public IntegerExpression(String expression) {
        super(expression);
    }

    public IntegerExpression(Integer value) {
        super(value);
    }

    @Override
    public Expression<Integer> clone() {
        return new IntegerExpression(getExpression());
    }

    @Override
    protected HashSet<Integer> parseRangeExpression(String expression) {
        int start = Integer.parseInt(expression.split("->")[0]);
        int end = Integer.parseInt(expression.split("@")[0].split("->")[1]);

        if(start > end) {
            int temp = end;
            end = start;
            start = temp;
        }

        int step = Integer.parseInt(expression.split("@")[1]);

        HashSet<Integer> values = new HashSet<>();
        for(int d = start; d <= end; d += step) {
            values.add(d);
        }

        return values;
    }

    @Override
    protected String getRangeRegex() {
        return "[0-9]+->[0-9]+@[0-9]+";
    }

    @Override
    protected HashSet<Integer> parseAndExpression(String expression) {
        return Arrays.stream(expression.split("&")).map(Integer::parseInt).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    protected String getAndRegex() {
        return "[0-9]+&([0-9]+&?)+";
    }

    @Override
    protected HashSet<Integer> parseSingleExpression(String expression) {
        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(Integer.parseInt(expression));
        return hashSet;
    }

    @Override
    protected String getSingleRegex() {
        return "[0-9]+";
    }

    @Override
    protected Expression<Integer> makeSingularExpression(Integer value) {
        return new IntegerExpression(value);
    }
}
