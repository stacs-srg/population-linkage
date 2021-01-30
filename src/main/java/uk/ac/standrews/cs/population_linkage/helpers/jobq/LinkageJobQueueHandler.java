/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

import java.io.IOException;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.EntitiesList;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.Job;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobCore;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobList;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobMappers;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.JobWithExpressions;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.Result;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.helpers.ValidatePopulationInStorr;
import uk.ac.standrews.cs.population_linkage.helpers.memorylogger.PreEmptiveOutOfMemoryWarning;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthFatherIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthMotherIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBrideIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathBrideOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.FatherGroomIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthGroomIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import static uk.ac.standrews.cs.population_linkage.helpers.StatusFileHandler.getStatus;

public class LinkageJobQueueHandler {

    // to use the linkage job queue handler define a csv job file with the following headings
    // linkage-results-file,reason,priority,required-memory,seed,population,size,pop-number,corruption-profile,threshold,metric,linkage-type,results-repo,links-persistent-name,pre-filter,pre-filter-required-fields,persist-links,evaluate-quality,births-cache-size,marriages-cache-size,deaths-cache-size,ros,max-sibling-age-diff,min-marriage-age,min-parenting-age,max-parenting-age,max-marriage-age-discrepancy,max-death-age
    //
    // The job queue can be used for both synthetic populations and the umea data
    // in the case of umea specify the population as 'umea' and put a dash in each of size,pop_number,corruption_number
    //
    // Linkage type defines the 'type' of linkage to be performed - the provided string should be the same as the
    // linkageType field in the relevant linkage runner class

    public static void main(String[] args) throws Exception {
        int assignedMemory = Integer.parseInt(args[0]);
        String jobQ = args[1];
        Path recordCountsFile = Paths.get(args[2]);
        Path statusFile = Paths.get(args[3]);
        Path gtLinksFile = Paths.get(args[4]);

        while(getStatus(statusFile)) {

            JobList jobs = new JobList(jobQ);
            Optional<Job> maybeJob = jobs.selectJobAndReleaseFile(assignedMemory);

            if (maybeJob.isPresent()) {
                setLinkageConfig(maybeJob.get());
                validatePopulationRecordsAreInStorrAndIfNotImport(recordCountsFile, maybeJob.get());
                runLinkageExperiment(maybeJob.get(), jobQ, assignedMemory, gtLinksFile);

                MemoryLogger.reset();
            } else {
                System.out.println("No suitable jobs in job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                Thread.sleep(60000);
            }
        }
    }

    private static void runLinkageExperiment(Job job, String jobQ, int assignedMemory, Path gtLinksFile) throws BucketException, java.io.IOException {
        StringMetric chosenMetric = Constants.get(job.getMetric(), 4096);

        Result result = new Result(job);
        result.setStartTime(System.currentTimeMillis());

        LinkageRecipe linkageRecipe = getLinkageRecipe(job.getLinkageType(), result.getResultsRepo(), result.getLinksSubRepo(), result.getRecordsRepo());

        LinkageQuality linkageQuality;
        try {
            linkageQuality = new BitBlasterLinkageRunner().run(
                    linkageRecipe, chosenMetric, job.getThreshold().doubleValue(), job.isPreFilter(), job.getPreFilterRequiredFields(),
                    false, false, job.isEvaluateQuality(), job.isPersistLinks(), gtLinksFile).getLinkageQuality();
        } catch(PreEmptiveOutOfMemoryWarning e) {
            returnJobToJobList(JobMappers.map(job), jobQ, assignedMemory);
            return;
        }

        result.calculateTimeTakeSeconds(System.currentTimeMillis());

        result.setFieldsUsed1(getLinkageFields(1, linkageRecipe));
        result.setFieldsUsed2(getLinkageFields(2, linkageRecipe));
        result.setTp(linkageQuality.getTp());
        result.setFp(linkageQuality.getFp());
        result.setFn(linkageQuality.getFn());
        result.setPrecision(linkageQuality.getPrecision());
        result.setRecall(linkageQuality.getRecall());
        result.setfMeasure(linkageQuality.getF_measure());
        result.setLinkageClass(linkageRecipe.getClass().getCanonicalName());

        EntitiesList<Result> results = new EntitiesList<>(Result.class, result.getLinkageResultsFile());
        results.add(result);
        results.writeEntriesToFile();
        results.releaseAndCloseFile();
    }

    private static void returnJobToJobList(JobWithExpressions job, String jobQ, int assignedMemory) throws IOException {
        int updatedMemoryRequirement = (int) Math.ceil(assignedMemory * 1.1);
        job.setRequiredMemory(updatedMemoryRequirement);

        JobList jobs = new JobList(jobQ);
        jobs.add(job);
        jobs.writeEntriesToFile();
        jobs.releaseAndCloseFile();
    }

    private static void validatePopulationRecordsAreInStorrAndIfNotImport(Path recordCountsFile, JobCore job) throws Exception {
        // validate the data is in the storr (local scratch space on clusters - but either way it's defined in application.properties)
        new ValidatePopulationInStorr(job.getPopulation(), job.getSize(), job.getPopNumber(), job.getCorruptionProfile())
                .validate(recordCountsFile);
    }

    private static void setLinkageConfig(Job job) {
        LinkageConfig.birthCacheSize = job.getBirthsCacheSize();
        LinkageConfig.marriageCacheSize = job.getMarriagesCacheSize();
        LinkageConfig.deathCacheSize = job.getDeathsCacheSize();
        LinkageConfig.numberOfROs = job.getRos();
        LinkageConfig.seed = job.getSeed();

        LinkageConfig.MAX_SIBLING_AGE_DIFF = job.getMaxSiblingAgeDiff();
        LinkageConfig.MIN_AGE_AT_MARRIAGE = job.getMinMarriageAge();
        LinkageConfig.MIN_PARENT_AGE_AT_BIRTH = job.getMinParentingAge();
        LinkageConfig.MAX_PARENT_AGE_AT_BIRTH = job.getMaxParentingAge();
        LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY = job.getMaxMarriageAgeDiscrepancy();
        LinkageConfig.MAX_AGE_AT_DEATH = job.getMaxDeathAge();
    }


    private static LinkageRecipe getLinkageRecipe(final String linkageType, final String resultsRepo, final String links_persistent_name, final String sourceRepo) {

        // TODO Replace with reflective call.

        switch (linkageType) {
            case BirthSiblingLinkageRecipe.LINKAGE_TYPE:
                return new BirthSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthDeathIdentityLinkageRecipe.LINKAGE_TYPE:
                return new BirthDeathIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthDeathSiblingLinkageRecipe.LINKAGE_TYPE:
                return new BirthDeathSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthFatherIdentityLinkageRecipe.LINKAGE_TYPE:
                return new BirthFatherIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthMotherIdentityLinkageRecipe.LINKAGE_TYPE:
                return new BirthMotherIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthParentsMarriageLinkageRecipe.LINKAGE_TYPE:
                return new BirthParentsMarriageLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthBrideIdentityLinkageRecipe.LINKAGE_TYPE:
                return new BirthBrideIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BrideBrideSiblingLinkageRecipe.LINKAGE_TYPE:
                return new BrideBrideSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BrideGroomSiblingLinkageRecipe.LINKAGE_TYPE:
                return new BrideGroomSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathBrideOwnMarriageIdentityLinkageRecipe.LINKAGE_TYPE:
                return new DeathBrideOwnMarriageIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathSiblingLinkageRecipe.LINKAGE_TYPE:
                return new DeathSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathGroomOwnMarriageIdentityLinkageRecipe.LINKAGE_TYPE:
                return new DeathGroomOwnMarriageIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case FatherGroomIdentityLinkageRecipe.LINKAGE_TYPE:
                return new FatherGroomIdentityLinkageRecipe(links_persistent_name, sourceRepo, resultsRepo);
            case BirthGroomIdentityLinkageRecipe.LINKAGE_TYPE:
                return new BirthGroomIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case GroomGroomSiblingLinkageRecipe.LINKAGE_TYPE:
                return new GroomGroomSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            default:
                throw new RuntimeException("LinkageType not found");
        }
    }

    private static String getLinkageFields(int n, LinkageRecipe linkageRecipe) { //String links_persistent_name, String gt_persistent_name, String sourceRepo, String resultsRepo) {

        Class<? extends LXP> record_type;
        List<Integer> fields;

        if (n == 1) {
            record_type = linkageRecipe.getStoredType();
            fields = linkageRecipe.getLinkageFields();
        } else {
            record_type = linkageRecipe.getSearchType();
            fields = linkageRecipe.getSearchMappingFields();
        }

        List<String> recordLabels = getRecordLabels(record_type);

        return Constants.stringRepresentationOf(fields, record_type, recordLabels);
    }

    private static List<String> getRecordLabels(Class<? extends LXP> record_type) {

        if (record_type.equals(Birth.class)) {
            return Birth.getLabels();
        }

        if (record_type.equals(Marriage.class)) {
            return Marriage.getLabels();
        }

        if (record_type.equals(Death.class)) {
            return Death.getLabels();
        }

        throw new RuntimeException("Record type not resolved:" + record_type);
    }

}
