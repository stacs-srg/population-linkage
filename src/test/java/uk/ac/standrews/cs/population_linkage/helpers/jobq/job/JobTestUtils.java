/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.DoubleExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.IntegerExpression;

public class JobTestUtils {

    public static JobWithExpressions buildJobWithExpressions() {
        JobWithExpressions jobWithExpressions = new JobWithExpressions();
        jobWithExpressions.setRequiredMemory(7);
        jobWithExpressions.setPriority(2);
        jobWithExpressions.setThreshold(new DoubleExpression("0.0->1.0@0.25"));
        jobWithExpressions.setPreFilterRequiredFields(new IntegerExpression("0->4@1"));
        jobWithExpressions.setBirthsCacheSize(new IntegerExpression("0"));
        jobWithExpressions.setMarriagesCacheSize(new IntegerExpression("0"));
        jobWithExpressions.setDeathsCacheSize(new IntegerExpression("1000"));
        jobWithExpressions.setRos(new IntegerExpression("25->70@15"));
        jobWithExpressions.setMaxSiblingAgeDiff(new IntegerExpression("30"));
        jobWithExpressions.setMinMarriageAge(new IntegerExpression("15&18"));
        jobWithExpressions.setMinParentingAge(new IntegerExpression("15&18"));
        jobWithExpressions.setMaxParentingAge(new IntegerExpression("45&47&50"));
        jobWithExpressions.setMaxMarriageAgeDiscrepancy(new IntegerExpression("15"));
        jobWithExpressions.setMaxDeathAge(new IntegerExpression("100&110"));
        return jobWithExpressions;
    }

    public static JobWithExpressions buildSingularJob() {
        JobWithExpressions jobWithExpressions = new JobWithExpressions();
        jobWithExpressions.setThreshold(new DoubleExpression("0.0"));
        jobWithExpressions.setPreFilterRequiredFields(new IntegerExpression("0"));
        jobWithExpressions.setBirthsCacheSize(new IntegerExpression("0"));
        jobWithExpressions.setMarriagesCacheSize(new IntegerExpression("0"));
        jobWithExpressions.setDeathsCacheSize(new IntegerExpression("1000"));
        jobWithExpressions.setRos(new IntegerExpression("25"));
        jobWithExpressions.setMaxSiblingAgeDiff(new IntegerExpression("30"));
        jobWithExpressions.setMinMarriageAge(new IntegerExpression("15"));
        jobWithExpressions.setMinParentingAge(new IntegerExpression("15"));
        jobWithExpressions.setMaxParentingAge(new IntegerExpression("45"));
        jobWithExpressions.setMaxMarriageAgeDiscrepancy(new IntegerExpression("15"));
        jobWithExpressions.setMaxDeathAge(new IntegerExpression("100"));
        return jobWithExpressions;
    }
}
