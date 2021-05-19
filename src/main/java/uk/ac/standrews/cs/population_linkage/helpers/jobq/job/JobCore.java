/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobCore {

    protected String linkageResultsFile;
    protected String reason;
    protected int priority;
    protected int requiredMemory;
    protected Long seed;
    protected String population;
    protected String size;
    protected String corruptionProfile;

    protected String metric;

    protected String linkageType;
    protected boolean preFilter;
    protected boolean persistLinks;
    protected boolean evaluateQuality;
    protected String experimentId;
    protected String linkagePhase;

    protected String popNumber = "";

    protected String evaluationApproach = "";
    protected String singlePathIndirectEvaluationApproach = "";
    protected String dualPathIndirectEvaluationApproach = "";

    @Override
    public String toString() {
        return "JobCore{" +
                "linkageResultsFile='" + linkageResultsFile + '\'' +
                ", reason='" + reason + '\'' +
                ", priority=" + priority +
                ", requiredMemory=" + requiredMemory +
                ", seed=" + seed +
                ", population='" + population + '\'' +
                ", size='" + size + '\'' +
                ", corruptionProfile='" + corruptionProfile + '\'' +
                ", metric='" + metric + '\'' +
                ", linkageType='" + linkageType + '\'' +
                ", preFilter=" + preFilter +
                ", persistLinks=" + persistLinks +
                ", evaluateQuality=" + evaluateQuality +
                ", experimentId='" + experimentId + '\'' +
                ", linkagePhase='" + linkagePhase + '\'' +
                ", singlePathIndirectEvaluationApproach='" + singlePathIndirectEvaluationApproach + '\'' +
                ", dualPathIndirectEvaluationApproach='" + dualPathIndirectEvaluationApproach + '\'' +
                '}';
    }

    @Override
    public JobCore clone() {
        JobCore clone = new JobCore();
        clone.linkageResultsFile = linkageResultsFile;
        clone.reason = reason;
        clone.priority = priority;
        clone.requiredMemory = requiredMemory;
        clone.seed = seed;
        clone.population = population;
        clone.size = size;
        clone.corruptionProfile = corruptionProfile;
        clone.metric = metric;
        clone.linkageType = linkageType;
        clone.preFilter = preFilter;
        clone.persistLinks = persistLinks;
        clone.evaluateQuality = evaluateQuality;
        clone.experimentId = experimentId;
        clone.linkagePhase = linkagePhase;
        clone.singlePathIndirectEvaluationApproach = singlePathIndirectEvaluationApproach;
        clone.dualPathIndirectEvaluationApproach = dualPathIndirectEvaluationApproach;
        clone.popNumber = popNumber;
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobCore jobCore = (JobCore) o;
        return priority == jobCore.priority &&
                requiredMemory == jobCore.requiredMemory &&
                preFilter == jobCore.preFilter &&
                persistLinks == jobCore.persistLinks &&
                evaluateQuality == jobCore.evaluateQuality &&
                Objects.equals(linkageResultsFile, jobCore.linkageResultsFile) &&
                Objects.equals(reason, jobCore.reason) &&
                Objects.equals(seed, jobCore.seed) &&
                Objects.equals(population, jobCore.population) &&
                Objects.equals(size, jobCore.size) &&
                Objects.equals(corruptionProfile, jobCore.corruptionProfile) &&
                Objects.equals(metric, jobCore.metric) &&
                Objects.equals(linkageType, jobCore.linkageType) &&
                Objects.equals(experimentId, jobCore.experimentId) &&
                Objects.equals(linkagePhase, jobCore.linkagePhase) &&
                Objects.equals(popNumber, jobCore.popNumber) &&
                Objects.equals(singlePathIndirectEvaluationApproach, jobCore.singlePathIndirectEvaluationApproach) &&
                Objects.equals(dualPathIndirectEvaluationApproach, jobCore.dualPathIndirectEvaluationApproach);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkageResultsFile, reason, priority, requiredMemory, seed, population, size, corruptionProfile, metric, linkageType, preFilter, persistLinks, evaluateQuality, experimentId, linkagePhase, popNumber, singlePathIndirectEvaluationApproach, dualPathIndirectEvaluationApproach);
    }

    public String getLinkageResultsFile() {
        return linkageResultsFile;
    }

    public void setLinkageResultsFile(String linkageResultsFile) {
        this.linkageResultsFile = linkageResultsFile;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getRequiredMemory() {
        return requiredMemory;
    }

    public void setRequiredMemory(int requiredMemory) {
        this.requiredMemory = requiredMemory;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCorruptionProfile() {
        return corruptionProfile;
    }

    public void setCorruptionProfile(String corruptionProfile) {
        this.corruptionProfile = corruptionProfile;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getLinkageType() {
        return linkageType;
    }

    public void setLinkageType(String linkageType) {
        this.linkageType = linkageType;
    }

    public boolean isPreFilter() {
        return preFilter;
    }

    public void setPreFilter(boolean preFilter) {
        this.preFilter = preFilter;
    }

    public boolean isPersistLinks() {
        return persistLinks;
    }

    public void setPersistLinks(boolean persistLinks) {
        this.persistLinks = persistLinks;
    }

    public boolean isEvaluateQuality() {
        return evaluateQuality;
    }

    public void setEvaluateQuality(boolean evaluateQuality) {
        this.evaluateQuality = evaluateQuality;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getLinkagePhase() {
        return linkagePhase;
    }

    public void setLinkagePhase(String linkagePhase) {
        this.linkagePhase = linkagePhase;
    }

    public String getEvaluationApproach() {
        return evaluationApproach;
    }

    public void setEvaluationApproach(String evaluationApproach) {
        this.evaluationApproach = evaluationApproach;
    }

    public String getSinglePathIndirectEvaluationApproach() {
        return singlePathIndirectEvaluationApproach;
    }

    public void setSinglePathIndirectEvaluationApproach(String singlePathIndirectEvaluationApproach) {
        this.singlePathIndirectEvaluationApproach = singlePathIndirectEvaluationApproach;
    }

    public String getDualPathIndirectEvaluationApproach() {
        return dualPathIndirectEvaluationApproach;
    }

    public void setDualPathIndirectEvaluationApproach(String dualPathIndirectEvaluationApproach) {
        this.dualPathIndirectEvaluationApproach = dualPathIndirectEvaluationApproach;
    }

    public String getPopNumber() {
        return popNumber;
    }

    public void setPopNumber(String popNumber) {
        this.popNumber = popNumber;
    }
}
