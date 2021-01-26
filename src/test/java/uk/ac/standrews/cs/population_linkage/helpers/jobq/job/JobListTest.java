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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobTestUtils.buildJobWithExpressions;
import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobTestUtils.buildSingularJob;

public class JobListTest {

    @Test
    public void expectedExplosion() {
        final JobList JOB_LIST_MULTIPLE = buildJobList(buildJobWithExpressions());

        Set<Job> allJobs = new HashSet<>();

        while(!JOB_LIST_MULTIPLE.isEmpty()) {
            JOB_LIST_MULTIPLE.selectJob(8).ifPresent(allJobs::add);
        }

        assertThat(allJobs).hasSize(2400);
        assertThat(JOB_LIST_MULTIPLE).hasSize(0);
    }

    @Test
    public void limitedExplosion() {
        final JobList JOB_LIST_MULTIPLE = buildJobList(buildJobWithExpressions());

        Set<Job> allJobs = new HashSet<>();

        JOB_LIST_MULTIPLE.selectJob(8).ifPresent(allJobs::add);

        assertThat(allJobs).hasSize(1);
        assertThat(JOB_LIST_MULTIPLE).hasSize(7);
    }

    @Test
    public void expectedExplosionFromSingle() {
        final JobList JOB_LIST_SINGLE = buildJobList(buildSingularJob());

        Set<Job> allJobs = new HashSet<>();

        while(!JOB_LIST_SINGLE.isEmpty()) {
            JOB_LIST_SINGLE.selectJob(8).ifPresent(allJobs::add);
        }

        assertThat(allJobs).hasSize(1);
        assertThat(JOB_LIST_SINGLE).hasSize(0);
    }

    private static JobList buildJobList(JobWithExpressions jobWithExpressions) {
        JobList jobList = new JobList();
        jobList.add(jobWithExpressions);
        return jobList;
    }



}
