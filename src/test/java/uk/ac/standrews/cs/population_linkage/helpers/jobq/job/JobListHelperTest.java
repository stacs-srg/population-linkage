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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JobListHelperTest {

    @Test
    public void isSingularJob() {
        assertThat(JobListHelper.isSingularJob(JobTestUtils.buildSingularJob())).isTrue();
    }

    @Test
    public void isNotSingularJob() {
        assertThat(JobListHelper.isSingularJob(JobTestUtils.buildJobWithExpressions())).isFalse();
    }

}
