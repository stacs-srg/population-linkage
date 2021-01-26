package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DoubleExpression extends Expression<Double> {

    public DoubleExpression(String expression) {
        super(expression);
    }

    public DoubleExpression(Double value) {
        super(value);
    }

    @Override
    public Expression<Double> clone() {
        return new DoubleExpression(getExpression());
    }

    @Override
    public String getRangeRegex() {
        return "[0-9]\\.[0-9]+->[0-9]\\.[0-9]+@[0-9]\\.[0-9]+";
    }

    @Override
    public String getAndRegex() {
        return "[0-9]\\.[0-9]+&([0-9]\\.[0-9]+&?)+";
    }

    @Override
    public String getSingleRegex() {
        return "[0-9].[0-9]+";
    }

    @Override
    protected Expression<Double> makeSingularExpression(Double value) {
        return new DoubleExpression(value);
    }

    @Override
    public HashSet<Double> parseRangeExpression(String expression) {
        var start = Double.parseDouble(expression.split("->")[0]);
        var end = Double.parseDouble(expression.split("@")[0].split("->")[1]);

        if(start > end) {
            var temp = end;
            end = start;
            start = temp;
        }

        var step = Double.parseDouble(expression.split("@")[1]);

        HashSet<Double> values = new HashSet<>();
        for(var d = start; d <= end; d += step) {
            values.add(d);
        }

        return values;
    }

    @Override
    public HashSet<Double> parseAndExpression(String expression) {
        return Arrays.stream(expression.split("&")).map(Double::parseDouble).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public HashSet<Double> parseSingleExpression(String expression) {
        return new HashSet<>(Set.of(Double.valueOf(expression)));
    }
}
