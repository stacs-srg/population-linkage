/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.EntitiesList;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobList;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobWithExpressions;


public class JobQueueSplitter {

    /**
     * job/file/to/split.csv target/dir/ sif,hogun 11
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String jobQ = args[0];
        String outputDir = args[1];
        Set<String> hosts = setOf(args[2]);
        int nodesPerHost = Integer.parseInt(args[3]);
        boolean wipeExistingJobs = Boolean.parseBoolean(args[4]);

        int partitions = hosts.size() * nodesPerHost;

        int jobsRemaining = checkRemainingJobs(outputDir, hosts, nodesPerHost, wipeExistingJobs);

        JobList jobs = new JobList(jobQ);

        jobs.explodeAllJobs();
        List<Set<JobWithExpressions>> jobSets = jobs.splitJobList(partitions);

        int totalJobs = Math.toIntExact(jobSets.stream().mapToLong(Collection::size).sum()) + jobsRemaining;
        int finalPartitionSize = totalJobs / partitions;

        int partition = 0;

        for(String host : hosts) {
            for(int node = 1; node <= nodesPerHost; node++) {
                String jobFileName = outputDir.concat("/").concat(host).concat("-").concat(String.valueOf(node)).concat("-job-list.csv");
                JobList jobList = new JobList(jobFileName, EntitiesList.Lock.BYPASS);
                if(wipeExistingJobs) {
                    jobList.clear();
                }
                final int n = node;
                jobList.addAll(jobSets.get(partition++)
                        .stream()
                        .map(job -> modifyResultFilePath(job, host, n))
                        .collect(Collectors.toSet()));
                jobList.writeEntriesToFile();
                jobList.releaseAndCloseFile(EntitiesList.Lock.BYPASS);
            }
        }
        jobs.releaseAndCloseFile(EntitiesList.Lock.JOBS);
    }

    private static int checkRemainingJobs(String outputDir, Set<String> hosts, int nodesPerHost, boolean wipeExistingJobs) throws IOException, InterruptedException {
        int remainingJobs = 0;

        if(wipeExistingJobs) {
            return remainingJobs;
        }

        for(String host : hosts) {
            for(int node = 1; node <= nodesPerHost; node++) {
                String jobFileName = outputDir.concat("/").concat(host).concat("-").concat(String.valueOf(node)).concat("-job-list.csv");
                JobList jobList = new JobList(jobFileName, EntitiesList.Lock.BYPASS);
                remainingJobs += jobList.size();
                jobList.releaseAndCloseFile(EntitiesList.Lock.BYPASS);
            }
        }

        return remainingJobs;
    }

    private static JobWithExpressions modifyResultFilePath(JobWithExpressions job, String hostname, int node) {
        String[] split = job.getLinkageResultsFile().split("\\.", 2);
        if(split.length == 2) {
            job.setLinkageResultsFile(split[0].concat("-").concat(hostname).concat("-").concat(String.valueOf(node)).concat(".").concat(split[1]));
        } else {
            job.setLinkageResultsFile(split[0].concat("-").concat(hostname).concat("-").concat(String.valueOf(node)).concat(".csv"));
        }
        return job;
    }

    private static Set<String> setOf(String csv) {
        return Arrays.stream(csv.split(",")).collect(Collectors.toSet());
    }

}
