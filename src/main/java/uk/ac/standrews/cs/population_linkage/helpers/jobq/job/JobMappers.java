package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.DoubleExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.IntegerExpression;

public class JobMappers {

    public static Job map(JobWithExpressions jobWithExpressions) {
        if(JobListHelper.isSingularJob(jobWithExpressions)) {
            Job job = new Job();
            job.setLinkageResultsFile(jobWithExpressions.getLinkageResultsFile());
            job.setReason(jobWithExpressions.getReason());
            job.setPriority(jobWithExpressions.getPriority());
            job.setRequiredMemory(jobWithExpressions.getRequiredMemory());
            job.setSeed(jobWithExpressions.getSeed());
            job.setPopulation(jobWithExpressions.getPopulation());
            job.setSize(jobWithExpressions.getSize());
            job.setPopNumber(jobWithExpressions.getPopNumber());
            job.setCorruptionProfile(jobWithExpressions.getCorruptionProfile());
            job.setMetric(jobWithExpressions.getMetric());
            job.setLinkageType(jobWithExpressions.getLinkageType());
            job.setPreFilter(jobWithExpressions.isPreFilter());
            job.setPersistLinks(jobWithExpressions.isPersistLinks());
            job.setEvaluateQuality(jobWithExpressions.isEvaluateQuality());
            job.setThreshold(jobWithExpressions.getThreshold().getValueIfSingular());
            job.setPreFilterRequiredFields(jobWithExpressions.getPreFilterRequiredFields().getValueIfSingular());
            job.setBirthsCacheSize(jobWithExpressions.getBirthsCacheSize().getValueIfSingular());
            job.setMarriagesCacheSize(jobWithExpressions.getMarriagesCacheSize().getValueIfSingular());
            job.setDeathsCacheSize(jobWithExpressions.getDeathsCacheSize().getValueIfSingular());
            job.setRos(jobWithExpressions.getRos().getValueIfSingular());
            job.setMaxSiblingAgeDiff(jobWithExpressions.getMaxSiblingAgeDiff().getValueIfSingular());
            job.setMinMarriageAge(jobWithExpressions.getMinMarriageAge().getValueIfSingular());
            job.setMinParentingAge(jobWithExpressions.getMinParentingAge().getValueIfSingular());
            job.setMaxParentingAge(jobWithExpressions.getMaxParentingAge().getValueIfSingular());
            job.setMaxMarriageAgeDiscrepancy(jobWithExpressions.getMaxMarriageAgeDiscrepancy().getValueIfSingular());
            job.setMaxDeathAge(jobWithExpressions.getMaxDeathAge().getValueIfSingular());
            return job;
        } else {
            throw new NotSingularJobException(String.format("Job not singular: %s", jobWithExpressions));
        }
    }

    public static JobWithExpressions map(Job job) {
        JobWithExpressions jobWithExpressions = new JobWithExpressions();
        jobWithExpressions.setLinkageResultsFile(job.getLinkageResultsFile());
        jobWithExpressions.setReason(job.getReason());
        jobWithExpressions.setPriority(job.getPriority());
        jobWithExpressions.setRequiredMemory(job.getRequiredMemory());
        jobWithExpressions.setSeed(job.getSeed());
        jobWithExpressions.setPopulation(job.getPopulation());
        jobWithExpressions.setSize(job.getSize());
        jobWithExpressions.setPopNumber(job.getPopNumber());
        jobWithExpressions.setCorruptionProfile(job.getCorruptionProfile());
        jobWithExpressions.setMetric(job.getMetric());
        jobWithExpressions.setLinkageType(job.getLinkageType());
        jobWithExpressions.setPreFilter(job.isPreFilter());
        jobWithExpressions.setPersistLinks(job.isPersistLinks());
        jobWithExpressions.setEvaluateQuality(job.isEvaluateQuality());
        jobWithExpressions.setThreshold(new DoubleExpression(job.getThreshold()));
        jobWithExpressions.setPreFilterRequiredFields(new IntegerExpression(job.getPreFilterRequiredFields()));
        jobWithExpressions.setBirthsCacheSize(new IntegerExpression(job.getBirthsCacheSize()));
        jobWithExpressions.setMarriagesCacheSize(new IntegerExpression(job.getMarriagesCacheSize()));
        jobWithExpressions.setDeathsCacheSize(new IntegerExpression(job.getDeathsCacheSize()));
        jobWithExpressions.setRos(new IntegerExpression(job.getRos()));
        jobWithExpressions.setMaxSiblingAgeDiff(new IntegerExpression(job.getMaxSiblingAgeDiff()));
        jobWithExpressions.setMinMarriageAge(new IntegerExpression(job.getMinMarriageAge()));
        jobWithExpressions.setMinParentingAge(new IntegerExpression(job.getMinParentingAge()));
        jobWithExpressions.setMaxParentingAge(new IntegerExpression(job.getMaxParentingAge()));
        jobWithExpressions.setMaxMarriageAgeDiscrepancy(new IntegerExpression(job.getMaxMarriageAgeDiscrepancy()));
        jobWithExpressions.setMaxDeathAge(new IntegerExpression(job.getMaxDeathAge()));
        return jobWithExpressions;
    }

}
