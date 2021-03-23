/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

public class InvalidJobException extends RuntimeException {
    public InvalidJobException(String message) {
        super(message);
    }
}
