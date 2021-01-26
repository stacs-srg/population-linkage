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

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.MemoryLogger;

import static java.lang.String.valueOf;

public class Result extends Job {

    private long startTime;
    private boolean corrupted;
    private int tp;
    private int fp;
    private int fn;
    private double precision;
    private double recall;
    private double fMeasure;
    private int timeTakeSeconds;
    private String linkageClass;
    private String fieldsUsed1;
    private String fieldsUsed2;

    // defined for CSV mapper to work
    private String recordsRepo;
    private String resultsRepo;
    private String linksSubRepo;
    private double maxMemoryUsage;
    private String codeVersion;
    private String hostname;

    public Result() {
    }

    public Result(Job job) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.map(job, this);
        corrupted = !getCorruptionProfile().equals("0");
    }

    @Override
    public String toString() {
        return "Result{" +
                "startTime='" + startTime + '\'' +
                ", corrupted=" + corrupted +
//                ", resultsRepo='" + resultsRepo + '\'' +
                ", tp=" + tp +
                ", fp=" + fp +
                ", fn=" + fn +
                ", precision=" + precision +
                ", recall=" + recall +
                ", fMeasure=" + fMeasure +
                ", timeTakeSeconds=" + timeTakeSeconds +
                ", linkageClass='" + linkageClass + '\'' +
//                ", maxMemoryUsage=" + maxMemoryUsage +
//                ", codeVersion='" + codeVersion + '\'' +
//                ", hostname='" + hostname + '\'' +
                ", fieldsUsed1='" + fieldsUsed1 + '\'' +
                ", fieldsUsed2='" + fieldsUsed2 + '\'' +
                ", job=" + super.toString() + "} " ;
    }

    public String getCodeVersion() {
        try {
            return execCmd("git rev-parse HEAD").trim();
        } catch (IOException e) {
            return "NA";
        }
    }

    public String getHostname() {
        try {
            return execCmd("hostname").trim();
        } catch (IOException e) {
            return "NA";
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
        return corrupted;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    public String getRecordsRepo() {
        if (getSize().equals("-") && getPopNumber().equals("-") && getCorruptionProfile().equals("-"))
            return getPopulation();

        if (corrupted)
            return getPopulation() + "_" + getSize() + "_" + getPopNumber() + "_corrupted_" + getCorruptionProfile();
        else {
            return getPopulation() + "_" + getSize() + "_" + getPopNumber() + "_clean";
        }
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public int getFp() {
        return fp;
    }

    public void setFp(int fp) {
        this.fp = fp;
    }

    public int getFn() {
        return fn;
    }

    public void setFn(int fn) {
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
        return MemoryLogger.getMax()/1000000000.0;
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
}
