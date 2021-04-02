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
import java.util.stream.Collectors;

public class StringExpression extends Expression<String> {

    public StringExpression(String expression) {
        this.expression = expression;
        this.values = explodeExpression(expression);
    }

    @Override
    public Expression<String> clone() {
        return new StringExpression(getExpression());
    }

    @Override
    protected HashSet<String> parseRangeExpression(String expression) {
        throw new UnsupportedOperationException("StringExpression does not support ranges");
    }

    @Override
    protected String getRangeRegex() {
        return "$a"; // This is an unmatchable regex, $ means final character and then
        // the a makes us check for an a following the final character - thus always false
        // thus will always return false, preventing us from every trying to convert a String
        // into a Range Expression and thus throwing the unsupported operation RTE exception
    }

    @Override
    protected HashSet<String> parseAndExpression(String expression) {
        return Arrays.stream(expression.split("&"))
                .map(String::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    protected String getAndRegex() {
        return "[^&]+&([^&]+&?)+";
    }

    @Override
    protected HashSet<String> parseSingleExpression(String expression) {
        HashSet<String> strings = new HashSet<>();
        strings.add(expression);
        return strings;
    }

    @Override
    protected String getSingleRegex() {
        return "[^&]+";
    }

    @Override
    protected Expression<String> makeSingularExpression(String value) {
        return new StringExpression(value);
    }
}
