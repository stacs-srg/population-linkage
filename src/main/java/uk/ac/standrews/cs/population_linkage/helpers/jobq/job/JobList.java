/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Hex;

import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobListHelper.isSingularJob;

public class JobList extends EntitiesList<JobWithExpressions> {

    public JobList(String jobListFile) throws IOException, InterruptedException {
        super(JobWithExpressions.class, jobListFile, Lock.JOBS);
    }

    // for testing only
    JobList() {
        super(JobWithExpressions.class);
    }

    public Set<Job> selectJobAndReleaseFile(int assignedMemory) throws IOException, InterruptedException {

//      this is a hack as processes keep busting through the lock and emptying the job file
        if(isEmpty()) {
            releaseAndCloseFile(Lock.JOBS);
            return Collections.emptySet();
        }

        Set<Job> topJob = selectJob(assignedMemory);

        System.out.println("Job taken: " + topJob);

        writeEntriesToFile();
        releaseAndCloseFile(Lock.JOBS);
        return topJob;
    }

    public Set<Job> selectJob(int assignedMemory) {
        Optional<JobWithExpressions> topJob = takeTopJob(assignedMemory);

        if(topJob.isPresent() && !JobListHelper.isSingularJob(topJob.get())) {
            if(!topJob.get().getExperimentId().equals("-")) {
                // then get rest of set
                Set<JobWithExpressions> jobsInExperiment = getAllJobsWithExperimentId(topJob.get().getExperimentId());
                removeAll(jobsInExperiment);
                jobsInExperiment.add(topJob.get());

                return extractSingularJobSet(jobsInExperiment);
            } else {
                Set<JobWithExpressions> partiallyExplodedJobs =
                        topJob.map(JobListHelper::explodeJobWithExpressions)
                                .orElse(Collections.emptySet());
                addAll(partiallyExplodedJobs);
                topJob = takeTopJob(assignedMemory); // this time the top job will be the singular job we created in the explosion
            }
        }

        return setOf(topJob.map(JobMappers::map));
    }

    private Set<Job> setOf(Optional<Job> job) {
        HashSet<Job> set = new HashSet<Job>();
        job.ifPresent(set::add);
        return set;
    }

    public static void main(String[] args) {
        System.out.println(Stream.of("A","A","B").distinct().collect(Collectors.toList()));
        System.out.println(Stream.of("A","A","B").collect(Collectors.toSet()));
    }

    private Set<Job> extractSingularJobSet(Set<JobWithExpressions> jobsInExperiment) {

        HashMap<String, Set<JobWithExpressions>> phaseJobsMap = new HashMap<>();

        Set<String> phases = jobsInExperiment.stream().map(JobCore::getLinkagePhase).collect(Collectors.toSet());

        for(String phase : phases) {
            Set<JobWithExpressions> phaseJobs = jobsInExperiment
                    .stream()
                    .filter(job -> job.linkagePhase.equals(phase))
                    .collect(Collectors.toSet());

            Set<JobWithExpressions> singularJobs = phaseJobs
                    .stream()
                    .filter(JobListHelper::isSingularJob)
                    .collect(Collectors.toSet());

            if(!singularJobs.isEmpty()) {
                phaseJobsMap.put(phase, phaseJobs);
            } else {
                Set<JobWithExpressions> partiallyExplodedJobs = phaseJobs
                        .stream()
                        .findAny()
                        .map(JobListHelper::explodeJobWithExpressions)
                        .orElseThrow(() -> new InvalidJobException(String.format("Missing Phase %s in Job List for experiment ID: %s",
                                phase, jobsInExperiment.stream().map(JobCore::getExperimentId).findAny().orElse("ID Not Found"))));

                phaseJobsMap.put(phase, partiallyExplodedJobs);
            }
        }

        Set<JobWithExpressions> explodedJobSet = new HashSet<>();

        for(Set<JobWithExpressions> jobsForPhase : phaseJobsMap.values()) {
            if(explodedJobSet.isEmpty()) {
                explodedJobSet.addAll(updateIds(jobsForPhase));
            } else {
                for(JobWithExpressions explodedJob : explodedJobSet) {
                    for(JobWithExpressions job : jobsForPhase) {
                        JobWithExpressions exJob = job.clone();
                        exJob.setExperimentId(explodedJob.getExperimentId());
                        explodedJobSet.add(exJob);
                    }
                }
            }
        }

        Set<String> experimentIds = explodedJobSet.stream().map(JobCore::getExperimentId).collect(Collectors.toSet());

        for(String id : experimentIds) {
            Stream<JobWithExpressions> jobs = explodedJobSet
                    .stream()
                    .filter(job -> job.getExperimentId().equals(id));

            if(jobs.allMatch(JobListHelper::isSingularJob)) {
                Set<JobWithExpressions> chosenJobSet = jobs.collect(Collectors.toSet());
                explodedJobSet.removeAll(chosenJobSet);
                addAll(explodedJobSet);
                return chosenJobSet.stream().map(JobMappers::map).collect(Collectors.toSet());
            }
        }

        throw new RuntimeException("Could not found singular job set");
    }

    private Collection<JobWithExpressions> updateIds(Set<JobWithExpressions> jobs) {
        jobs.forEach(job -> job.setExperimentId(generateRandomString()));
        return jobs;
    }

    private static String generateRandomString() {
        final byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);

        return Hex.encodeHexString(bytes)
                .substring(64)
                .toUpperCase();
    }

    private Optional<JobWithExpressions> getNonSingularJobIn(Set<JobWithExpressions> jobsInExperiment) {
        return jobsInExperiment.stream()
                .filter(job -> !JobListHelper.isSingularJob(job))
                .findFirst();
    }

    private Set<JobWithExpressions> getNonSingularJobsIn(Set<JobWithExpressions> jobsInExperiment) {
        return jobsInExperiment.stream()
                .filter(job -> !JobListHelper.isSingularJob(job))
                .collect(Collectors.toSet());
    }

    private Set<JobWithExpressions> getSingularJobsIn(Set<JobWithExpressions> jobsInExperiment) {
        return jobsInExperiment.stream()
                .filter(JobListHelper::isSingularJob)
                .collect(Collectors.toSet());
    }

    private Set<JobWithExpressions> getAllJobsWithExperimentId(String experimentId) {
        return stream()
                .filter(job -> job.getExperimentId().equals(experimentId))
                .collect(Collectors.toSet());
    }

    private Optional<JobWithExpressions> takeTopJob(int assignedMemory) { // this needs to be thinking in terms of a JobSet
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
