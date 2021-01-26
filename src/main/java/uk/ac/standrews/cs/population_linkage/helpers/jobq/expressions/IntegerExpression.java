/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
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
        var start = Integer.parseInt(expression.split("->")[0]);
        var end = Integer.parseInt(expression.split("@")[0].split("->")[1]);

        if(start > end) {
            var temp = end;
            end = start;
            start = temp;
        }

        var step = Integer.parseInt(expression.split("@")[1]);

        HashSet<Integer> values = new HashSet<>();
        for(var d = start; d <= end; d += step) {
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
        return new HashSet<>(Set.of(Integer.parseInt(expression)));
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
