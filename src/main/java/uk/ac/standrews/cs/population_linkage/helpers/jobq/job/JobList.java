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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

        Set<Job> topJobs = selectJob(assignedMemory);

        System.out.println("Job taken: " + topJobs);

        writeEntriesToFile();
        releaseAndCloseFile(Lock.JOBS);
        return topJobs;
    }

    public Set<Job> selectJob(int assignedMemory) {
        Optional<JobWithExpressions> topJob = takeTopJob(assignedMemory);

        if(topJob.isPresent()) { // TODO singular check needs to have a pre check for experiment ID
            if(!JobListHelper.isSingularJob(topJob.get()) && !isPartOfMultiPhaseLinkage(topJob)) {
                return setOf(topJob.map(JobMappers::map));
            }

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

    private boolean isPartOfMultiPhaseLinkage(Optional<JobWithExpressions> topJob) {
        return !topJob.get().getExperimentId().equals("-");
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

        for(String phase : phaseJobsMap.keySet()) {
            Set<JobWithExpressions> jobsForPhase = phaseJobsMap.get(phase);
            if(explodedJobSet.isEmpty()) {
                explodedJobSet.addAll(updateIds(jobsForPhase));
            } else {
                Set<JobWithExpressions> newExplodedJobs = new HashSet<>();
                for(JobWithExpressions explodedJob : explodedJobSet) {
                    for(JobWithExpressions job : jobsForPhase) {
                        JobWithExpressions exJob = job.clone();
                        exJob.setExperimentId(explodedJob.getExperimentId());
                        newExplodedJobs.add(exJob);
                    }
                }
                explodedJobSet.addAll(newExplodedJobs);

                for(String experimentId : getExperimentIds(explodedJobSet)) {
                    Set<JobWithExpressions> jobsWithExperimentId = getJobsWithExperimentId(explodedJobSet, experimentId);
                    Set<JobWithExpressions> jobsWithPhase = getJobsWithPhase(jobsWithExperimentId, phase);
                    if(jobsWithPhase.size() > 1) { // can we just do this for all?
                        explodedJobSet.removeAll(jobsWithExperimentId);
                        jobsWithExperimentId.removeAll(jobsWithPhase);
                        for(JobWithExpressions job : jobsWithPhase) {
                            assertNoDuplicatePhases(jobsWithExperimentId); // this should pass because we're removed the offending duplicates two lines above.
                            String newExperimentId = generateRandomString();
                            Set<JobWithExpressions> newJobs = cloneJobs(jobsWithExperimentId);
                            newJobs.add(job);
                            newJobs.forEach(j -> j.setExperimentId(newExperimentId));
                            explodedJobSet.addAll(newJobs);
                        }
                    }
                }
            }
        }

        Set<String> experimentIds = getExperimentIds(explodedJobSet);

        for(String id : experimentIds) {
            Set<JobWithExpressions> jobs = explodedJobSet
                    .stream()
                    .filter(job -> job.getExperimentId().equals(id))
                    .collect(Collectors.toSet());

            if(jobs.stream().allMatch(JobListHelper::isSingularJob)) {
                explodedJobSet.removeAll(jobs);
                addAll(explodedJobSet);
                return jobs.stream().map(JobMappers::map).collect(Collectors.toSet());
            }
        }

        throw new RuntimeException("Could not find singular job set");
    }

    private Set<JobWithExpressions> cloneJobs(Set<JobWithExpressions> jobs) {
        return jobs.stream().map(JobWithExpressions::clone).collect(Collectors.toSet());
    }

    private void assertNoDuplicatePhases(Set<JobWithExpressions> jobs) throws InvalidJobException {
        Set<String> phases = jobs.stream().map(JobCore::getLinkagePhase).collect(Collectors.toSet());
        phases.forEach(phase -> {
            Set<JobWithExpressions> jobsInPhase = jobs.stream().filter(job -> Objects.equals(job.linkagePhase, phase)).collect(Collectors.toSet());
            if(jobsInPhase.size() > 1) {
                throw new InvalidJobException(String.format("Expected no duplicate phases but found %s in set: %s", jobs.size(), jobsInPhase));
            }
        });
    }

    private Set<JobWithExpressions> getJobsWithExperimentId(Set<JobWithExpressions> jobs, String experimentId) {
        return jobs
                .stream()
                .filter(job -> Objects.equals(job.getExperimentId(), experimentId))
                .collect(Collectors.toSet());
    }

    private Set<JobWithExpressions> getJobsWithPhase(Set<JobWithExpressions> jobs, String phase) {
        return jobs
                .stream()
                .filter(job -> Objects.equals(job.linkagePhase, phase))
                .collect(Collectors.toSet());
    }

    private Set<String> getExperimentIds(Set<JobWithExpressions> explodedJobSet) {
        return explodedJobSet
                .stream()
                .map(JobCore::getExperimentId)
                .collect(Collectors.toSet());
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
