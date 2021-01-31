/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.Expression;

public class JobListHelper {

    public static boolean isSingularJob(JobWithExpressions jobWithExpressions) {
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(jobWithExpressions.getClass().getDeclaredFields()));

        for (Field field : fields) {
            try {
                Expression expression = (Expression) field.get(jobWithExpressions);
                if (!expression.isSingular()) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Error when reflecting expressions of job=%s", jobWithExpressions), e);
            }
        }
        return true;
    }

    public static Set<JobWithExpressions> explodeJobWithExpressions(JobWithExpressions jobWithExpressions) {
        if(isSingularJob(jobWithExpressions)) {
            return Set.of(jobWithExpressions);
        }

        Set<JobWithExpressions> derivedJobs = new HashSet<>();

        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(jobWithExpressions.getClass().getDeclaredFields()));

        for (Field field : fields) {
            try {
                Expression expression = (Expression) field.get(jobWithExpressions);
                if (!expression.isSingular()) {
                    Expression taken = expression.takeValue();
                    derivedJobs.add(jobWithExpressions.clone());
                    field.set(jobWithExpressions, taken);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("Error when reflecting expressions of job=%s", jobWithExpressions), e);
            }
        }
        derivedJobs.add(jobWithExpressions); // this is now a singular job

        return derivedJobs;
    }
}
