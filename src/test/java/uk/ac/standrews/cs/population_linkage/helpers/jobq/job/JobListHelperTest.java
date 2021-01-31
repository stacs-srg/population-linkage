/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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
