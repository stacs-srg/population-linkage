package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DoubleExpression extends Expression<BigDecimal> {

    public DoubleExpression(String expression) {
        super(expression);
    }

    public DoubleExpression(BigDecimal value) {
        super(value);
    }

    @Override
    public Expression<BigDecimal> clone() {
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
    protected Expression<BigDecimal> makeSingularExpression(BigDecimal value) {
        return new DoubleExpression(value);
    }

    @Override
    public HashSet<BigDecimal> parseRangeExpression(String expression) {
        var start = new BigDecimal(expression.split("->")[0]);
        var end = new BigDecimal(expression.split("@")[0].split("->")[1]);


        if(aGtB(start, end)) {
            var temp = end;
            end = start;
            start = temp;
        }

        var step = new BigDecimal(expression.split("@")[1]);

        HashSet<BigDecimal> values = new HashSet<>();
        for(var d = start; aLtEtB(d, end); d = d.add(step)) {
            values.add(d);
        }

        return values;
    }

    private boolean aGtB(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    private boolean aLtEtB(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0;
    }


    @Override
    public HashSet<BigDecimal> parseAndExpression(String expression) {
        return Arrays.stream(expression.split("&")).map(BigDecimal::new).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public HashSet<BigDecimal> parseSingleExpression(String expression) {
        return new HashSet<>(Set.of(new BigDecimal(expression)));
    }
}