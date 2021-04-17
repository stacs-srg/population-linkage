/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

public class InvalidEvaluationApproachException extends Exception {
    public InvalidEvaluationApproachException(String message) {
        super(message);
    }

    public InvalidEvaluationApproachException(Exception e) {
        super(e);
    }
}
