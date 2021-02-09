/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobListHelper.isSingularJob;

public class JobList extends EntitiesList<JobWithExpressions> {

    public JobList(String jobListFile) throws IOException, InterruptedException {
        super(JobWithExpressions.class, jobListFile, Lock.JOBS);
    }

    // for testing only
    JobList() {
        super(JobWithExpressions.class);
    }

    public Optional<Job> selectJobAndReleaseFile(int assignedMemory) throws IOException {

//        // this is a hack as process keep busting through the lock and emptying the job file
        if(isEmpty()) {
            releaseAndCloseFile(Lock.JOBS);
            return Optional.empty();
        }

        Optional<Job> topJob = selectJob(assignedMemory);

        System.out.println("Job taken: " + topJob);

        writeEntriesToFile();
        releaseAndCloseFile(Lock.JOBS);
        return topJob;
    }

    public Optional<Job> selectJob(int assignedMemory) {
        Optional<JobWithExpressions> topJob = takeTopJob(assignedMemory);

        if(topJob.isPresent() && !JobListHelper.isSingularJob(topJob.get())) {
            Set<JobWithExpressions> partiallyExplodedJobs =
                    topJob.map(JobListHelper::explodeJobWithExpressions)
                            .orElse(Collections.emptySet());
            addAll(partiallyExplodedJobs);
            topJob = takeTopJob(assignedMemory); // this time the top job will be the singular job we created in the explosion
        }

        return topJob.map(JobMappers::map);
    }

    private Optional<JobWithExpressions> takeTopJob(int assignedMemory) {
        Optional<JobWithExpressions> topJobWithExpressions = getTopJobWithExpression(assignedMemory);
        Optional<JobWithExpressions> topSingularJob = getTopSingularJob(assignedMemory);

        if(!topSingularJob.isPresent() && !topJobWithExpressions.isPresent()) {
            return Optional.empty();
        }

        int expressionPriority = topJobWithExpressions.map(JobCore::getPriority).orElse(Integer.MAX_VALUE);
        int singularPriority = topSingularJob.map(JobCore::getPriority).orElse(Integer.MAX_VALUE);

        if(topSingularJob.isPresent() && singularPriority <= expressionPriority) {
            remove(topSingularJob.get());
            return topSingularJob;
        } else {
            remove(topJobWithExpressions.get());
            return topJobWithExpressions;
        }
    }

    private Optional<JobWithExpressions> getTopSingularJob(int assignedMemory) {
        return stream()
                .sorted(Comparator.comparingInt(JobCore::getPriority))
                .filter(JobListHelper::isSingularJob)
                .filter(job -> job.getRequiredMemory() <= assignedMemory)
                .findFirst();
    }

    private Optional<JobWithExpressions> getTopJobWithExpression(int assignedMemory) {
        return stream()
                .sorted(Comparator.comparingInt(JobCore::getPriority))
                .filter(job -> !isSingularJob(job))
                .filter(job -> job.getRequiredMemory() <= assignedMemory)
                .findFirst();
    }
}
