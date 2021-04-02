/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.standrews.cs.population_linkage.compositeLinker.DualPathIndirectLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.compositeLinker.IndirectLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.compositeLinker.SinglePathIndirectLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.StringExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.EntitiesList;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.job.InvalidJobException;
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
import uk.ac.standrews.cs.population_linkage.linkageRecipes.ReversedLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Utils;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.StandardEvaluationApproach;
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
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import static uk.ac.standrews.cs.population_linkage.helpers.StatusFileHandler.getStatus;
import static uk.ac.standrews.cs.population_linkage.helpers.jobq.job.IndirectLinkageRecipeHelper.*;

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
        LinkageConfig.GT_COUNTS_FILE = Paths.get(args[4]);


        while(getStatus(statusFile)) {
            JobList jobs = new JobList(jobQ);
            Set<Job> maybeJobs = jobs.selectJobAndReleaseFile(assignedMemory);
            validatePopulationRecordsAreInStorrAndIfNotImport(recordCountsFile, maybeJobs);

            try {
                if (maybeJobs.size() == 1) {
                    Job job = maybeJobs.stream().findFirst().get();
                    runLinkageExperiment(job);

                } else if (maybeJobs.size() > 1) {
                    runCompositeLinkageExperiment(maybeJobs);

                } else {
                    System.out.println("No suitable jobs in job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    Thread.sleep(60000);
                }
            } catch (PreEmptiveOutOfMemoryWarning e) {
                returnJobToJobList(maybeJobs, jobQ, assignedMemory);
            }
            MemoryLogger.reset();
        }
    }

    private static final List<String> SINGLE_PATH_LINKAGE_PHASES = list("1","2");
    private static final List<String> DUAL_PATH_A_LINKAGE_PHASES = list("A1","A2");
    private static final List<String> DUAL_PATH_B_LINKAGE_PHASES = list("B1","B2");
    private static final List<String> DUAL_PATH_LINKAGE_PHASES = list("A1","A2","B1","B2");


    private static void runCompositeLinkageExperiment(Set<Job> jobs) throws IOException, InterruptedException, BucketException, InvalidEvaluationApproachException, PersistentObjectException {

        Set<Result> results = jobs.stream().map(JobMappers::toResult).collect(Collectors.toSet());

        Result referenceJob = assertJobsShareKeyValuesElseThrow(results); // there's nothing special about this job, but given we've confirmed all the jobs share the same key values we'll just use the reference job for the common values
        Storr storr = new Storr(referenceJob.getRecordsRepo(), referenceJob.getLinksSubRepo(), referenceJob.getResultsRepo());
        assertValidEvaluationApproaches(referenceJob, storr);

        Map<Result, LinkageRecipe> jobToLinkageRecipes = results.stream()
                .collect(Collectors.toMap(Function.identity(), job -> getLinkageRecipe(job.getLinkageType(), storr)));

        Map<String, LinkageRecipe> phaseToLinkageRecipes = jobToLinkageRecipes.keySet().stream()
                .collect(Collectors.toMap(JobCore::getLinkagePhase, jobToLinkageRecipes::get));

        Map<String, Result> phaseToJob = results.stream()
                .collect(Collectors.toMap(JobCore::getLinkagePhase, Function.identity()));

        if (containsExactlyLinkagePhases(results, SINGLE_PATH_LINKAGE_PHASES)) {

            SinglePathIndirectLinkageRecipe compositeLinkageRecipe =
                    new SinglePathIndirectLinkageRecipe(phaseToLinkageRecipes.get("1"), phaseToLinkageRecipes.get("2"));

            Set<Result> linkageResults = runSinglePathIndirectLinkage(phaseToJob, SINGLE_PATH_LINKAGE_PHASES, compositeLinkageRecipe, referenceJob, storr);

            saveResultsToFile(referenceJob, linkageResults);
        } else if (containsExactlyLinkagePhases(results, DUAL_PATH_LINKAGE_PHASES)) {

            SinglePathIndirectLinkageRecipe compositeLinkageRecipeA =
                    new SinglePathIndirectLinkageRecipe(phaseToLinkageRecipes.get("A1"), phaseToLinkageRecipes.get("A2"));

            Set<Result> linkageResults = runSinglePathIndirectLinkage(phaseToJob, DUAL_PATH_A_LINKAGE_PHASES, compositeLinkageRecipeA, referenceJob, storr);

            SinglePathIndirectLinkageRecipe compositeLinkageRecipeB =
                    new SinglePathIndirectLinkageRecipe(phaseToLinkageRecipes.get("B1"), phaseToLinkageRecipes.get("B2"));

            linkageResults.addAll(runSinglePathIndirectLinkage(phaseToJob, DUAL_PATH_B_LINKAGE_PHASES, compositeLinkageRecipeB, referenceJob, storr));

            DualPathIndirectLinkageRecipe dualPathIndirectLinkageRecipe = new DualPathIndirectLinkageRecipe(compositeLinkageRecipeA, compositeLinkageRecipeB);

            int combinedLinkageConfigHashes = phaseToJob.values()
                    .stream()
                    .map(Result::getLinkageConfigurationHash)
                    .collect(Collectors.toList())
                    .hashCode();

            for (String evaluationApproachString : getDualPathEvaluationApproaches(referenceJob)) {
                linkageResults.add(evaluate(referenceJob, storr, dualPathIndirectLinkageRecipe, evaluationApproachString, "DUAL_PATH_INDIRECT", combinedLinkageConfigHashes));
            }

            saveResultsToFile(referenceJob, linkageResults);
        }
    }

    private static Set<Result> runSinglePathIndirectLinkage(Map<String, Result> phaseToJob, List<String> chosenLinkagePhases,
            SinglePathIndirectLinkageRecipe compositeLinkageRecipe, Result referenceJob, Storr storr) throws BucketException, InvalidEvaluationApproachException, PersistentObjectException {

        // this runs the two linkage phases in the Indirect Linkage Recipe
        Set<Result> linkageResults = runLinkagePhase(chosenLinkagePhases.get(0), phaseToJob, compositeLinkageRecipe);
        linkageResults.addAll(runLinkagePhase(chosenLinkagePhases.get(1), phaseToJob, compositeLinkageRecipe));

        int combinedLinkageConfigHashes = phaseToJob.values().stream().map(Result::getLinkageConfigurationHash).collect(Collectors.toList()).hashCode();

        // this evaluates the Indirect Linkage Recipe based on the evaluation approaches specified in the job file
        for (String evaluationApproachString : getSinglePathEvaluationApproaches(referenceJob)) {
            linkageResults.add(evaluate(referenceJob, storr, compositeLinkageRecipe, evaluationApproachString, "SINGLE_PATH_INDIRECT", combinedLinkageConfigHashes));
        }
        return linkageResults;
    }

    private static Set<String> getSinglePathEvaluationApproaches(Result referenceJob) {
        String approach = referenceJob.getSinglePathIndirectEvaluationApproach();
        if("".equals(approach)) {
            return new HashSet<>();
        }
        return new StringExpression(approach).getValues();
    }

    private static Set<String> getDualPathEvaluationApproaches(Result referenceJob) {
        String approach = referenceJob.getDualPathIndirectEvaluationApproach();
        if("".equals(approach)) {
            return new HashSet<>();
        }
        return new StringExpression(approach).getValues();
    }

    private static Result evaluate(Result referenceJob, Storr storr, IndirectLinkageRecipe compositeLinkageRecipe, String evaluationApproachString, String evaluationPhase, int overrideHash) throws InvalidEvaluationApproachException, BucketException, PersistentObjectException {
        Result result = referenceJob.clone();
        result.setStartTime(System.currentTimeMillis());
        EvaluationApproach evaluationApproach = convertToApproach(evaluationApproachString, storr);
        LinkageQuality linkageQuality = compositeLinkageRecipe.evaluateIndirectLinkage(evaluationApproach);
        setCoreResults(result, evaluationApproach.getLinkageRecipe());
        setLinkageQualityResults(result, evaluationApproach.getEvaluationDescription(), linkageQuality);
        result.setLinkagePhase(evaluationPhase);
        result.setLinkageConfigurationHash(overrideHash);
        return result;
    }

    private static void assertValidEvaluationApproaches(Result referenceJob, Storr storr) throws InvalidEvaluationApproachException {
        for(String evaluationApproach : getSinglePathEvaluationApproaches(referenceJob)) {
            convertToApproach(evaluationApproach, storr);
        }
    }

    private static EvaluationApproach convertToApproach(String indirectEvaluationApproachString, Storr storr) throws InvalidEvaluationApproachException {
        String[] split = indirectEvaluationApproachString.split("\\.");
        if(split.length != 2) {
            throw new InvalidEvaluationApproachException("Evaluation approach not in known form: " + indirectEvaluationApproachString);
        } else {
            try {
                LinkageRecipe linkageRecipe = getLinkageRecipe(split[0], storr);
                switch (Enum.valueOf(EvaluationApproach.Type.class, split[1])) {
                    case ALL:
                        return new StandardEvaluationApproach(linkageRecipe);
                }
            } catch (UnsupportedOperationException | IllegalArgumentException e) {
                throw new InvalidEvaluationApproachException(e);
            }
        }
        throw new InvalidEvaluationApproachException("Evaluation approach not in found for: " + indirectEvaluationApproachString);
    }

    private static Set<Result> runLinkagePhase(String linkagePhase, Map<String, Result> phaseToJob, SinglePathIndirectLinkageRecipe compositeLinkageRecipe) throws BucketException {
        Result phase = phaseToJob.get(linkagePhase);
        setLinkageConfig(phase);
        phase.setStartTime(System.currentTimeMillis());
        runRecipe(linkagePhase, compositeLinkageRecipe, getChosenMetric(phase), phase.getThreshold().doubleValue(),
                phase.getPreFilterRequiredFields(), true, phase.isEvaluateQuality(), phase.isPersistLinks());

        setCoreResults(phase, getRecipe(linkagePhase, compositeLinkageRecipe));
        return getResults(phase, getRecipeResults(linkagePhase, compositeLinkageRecipe));
    }

    private static Result assertJobsShareKeyValuesElseThrow(Set<Result> jobs) {
        if(jobs.isEmpty()) throw new RuntimeException("No jobs found");
        Result referenceJob = jobs.stream().findFirst().get();

        jobs.forEach(job -> {
            if(!(job.getRecordsRepo().equals(referenceJob.getRecordsRepo()) &&
                    job.getSinglePathIndirectEvaluationApproach().equals(referenceJob.getSinglePathIndirectEvaluationApproach()))) {
                throw new InvalidJobException(String.format("Composite Linkage jobs must share same: " +
                        "RecordsRepo(%s=?%s), " +
                        "IndirectEvaluationApproaches(%s=?%s)",
                        referenceJob.getRecordsRepo(), job.getRecordsRepo(),
                        referenceJob.getSinglePathIndirectEvaluationApproach(), job.getSinglePathIndirectEvaluationApproach()));
            }
        });
        return referenceJob;
    }

    private static boolean containsExactlyLinkagePhases(Set<? extends Job> job, Collection<String> phases) {
        return job.size() == phases.size() && job.stream().map(JobCore::getLinkagePhase).allMatch(phases::contains);
    }

    private static List<String> list(String... strings) {
        return Arrays.asList(strings);
    }


    private static void runLinkageExperiment(Job job) throws BucketException, java.io.IOException, InterruptedException {

        Result result = JobMappers.toResult(job);
        result.setStartTime(System.currentTimeMillis());

        setLinkageConfig(job);

        Storr storr = new Storr(result.getRecordsRepo(), result.getLinksSubRepo(), result.getResultsRepo());
        LinkageRecipe linkageRecipe = getLinkageRecipe(job.getLinkageType(), storr);

        Map<EvaluationApproach.Type, LinkageQuality> evaluationResults;

        evaluationResults = new BitBlasterLinkageRunner().run(
                linkageRecipe, getChosenMetric(job), job.getThreshold().doubleValue(), job.getPreFilterRequiredFields(),
                false, job.isEvaluateQuality(), job.isPersistLinks()).getLinkageEvaluations();

        storr.stopStoreWatcher();

        setCoreResults(result, linkageRecipe);
        saveResultsToFile(result, getResults(result, evaluationResults));
    }

    private static void setCoreResults(Result result, LinkageRecipe linkageRecipe) {
        result.calculateTimeTakeSeconds(System.currentTimeMillis());
        result.setFieldsUsed1(getLinkageFields(1, linkageRecipe));
        result.setFieldsUsed2(getLinkageFields(2, linkageRecipe));
        result.setLinkageClass(Utils.getLinkageClassName(linkageRecipe));
    }

    private static StringMetric getChosenMetric(Job job) {
        return Constants.get(job.getMetric(), 8370);
    }

    private static void saveResultsToFile(Job reference, Set<Result> jobResults) throws IOException, InterruptedException {
        EntitiesList<Result> results = new EntitiesList<>(Result.class, reference.getLinkageResultsFile(), EntitiesList.Lock.RESULTS);
        results.addAll(jobResults);
        results.writeEntriesToFile();
        results.releaseAndCloseFile(EntitiesList.Lock.RESULTS);
    }

    private static Set<Result> getResults(Result result, Map<EvaluationApproach.Type, LinkageQuality> evaluationResults) {
        Set<Result> jobResults = new HashSet<>();

        for(EvaluationApproach.Type type : evaluationResults.keySet()) {
            LinkageQuality linkageQuality = evaluationResults.get(type);
            Result temp = result.clone();
            setLinkageQualityResults(temp, type, linkageQuality);
            jobResults.add(temp);
        }
        return jobResults;
    }

    private static void setLinkageQualityResults(Result result, EvaluationApproach.Type type, LinkageQuality linkageQuality) {
        result.setEvaluationApproach(type);
        result.setTp(linkageQuality.getTp());
        result.setFp(linkageQuality.getFp());
        result.setFn(linkageQuality.getFn());
        result.setPrecision(linkageQuality.getPrecision());
        result.setRecall(linkageQuality.getRecall());
        result.setfMeasure(linkageQuality.getF_measure());
        result.setLinksLostOnPreFilter(linkageQuality.getLinksLostOnPrefilter());
    }

    private static void returnJobToJobList(Set<Job> jobs, String jobQ, int assignedMemory) throws IOException, InterruptedException {
        int updatedMemoryRequirement = (int) Math.ceil(assignedMemory * 1.1);
        JobList jobList = new JobList(jobQ);

        jobList.addAll(jobs.stream()
                .map(JobMappers::map)
                .map(job -> updateMemory(job, updatedMemoryRequirement))
                .collect(Collectors.toSet()));

        jobList.writeEntriesToFile();
        jobList.releaseAndCloseFile(EntitiesList.Lock.JOBS);
    }

    private static JobWithExpressions updateMemory(JobWithExpressions job, int memory) {
        job.setRequiredMemory(memory);
        return job;
    }

    private static void validatePopulationRecordsAreInStorrAndIfNotImport(Path recordCountsFile, Set<Job> jobs) throws Exception {
        // validate the data is in the storr (local scratch space on clusters - but either way it's defined in application.properties)
        for(Job job : jobs) {
            new ValidatePopulationInStorr(job.getPopulation(), job.getSize(), String.valueOf(job.getPopNumber()), job.getCorruptionProfile())
                    .validate(recordCountsFile);
        }
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


    private static LinkageRecipe getLinkageRecipe(final String linkageType, final Storr storr) {

        String basicLinkageType = linkageType;
        boolean reversed = false;

        if(Objects.equals(linkageType.split("-")[0], "reversed")) {
            basicLinkageType = linkageType.split("-", 2)[1];
            reversed = true;
        }

        LinkageRecipe chosenLinkageRecipe;

        switch (basicLinkageType) {
            case BirthSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthSiblingLinkageRecipe(storr);
                break;
            case BirthDeathIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthDeathIdentityLinkageRecipe(storr);
                break;
            case BirthDeathSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthDeathSiblingLinkageRecipe(storr);
                break;
            case BirthFatherIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthFatherIdentityLinkageRecipe(storr);
                break;
            case BirthMotherIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthMotherIdentityLinkageRecipe(storr);
                break;
            case BirthBrideIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthBrideIdentityLinkageRecipe(storr);
                break;
            case BrideBrideSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BrideBrideSiblingLinkageRecipe(storr);
                break;
            case BrideGroomSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BrideGroomSiblingLinkageRecipe(storr);
                break;
            case DeathBrideOwnMarriageIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new DeathBrideOwnMarriageIdentityLinkageRecipe(storr);
                break;
            case DeathSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new DeathSiblingLinkageRecipe(storr);
                break;
            case DeathGroomOwnMarriageIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new DeathGroomOwnMarriageIdentityLinkageRecipe(storr);
                break;
            case FatherGroomIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new FatherGroomIdentityLinkageRecipe(storr);
                break;
            case BirthGroomIdentityLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new BirthGroomIdentityLinkageRecipe(storr);
                break;
            case GroomGroomSiblingLinkageRecipe.LINKAGE_TYPE:
                chosenLinkageRecipe = new GroomGroomSiblingLinkageRecipe(storr);
                break;
            default:
                throw new UnsupportedOperationException("LinkageType not found");
        }

        if(reversed) {
            return new ReversedLinkageRecipe(chosenLinkageRecipe);
        } else {
            return chosenLinkageRecipe;
        }
    }

    private static String getLinkageFields(int n, LinkageRecipe linkageRecipe) {

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
