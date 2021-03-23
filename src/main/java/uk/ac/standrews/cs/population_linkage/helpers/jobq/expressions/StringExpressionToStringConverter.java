/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import com.fasterxml.jackson.databind.util.StdConverter;

public class StringExpressionToStringConverter extends StdConverter<StringExpression, String> {

    @Override
    public String convert(StringExpression value) {
        return value.toString();
    }
}
