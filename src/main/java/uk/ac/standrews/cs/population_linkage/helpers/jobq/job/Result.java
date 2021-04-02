/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;

import static java.lang.String.valueOf;

public class Result extends Job {

    private long startTime;
    private boolean corrupted;

    private long linksLostOnPreFilter;
    private long tp;
    private long fp;
    private long fn;
    private double precision;
    private double recall;
    private double fMeasure;
    private int timeTakeSeconds;

    private String linkageClass;
    private String fieldsUsed1;
    private String fieldsUsed2;

    private EvaluationApproach.Type evaluationApproach;

    // defined for CSV mapper to work
    private String recordsRepo;
    private String resultsRepo;
    private String linksSubRepo;
    private Double maxMemoryUsage;
    private String codeVersion;
    private String hostname;

    private Integer linkageConfigurationHash = null;

    public Result clone() {
        Result clone = new Result();
        clone.linksLostOnPreFilter = this.linksLostOnPreFilter;
        clone.startTime = this.startTime;
        clone.corrupted = this.corrupted;
        clone.tp = this.tp;
        clone.fp = this.fp;
        clone.fn = this.fn;
        clone.precision = this.precision;
        clone.recall = this.recall;
        clone.fMeasure = this.fMeasure;
        clone.timeTakeSeconds = this.timeTakeSeconds;
        clone.linkageClass = this.linkageClass;
        clone.fieldsUsed1 = this.fieldsUsed1;
        clone.fieldsUsed2 = this.fieldsUsed2;
        clone.recordsRepo = this.recordsRepo;
        clone.resultsRepo = this.resultsRepo;
        clone.linksSubRepo = this.linksSubRepo;
        clone.maxMemoryUsage = this.maxMemoryUsage;
        clone.codeVersion = this.codeVersion;
        clone.hostname = this.hostname;
        clone.threshold = this.threshold;
        clone.preFilterRequiredFields = this.preFilterRequiredFields;
        clone.birthsCacheSize = this.birthsCacheSize;
        clone.marriagesCacheSize = this.marriagesCacheSize;
        clone.deathsCacheSize = this.deathsCacheSize;
        clone.ros = this.ros;
        clone.maxSiblingAgeDiff = this.maxSiblingAgeDiff;
        clone.minMarriageAge = this.minMarriageAge;
        clone.minParentingAge = this.minParentingAge;
        clone.maxParentingAge = this.maxParentingAge;
        clone.maxMarriageAgeDiscrepancy = this.maxMarriageAgeDiscrepancy;
        clone.maxDeathAge = this.maxDeathAge;
        clone.linkageResultsFile = this.linkageResultsFile;
        clone.reason = this.reason;
        clone.priority = this.priority;
        clone.requiredMemory = this.requiredMemory;
        clone.seed = this.seed;
        clone.population = this.population;
        clone.size = this.size;
        clone.popNumber = this.popNumber;
        clone.corruptionProfile = this.corruptionProfile;
        clone.metric = this.metric;
        clone.linkageType = this.linkageType;
        clone.preFilter = this.preFilter;
        clone.persistLinks = this.persistLinks;
        clone.evaluateQuality = this.evaluateQuality;
        clone.evaluationApproach = this.evaluationApproach;
        clone.singlePathIndirectEvaluationApproach = this.singlePathIndirectEvaluationApproach;
        clone.dualPathIndirectEvaluationApproach = this.dualPathIndirectEvaluationApproach;
        clone.linkagePhase = this.linkagePhase;
        clone.experimentId = this.experimentId;
        clone.linkageConfigurationHash = this.linkageConfigurationHash;
        return clone;
    }

    @Override
    public String toString() {
        return "Result{" +
                "startTime=" + startTime +
                ", corrupted=" + corrupted +
                ", linksLostOnPreFilter=" + linksLostOnPreFilter +
                ", tp=" + tp +
                ", fp=" + fp +
                ", fn=" + fn +
                ", precision=" + precision +
                ", recall=" + recall +
                ", fMeasure=" + fMeasure +
                ", timeTakeSeconds=" + timeTakeSeconds +
                ", linkageClass='" + linkageClass + '\'' +
                ", fieldsUsed1='" + fieldsUsed1 + '\'' +
                ", fieldsUsed2='" + fieldsUsed2 + '\'' +
                ", evaluationApproach=" + evaluationApproach +
                ", recordsRepo='" + recordsRepo + '\'' +
                ", resultsRepo='" + resultsRepo + '\'' +
                ", linksSubRepo='" + linksSubRepo + '\'' +
                ", maxMemoryUsage=" + maxMemoryUsage +
                ", codeVersion='" + codeVersion + '\'' +
                ", hostname='" + hostname + '\'' +
                ", threshold=" + threshold +
                ", preFilterRequiredFields=" + preFilterRequiredFields +
                ", birthsCacheSize=" + birthsCacheSize +
                ", marriagesCacheSize=" + marriagesCacheSize +
                ", deathsCacheSize=" + deathsCacheSize +
                ", ros=" + ros +
                ", maxSiblingAgeDiff=" + maxSiblingAgeDiff +
                ", minMarriageAge=" + minMarriageAge +
                ", minParentingAge=" + minParentingAge +
                ", maxParentingAge=" + maxParentingAge +
                ", maxMarriageAgeDiscrepancy=" + maxMarriageAgeDiscrepancy +
                ", maxDeathAge=" + maxDeathAge +
                ", popNumber=" + popNumber +
                ", linkageResultsFile='" + linkageResultsFile + '\'' +
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Result result = (Result) o;
        return startTime == result.startTime &&
                corrupted == result.corrupted &&
                linksLostOnPreFilter == result.linksLostOnPreFilter &&
                tp == result.tp &&
                fp == result.fp &&
                fn == result.fn &&
                Double.compare(result.precision, precision) == 0 &&
                Double.compare(result.recall, recall) == 0 &&
                Double.compare(result.fMeasure, fMeasure) == 0 &&
                timeTakeSeconds == result.timeTakeSeconds &&
                Objects.equals(linkageClass, result.linkageClass) &&
                Objects.equals(fieldsUsed1, result.fieldsUsed1) &&
                Objects.equals(fieldsUsed2, result.fieldsUsed2) &&
                evaluationApproach == result.evaluationApproach &&
                Objects.equals(recordsRepo, result.recordsRepo) &&
                Objects.equals(resultsRepo, result.resultsRepo) &&
                Objects.equals(linksSubRepo, result.linksSubRepo) &&
                Objects.equals(maxMemoryUsage, result.maxMemoryUsage) &&
                Objects.equals(codeVersion, result.codeVersion) &&
                Objects.equals(hostname, result.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startTime, corrupted, linksLostOnPreFilter, tp, fp, fn, precision, recall, fMeasure, timeTakeSeconds, linkageClass, fieldsUsed1, fieldsUsed2, evaluationApproach, recordsRepo, resultsRepo, linksSubRepo, maxMemoryUsage, codeVersion, hostname);
    }

    public String getCodeVersion() {
        if(codeVersion == null) {
            try {
                return execCmd("git rev-parse HEAD").trim();
            } catch (IOException e) {
                return "NA";
            }
        } else {
            return codeVersion;
        }
    }

    public String getHostname() {
        if(hostname == null) {
            try {
                return execCmd("hostname").trim();
            } catch (IOException e) {
                return "NA";
            }
        } else {
            return hostname;
        }
    }

    public String getResultsRepo() {
        return String.join("_", getPopulation(), valueOf(getPopNumber()), getCorruptionProfile(), getLinkageType(), "links");
    }

    public String getLinksSubRepo() {
        return String.join("_", getReason(), getMetric(), valueOf(getThreshold()), formatDateTime(getStartTime()));
    }

    private String formatDateTime(long epochTime) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace(":","-");
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isCorrupted() {
        return !getCorruptionProfile().equals("0");
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    public String getRecordsRepo() {
        if (getSize().equals("-") && getCorruptionProfile().equals("-"))
            return getPopulation();

        if (isCorrupted())
            return getPopulation() + "_" + getSize() + "_" + getPopNumber() + "_corrupted_" + getCorruptionProfile();
        else {
            return getPopulation() + "_" + getSize() + "_" + getPopNumber() + "_clean";
        }
    }

    public int getLinkageConfigurationHash() {
        if(linkageConfigurationHash == null) {
            return Objects.hash(linkageClass, fieldsUsed1, fieldsUsed2, metric,threshold,preFilterRequiredFields,maxSiblingAgeDiff,minMarriageAge,minParentingAge,maxParentingAge,maxMarriageAgeDiscrepancy,maxDeathAge);
        }
        return linkageConfigurationHash;
    }

    public void setLinkageConfigurationHash(int linkageConfigurationHash) {
        this.linkageConfigurationHash = linkageConfigurationHash;
    }

    public long getTp() {
        return tp;
    }

    public void setTp(long tp) {
        this.tp = tp;
    }

    public long getFp() {
        return fp;
    }

    public void setFp(long fp) {
        this.fp = fp;
    }

    public long getFn() {
        return fn;
    }

    public void setFn(long fn) {
        this.fn = fn;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getfMeasure() {
        return fMeasure;
    }

    public void setfMeasure(double fMeasure) {
        this.fMeasure = fMeasure;
    }

    public int getTimeTakeSeconds() {
        return timeTakeSeconds;
    }

    public void setTimeTakeSeconds(int timeTakeSeconds) {
        this.timeTakeSeconds = timeTakeSeconds;
    }

    public void calculateTimeTakeSeconds(long endTime) {
        this.timeTakeSeconds = Long.valueOf((endTime - startTime) / 1000).intValue();
    }

    public String getLinkageClass() {
        return linkageClass;
    }

    public void setLinkageClass(String linkageClass) {
        this.linkageClass = linkageClass;
    }

    public double getMaxMemoryUsage() {
        if(maxMemoryUsage == null) {
            return MemoryLogger.getMax() / 1000000000.0;
        } else {
            return maxMemoryUsage;
        }
    }

    public EvaluationApproach.Type getEvaluationApproach() {
        return evaluationApproach;
    }

    public void setEvaluationApproach(EvaluationApproach.Type evaluationApproach) {
        this.evaluationApproach = evaluationApproach;
    }

    public String getFieldsUsed1() {
        return fieldsUsed1;
    }

    public void setFieldsUsed1(String fieldsUsed1) {
        this.fieldsUsed1 = fieldsUsed1;
    }

    public String getFieldsUsed2() {
        return fieldsUsed2;
    }

    public void setFieldsUsed2(String fieldsUsed2) {
        this.fieldsUsed2 = fieldsUsed2;
    }

    private static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public long getLinksLostOnPreFilter() {
        return linksLostOnPreFilter;
    }

    public void setLinksLostOnPreFilter(long linksLostOnPreFilter) {
        this.linksLostOnPreFilter = linksLostOnPreFilter;
    }
}
