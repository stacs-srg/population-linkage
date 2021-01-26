package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import java.util.Objects;

public class Job extends JobCore{

    protected double threshold;
    protected int preFilterRequiredFields;
    protected int birthsCacheSize;
    protected int marriagesCacheSize;
    protected int deathsCacheSize;
    protected int ros;
    protected int maxSiblingAgeDiff;
    protected int minMarriageAge;
    protected int minParentingAge;
    protected int maxParentingAge;
    protected int maxMarriageAgeDiscrepancy;
    protected int maxDeathAge;

    @Override
    public String toString() {
        return "Job{" +
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Job job = (Job) o;
        return super.equals(o) &&
                Double.compare(job.threshold, threshold) == 0 &&
                preFilterRequiredFields == job.preFilterRequiredFields &&
                birthsCacheSize == job.birthsCacheSize &&
                marriagesCacheSize == job.marriagesCacheSize &&
                deathsCacheSize == job.deathsCacheSize &&
                ros == job.ros &&
                maxSiblingAgeDiff == job.maxSiblingAgeDiff &&
                minMarriageAge == job.minMarriageAge &&
                minParentingAge == job.minParentingAge &&
                maxParentingAge == job.maxParentingAge &&
                maxMarriageAgeDiscrepancy == job.maxMarriageAgeDiscrepancy &&
                maxDeathAge == job.maxDeathAge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), threshold, preFilterRequiredFields, birthsCacheSize, marriagesCacheSize, deathsCacheSize, ros, maxSiblingAgeDiff, minMarriageAge, minParentingAge, maxParentingAge, maxMarriageAgeDiscrepancy, maxDeathAge);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getPreFilterRequiredFields() {
        return preFilterRequiredFields;
    }

    public void setPreFilterRequiredFields(int preFilterRequiredFields) {
        this.preFilterRequiredFields = preFilterRequiredFields;
    }

    public int getBirthsCacheSize() {
        return birthsCacheSize;
    }

    public void setBirthsCacheSize(int birthsCacheSize) {
        this.birthsCacheSize = birthsCacheSize;
    }

    public int getMarriagesCacheSize() {
        return marriagesCacheSize;
    }

    public void setMarriagesCacheSize(int marriagesCacheSize) {
        this.marriagesCacheSize = marriagesCacheSize;
    }

    public int getDeathsCacheSize() {
        return deathsCacheSize;
    }

    public void setDeathsCacheSize(int deathsCacheSize) {
        this.deathsCacheSize = deathsCacheSize;
    }

    public int getRos() {
        return ros;
    }

    public void setRos(int ros) {
        this.ros = ros;
    }

    public int getMaxSiblingAgeDiff() {
        return maxSiblingAgeDiff;
    }

    public void setMaxSiblingAgeDiff(int maxSiblingAgeDiff) {
        this.maxSiblingAgeDiff = maxSiblingAgeDiff;
    }

    public int getMinMarriageAge() {
        return minMarriageAge;
    }

    public void setMinMarriageAge(int minMarriageAge) {
        this.minMarriageAge = minMarriageAge;
    }

    public int getMinParentingAge() {
        return minParentingAge;
    }

    public void setMinParentingAge(int minParentingAge) {
        this.minParentingAge = minParentingAge;
    }

    public int getMaxParentingAge() {
        return maxParentingAge;
    }

    public void setMaxParentingAge(int maxParentingAge) {
        this.maxParentingAge = maxParentingAge;
    }

    public int getMaxMarriageAgeDiscrepancy() {
        return maxMarriageAgeDiscrepancy;
    }

    public void setMaxMarriageAgeDiscrepancy(int maxMarriageAgeDiscrepancy) {
        this.maxMarriageAgeDiscrepancy = maxMarriageAgeDiscrepancy;
    }

    public int getMaxDeathAge() {
        return maxDeathAge;
    }

    public void setMaxDeathAge(int maxDeathAge) {
        this.maxDeathAge = maxDeathAge;
    }
}
