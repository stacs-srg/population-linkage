/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class Expression<T> implements Serializable {

    private static final long serialVersionUID = -5673894763477710L;

    private String expression;
    private final HashSet<T> values;

    public Expression(String expression) {
        this.expression = expression;
        values = explodeExpression(expression);
    }

    public Expression(T value) {
        values = new HashSet<>();
        values.add(value);
        updateExpression();
    }

    private HashSet<T> explodeExpression(String expression) {
        if(expression.matches(getSingleRegex())) {
            return parseSingleExpression(expression);
        }

        if(expression.matches(getAndRegex())) {
            return parseAndExpression(expression);
        }

        if(expression.matches(getRangeRegex())) {
            return parseRangeExpression(expression);
        }

        throw new InvalidParameterException(String.format("(%s) (%s) did not match expected forms: (%s) OR (%s) OR (%s)", this.getClass().getSimpleName(), expression, getSingleRegex(), getAndRegex(), getRangeRegex()));
    }

    @Override
    public abstract Expression<T> clone();

    protected abstract HashSet<T> parseRangeExpression(String expression);

    protected abstract String getRangeRegex();

    protected abstract HashSet<T> parseAndExpression(String expression);

    protected abstract String getAndRegex();

    protected abstract HashSet<T> parseSingleExpression(String expression);

    protected abstract String getSingleRegex();

    public Expression<T> takeValue() {
        T taken = values.stream().findFirst().orElseThrow(() ->
                new InvalidExpressionStateException(String.format("Empty values set for expression: %s", expression)));
        values.remove(taken);
        updateExpression();
        return makeSingularExpression(taken);
    }

    protected abstract Expression<T> makeSingularExpression(T value);

    private void updateExpression() {
        Iterable<String> iterable = () -> values.stream().sorted().map(String::valueOf).iterator();
        expression = String.join("&", iterable);
    }

    public boolean isSingular() {
        return values.size() == 1;
    }

    public Set<T> getValues() {
        return values;
    }

    public T getValueIfSingular() {
        if(isSingular()) {
            return values.stream().findAny().orElseThrow(() ->
                    new ExpressionNotSingularException(String.format("Expresion (%s) with values (%s) is not singular", expression, values)));
        }
        throw new ExpressionNotSingularException(String.format("Expresion (%s) with values (%s) is not singular", expression, values));
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression<?> that = (Expression<?>) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, values);
    }
}
