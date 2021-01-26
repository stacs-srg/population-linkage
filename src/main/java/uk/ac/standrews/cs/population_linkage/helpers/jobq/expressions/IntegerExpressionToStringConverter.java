package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import com.fasterxml.jackson.databind.util.StdConverter;

public class IntegerExpressionToStringConverter extends StdConverter<IntegerExpression, String> {

    @Override
    public String convert(IntegerExpression value) {
        return value.toString();
    }
}
