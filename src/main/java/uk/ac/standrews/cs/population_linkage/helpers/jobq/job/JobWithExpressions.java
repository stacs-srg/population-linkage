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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Objects;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.DoubleExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.DoubleExpressionToStringConverter;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.IntegerExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.IntegerExpressionToStringConverter;

public class JobWithExpressions extends JobCore {

    protected DoubleExpression threshold;
    protected IntegerExpression preFilterRequiredFields;
    protected IntegerExpression birthsCacheSize;
    protected IntegerExpression marriagesCacheSize;
    protected IntegerExpression deathsCacheSize;
    protected IntegerExpression ros;
    protected IntegerExpression maxSiblingAgeDiff;
    protected IntegerExpression minMarriageAge;
    protected IntegerExpression minParentingAge;
    protected IntegerExpression maxParentingAge;
    protected IntegerExpression maxMarriageAgeDiscrepancy;
    protected IntegerExpression maxDeathAge;

    @Override
    public String toString() {
        return "JobWithExpressions{" +
                "threshold=" + threshold +
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
                ", linkageResultsFile='" + linkageResultsFile + '\'' +
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
    public JobWithExpressions clone() {
        JobWithExpressions clone = new JobWithExpressions();
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
        clone.threshold = (DoubleExpression) threshold.clone();
        clone.preFilterRequiredFields = (IntegerExpression) preFilterRequiredFields.clone();
        clone.birthsCacheSize = (IntegerExpression) birthsCacheSize.clone();
        clone.marriagesCacheSize = (IntegerExpression) marriagesCacheSize.clone();
        clone.deathsCacheSize = (IntegerExpression) deathsCacheSize.clone();
        clone.ros = (IntegerExpression) ros.clone();
        clone.maxSiblingAgeDiff = (IntegerExpression) maxSiblingAgeDiff.clone();
        clone.minMarriageAge = (IntegerExpression) minMarriageAge.clone();
        clone.minParentingAge = (IntegerExpression) minParentingAge.clone();
        clone.maxParentingAge = (IntegerExpression) maxParentingAge.clone();
        clone.maxMarriageAgeDiscrepancy = (IntegerExpression) maxMarriageAgeDiscrepancy.clone();
        clone.maxDeathAge = (IntegerExpression) maxDeathAge.clone();
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobWithExpressions that = (JobWithExpressions) o;
        return super.equals(o) &&
                Objects.equals(threshold, that.threshold) &&
                Objects.equals(preFilterRequiredFields, that.preFilterRequiredFields) &&
                Objects.equals(birthsCacheSize, that.birthsCacheSize) &&
                Objects.equals(marriagesCacheSize, that.marriagesCacheSize) &&
                Objects.equals(deathsCacheSize, that.deathsCacheSize) &&
                Objects.equals(ros, that.ros) &&
                Objects.equals(maxSiblingAgeDiff, that.maxSiblingAgeDiff) &&
                Objects.equals(minMarriageAge, that.minMarriageAge) &&
                Objects.equals(minParentingAge, that.minParentingAge) &&
                Objects.equals(maxParentingAge, that.maxParentingAge) &&
                Objects.equals(maxMarriageAgeDiscrepancy, that.maxMarriageAgeDiscrepancy) &&
                Objects.equals(maxDeathAge, that.maxDeathAge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threshold, preFilterRequiredFields, birthsCacheSize, marriagesCacheSize, deathsCacheSize, ros, maxSiblingAgeDiff, minMarriageAge, minParentingAge, maxParentingAge, maxMarriageAgeDiscrepancy, maxDeathAge);
    }

    @JsonSerialize(converter = DoubleExpressionToStringConverter.class)
    public DoubleExpression getThreshold() {
        return threshold;
    }

    public void setThreshold(DoubleExpression threshold) {
        this.threshold = threshold;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getPreFilterRequiredFields() {
        return preFilterRequiredFields;
    }

    public void setPreFilterRequiredFields(IntegerExpression preFilterRequiredFields) {
        this.preFilterRequiredFields = preFilterRequiredFields;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getBirthsCacheSize() {
        return birthsCacheSize;
    }

    public void setBirthsCacheSize(IntegerExpression birthsCacheSize) {
        this.birthsCacheSize = birthsCacheSize;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMarriagesCacheSize() {
        return marriagesCacheSize;
    }

    public void setMarriagesCacheSize(IntegerExpression marriagesCacheSize) {
        this.marriagesCacheSize = marriagesCacheSize;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getDeathsCacheSize() {
        return deathsCacheSize;
    }

    public void setDeathsCacheSize(IntegerExpression deathsCacheSize) {
        this.deathsCacheSize = deathsCacheSize;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getRos() {
        return ros;
    }

    public void setRos(IntegerExpression ros) {
        this.ros = ros;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMaxSiblingAgeDiff() {
        return maxSiblingAgeDiff;
    }

    public void setMaxSiblingAgeDiff(IntegerExpression maxSiblingAgeDiff) {
        this.maxSiblingAgeDiff = maxSiblingAgeDiff;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMinMarriageAge() {
        return minMarriageAge;
    }

    public void setMinMarriageAge(IntegerExpression minMarriageAge) {
        this.minMarriageAge = minMarriageAge;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMinParentingAge() {
        return minParentingAge;
    }

    public void setMinParentingAge(IntegerExpression minParentingAge) {
        this.minParentingAge = minParentingAge;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMaxParentingAge() {
        return maxParentingAge;
    }

    public void setMaxParentingAge(IntegerExpression maxParentingAge) {
        this.maxParentingAge = maxParentingAge;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMaxMarriageAgeDiscrepancy() {
        return maxMarriageAgeDiscrepancy;
    }

    public void setMaxMarriageAgeDiscrepancy(IntegerExpression maxMarriageAgeDiscrepancy) {
        this.maxMarriageAgeDiscrepancy = maxMarriageAgeDiscrepancy;
    }

    @JsonSerialize(converter = IntegerExpressionToStringConverter.class)
    public IntegerExpression getMaxDeathAge() {
        return maxDeathAge;
    }

    public void setMaxDeathAge(IntegerExpression maxDeathAge) {
        this.maxDeathAge = maxDeathAge;
    }
}
