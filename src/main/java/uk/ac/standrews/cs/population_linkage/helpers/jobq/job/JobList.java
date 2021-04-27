/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Hex;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.IntegerExpression;

import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobListHelper.isSingularJob;

public class JobList extends EntitiesList<JobWithExpressions> {

    public JobList(String jobListFile) throws IOException, InterruptedException {
        super(JobWithExpressions.class, jobListFile, Lock.JOBS);
    }

    public JobList(String jobListFile, Lock lock) throws IOException, InterruptedException {
        super(JobWithExpressions.class, jobListFile, lock);
    }

    // for testing only
    JobList() {
        super(JobWithExpressions.class);
    }

    public void explodeAllJobs() {
        Set<Job> allJobs = new HashSet<>();
        while(!isEmpty()) {
            allJobs.addAll(selectJob(Integer.MAX_VALUE, false));
        }
        addAll(allJobs.stream().map(JobMappers::map).collect(Collectors.toSet()));
    }

    public List<Set<JobWithExpressions>> splitJobList(ArrayList<Integer> jobCountsByPartition) {
        List<Set<JobWithExpressions>> sets = new ArrayList<>();

        Collections.shuffle(this);

        int totalJobs = (jobCountsByPartition.stream().mapToInt(Integer::intValue).filter(value -> value > size() / jobCountsByPartition.size()).sum() + size());
        int jobsPerPartition = (int) Math.ceil( totalJobs / (double) jobCountsByPartition.size());

        for(int partition = 0; partition < jobCountsByPartition.size(); partition++) {
            int jobsTaken = 0;
            Set<JobWithExpressions> jobSet = new HashSet<>();

            while (!isEmpty() && jobsTaken < jobsPerPartition - jobCountsByPartition.get(partition)) {
                Set<JobWithExpressions> jobs = stream()
                        .findFirst()
                        .map(chosenJob -> {
                            if (!isPartOfMultiPhaseLinkage(Optional.of(chosenJob))) {
                                return setOf(chosenJob);
                            } else {
                                return stream()
                                        .filter(job -> job.getExperimentId().equals(chosenJob.getExperimentId()))
                                        .collect(Collectors.toSet());
                            }
                        }).orElseThrow(() -> new InvalidJobException("Split failed"));

                removeAll(jobs);
                jobSet.addAll(jobs);
                jobsTaken += jobs.size();

            };

            if(partition == jobCountsByPartition.size() - 1 && !isEmpty()) {
                Set<JobWithExpressions> jobs = new HashSet<>(this);
                this.removeAll(jobs);
                jobSet.addAll(jobs);
            }

            sets.add(jobSet);
        }
        return sets;
    }

    public Set<Job> selectJobAndReleaseFile(int assignedMemory) throws IOException, InterruptedException {

        Set<Job> topJobs = selectJob(assignedMemory, false);

        System.out.println("Job taken: " + topJobs);

        writeEntriesToFile();
        releaseAndCloseFile(Lock.JOBS);
        return topJobs;
    }

    public Set<Job> selectJob(int assignedMemory, boolean bypassPopulationNumberExplosion) {
        Optional<JobWithExpressions> topJob = takeTopJob(assignedMemory);

        if(topJob.isPresent()) {

            if(!isPartOfMultiPhaseLinkage(topJob)) {
                Set<JobWithExpressions> partiallyExplodedJobs =
                        topJob.map(JobListHelper::explodeJobWithExpressions)
                                .orElse(Collections.emptySet());
                addAll(partiallyExplodedJobs);
                topJob = takeTopJob(assignedMemory); // this time the top job will be the singular job we created in the explosion
                topJob = explodePopulationNumber(topJob);
                return setOf(topJob.map(JobMappers::map));
            } else {
                Set<JobWithExpressions> jobsInExperiment = getAllJobsWithExperimentId(topJob.get().getExperimentId());
                removeAll(jobsInExperiment);
                jobsInExperiment.add(topJob.get());
                Set<JobWithExpressions> chosenJobs = extractSingularJobSet(jobsInExperiment);
                if(!bypassPopulationNumberExplosion) {
                    chosenJobs = explodePopulationNumber(chosenJobs);
                }
                return chosenJobs.stream().map(JobMappers::map).collect(Collectors.toSet());
            }
        }

        getReferenceExpression(this);
        return new HashSet<>(); // i.e empty set as no suitable jobs
    }

    private Set<JobWithExpressions> explodePopulationNumber(Set<JobWithExpressions> jobs) {
        if(jobs.isEmpty()) {
            return jobs;
        } else {
            // check all jobs have same population numbers
            assertAllJobsHaveSamePopulationNumbersElseThrow(jobs);

            // choose a popNumber
            IntegerExpression popNumbers = getReferenceExpression(jobs);
            String chosenPopNumber = String.valueOf(popNumbers.takeValue().getValueIfSingular());

            if(!popNumbers.getValues().isEmpty()) {
                addAll(jobs.stream()
                        .map(job -> updatePopNumber(job, popNumbers.getExpression()))
                        .collect(Collectors.toSet()));
            }

            String newExperimentId = generateRandomString();
            Set<JobWithExpressions> temp = jobs.stream()
                    .map(JobWithExpressions::clone)
                    .map(job -> updateExperimentIds(job, newExperimentId))
                    .map(job -> updatePopNumber(job, chosenPopNumber))
                    .collect(Collectors.toSet());

            return temp;
        }
    }

    private JobWithExpressions updatePopNumber(JobWithExpressions job, String popNumber) {
        job.setPopNumber(popNumber);
        return job;
    }

    private Optional<JobWithExpressions> explodePopulationNumber(Optional<JobWithExpressions> topJob) {
        if(!topJob.isPresent()) {
            return Optional.empty();
        } else {
            JobWithExpressions job = topJob.get();
            IntegerExpression popNumbers = new IntegerExpression(job.getPopNumber());

            if(popNumbers.isSingular()) {
                return topJob;
            } else {
                Integer chosenPopNumber = popNumbers.takeValue().getValueIfSingular();

                // return non-selected popNumbers job to list
                JobWithExpressions otherJobs = job.clone();
                otherJobs.setPopNumber(popNumbers.getExpression());
                add(otherJobs);

                // update selected popNumber job with new experimentId
                JobWithExpressions chosenJob = job.clone();
                chosenJob.setPopNumber(String.valueOf(chosenPopNumber));
                chosenJob.setExperimentId(generateRandomString());

                return Optional.of(chosenJob);
            }
        }
    }

    private void assertAllJobsHaveSamePopulationNumbersElseThrow(Set<JobWithExpressions> chosenJobs) {
        IntegerExpression referenceExpression = getReferenceExpression(chosenJobs);

        if(!chosenJobs.stream().map(JobCore::getPopNumber).map(IntegerExpression::new)
                .allMatch(exp -> exp.equals(referenceExpression))) {
            throw new InvalidJobException("Mismatch in popNumbers for jobs in experiment");
        }
    }

    private IntegerExpression getReferenceExpression(Collection<JobWithExpressions> chosenJobs) {
        return chosenJobs.stream()
                .findFirst()
                .map(JobCore::getPopNumber)
                .map(IntegerExpression::new)
                .orElseThrow(() -> new IllegalStateException("Executed method with unexpected empty set"));
    }

    private void check(Collection<JobWithExpressions> chosenJobs) {
        chosenJobs.forEach(job -> new IntegerExpression(job.getPopNumber()) );
//                .orElseThrow(() -> new IllegalStateException("Executed method with unexpected empty set"));
    }

    private boolean isPartOfMultiPhaseLinkage(Optional<JobWithExpressions> topJob) {
        return !topJob.get().getLinkagePhase().equals("");
    }

    private Set<Job> setOf(Optional<Job> job) {
        HashSet<Job> set = new HashSet<>();
        job.ifPresent(set::add);
        return set;
    }

    private Set<JobWithExpressions> setOf(JobWithExpressions job) {
        HashSet<JobWithExpressions> set = new HashSet<>();
        set.add(job);
        return set;
    }

    public static void main(String[] args) {
        HashSet<String> values = new HashSet<>();
        values.add("1");values.add("2");values.add("3");

        HashSet<String> values2 = new HashSet<>();
        values2.add("1");values2.add("3");values2.add("2");

        System.out.println(Objects.equals(values, values2));
    }

    private Set<JobWithExpressions> extractSingularJobSet(Set<JobWithExpressions> jobsInExperiment) {

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
                explodedJobSet.addAll(updateExperimentIds(jobsForPhase));
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
                return jobs;
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

    private Collection<JobWithExpressions> updateExperimentIds(Set<JobWithExpressions> jobs) {
        jobs.forEach(job -> job.setExperimentId(generateRandomString()));
        return jobs;
    }

    private JobWithExpressions updateExperimentIds(JobWithExpressions job, String experimentId) {
        job.setExperimentId(experimentId);
        return job;
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

        check(topJobWithExpressions.map(this::setOf).orElse(new HashSet<>()));
        check(topSingularJob.map(this::setOf).orElse(new HashSet<>()));

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
