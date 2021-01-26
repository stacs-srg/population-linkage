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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobCore {

    protected String linkageResultsFile;
    protected String reason;
    protected int priority;
    protected int requiredMemory;
    protected Long seed;
    protected String population;
    protected String size;
    protected String popNumber;
    protected String corruptionProfile;
    protected String metric;
    protected String linkageType;
    protected boolean preFilter;
    protected boolean persistLinks;
    protected boolean evaluateQuality;

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
                ", popNumber='" + popNumber + '\'' +
                ", corruptionProfile='" + corruptionProfile + '\'' +
                ", metric='" + metric + '\'' +
                ", linkageType='" + linkageType + '\'' +
                ", preFilter=" + preFilter +
                ", persistLinks=" + persistLinks +
                ", evaluateQuality=" + evaluateQuality +
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
        clone.popNumber = popNumber;
        clone.corruptionProfile = corruptionProfile;
        clone.metric = metric;
        clone.linkageType = linkageType;
        clone.preFilter = preFilter;
        clone.persistLinks = persistLinks;
        clone.evaluateQuality = evaluateQuality;
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
                Objects.equals(popNumber, jobCore.popNumber) &&
                Objects.equals(corruptionProfile, jobCore.corruptionProfile) &&
                Objects.equals(metric, jobCore.metric) &&
                Objects.equals(linkageType, jobCore.linkageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkageResultsFile, reason, priority, requiredMemory, seed, population, size, popNumber, corruptionProfile, metric, linkageType, preFilter, persistLinks, evaluateQuality);
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

    public String getPopNumber() {
        return popNumber;
    }

    public void setPopNumber(String popNumber) {
        this.popNumber = popNumber;
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
}
