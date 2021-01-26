package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import com.fasterxml.jackson.databind.util.StdConverter;

public class DoubleExpressionToStringConverter extends StdConverter<DoubleExpression, String> {

    @Override
    public String convert(DoubleExpression value) {
        return value.toString();
    }
}
