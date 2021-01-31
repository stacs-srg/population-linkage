/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions;

public class InvalidExpressionStateException extends RuntimeException {
    public InvalidExpressionStateException(String message) {
        super(message);
    }
}
