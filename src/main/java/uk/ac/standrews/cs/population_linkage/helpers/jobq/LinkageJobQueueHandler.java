/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq;

import com.google.common.collect.Sets;
import uk.ac.standrews.cs.population_linkage.helpers.JobRunnerIO;
import uk.ac.standrews.cs.population_linkage.helpers.ValidatePopulationInStorr;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.Job;
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
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LinkageJobQueueHandler {

    // to use the linkage job queue handler define a csv job file with the following headings
    // linkage-results-file,reason,priority,required-memory,seed,population,size,pop-number,corruption-profile,threshold,metric,linkage-type,results-repo,links-persistent-name,pre-filter,pre-filter-required-fields,persist-links,evaluate-quality,births-cache-size,marriages-cache-size,deaths-cache-size,ros,max-sibling-age-diff,min-marriage-age,min-parenting-age,max-parenting-age,max-marriage-age-discrepancy,max-death-age
    //
    // Empty fields should contain a - (i.e. a single dash)
    // The job queue can be used for both synthetic populations and the umea data
    // in the case of umea specify the population as 'umea' and put a dash in each of size,pop_number,corruption_number
    //
    // Linkage type defines the 'type' of linkage to be performed - the provided string should be the same as the
    // linkageType field in the relevant linkage runner class

    private static final String COMMENT_INDICATOR = "#";

    public static void main(String[] args) throws Exception {
        Path jobQ = Paths.get(args[0]);
        int assignedMemory = Integer.parseInt(args[1]);
        Path recordCountsFile = Paths.get(args[2]);
        Path statusFile = Paths.get(args[3]);

        while(getStatus(statusFile)) {

            FileChannel fileChannel = getFileChannel(jobQ);
            System.out.println("Locking job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileChannel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Locked job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            List<List<String>> jobs = readInJobFile(fileChannel);

            Job job = pickJob(jobs, assignedMemory);

            if (job != null) {

                System.out.println("Job taken: " + job);

                overwriteToJobFile(fileChannel, jobs);
                fileChannel.close();
                System.out.println("Released job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // At this point we have a linkage job no body else has - now we need to:
                // put job info into variables

                var linkageResultsFile = Paths.get(job.get("linkage-results-file"));
                var reason = job.get("reason");
                var priority = job.get("priority", Integer.class);
                var requiredMemory = job.get("required-memory", Integer.class);

                var populationName = job.get("population");
                var populationSize = job.get("size");
                var populationNumber = job.get("pop-number");
                var corruptionProfile = job.get("corruption-profile");
                var corrupted = !corruptionProfile.equals("0");

                var threshold = job.get("threshold", Double.class);
                var metric = job.get("metric");
                var linkageType = job.get("linkage-type");

                var resultsRepo = job.get("results-repo");
                var links_persistent_name = job.get("links-persistent-name");

                var preFilter = job.get("pre-filter", Boolean.class);
                var preFilterRequiredFields = job.get("pre-filter-required-fields", Integer.class);
                var persist_links = job.get("persist-links", Boolean.class);
                var evaluate_quality = job.get("evaluate-quality", Boolean.class);

                var birthsCacheSize = job.get("births-cache-size", Integer.class);
                var marriagesCacheSize = job.get("marriages-cache-size", Integer.class);
                var deathsCacheSize = job.get("deaths-cache-size", Integer.class);
                var numROs = job.get("ros", Integer.class);

                var seed = job.getNullable("seed", "-", Long.class);
                seed = seed == null ? LinkageConfig.seed : seed;

                var maxSiblingAgeDiff = job.getNullable("max-sibling-age-diff", "-", Integer.class);
                var minAgeAtMarriage = job.get("min-marriage-age", Integer.class);
                var minParentAgeAtBirth = job.get("min-parenting-age", Integer.class);
                var maxParentAgeAtBirth = job.get("max-parenting-age", Integer.class);
                var maxAllowableMarriageAgeDiscrepancy = job.get("max-marriage-age-discrepancy", Integer.class);
                var maxAgeAtDeath = job.get("max-death-age", Integer.class);

                var sourceRepo = toRepoName(populationName, populationSize, populationNumber, corruptionProfile, corrupted);

                LinkageConfig.birthCacheSize = birthsCacheSize;
                LinkageConfig.marriageCacheSize = marriagesCacheSize;
                LinkageConfig.deathCacheSize = deathsCacheSize;
                LinkageConfig.numberOfROs = numROs;
                LinkageConfig.seed = seed;

                LinkageConfig.MAX_SIBLING_AGE_DIFF = maxSiblingAgeDiff;
                LinkageConfig.MIN_AGE_AT_MARRIAGE = minAgeAtMarriage;
                LinkageConfig.MIN_PARENT_AGE_AT_BIRTH = minParentAgeAtBirth;
                LinkageConfig.MAX_PARENT_AGE_AT_BIRTH = maxParentAgeAtBirth;
                LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY = maxAllowableMarriageAgeDiscrepancy;
                LinkageConfig.MAX_AGE_AT_DEATH = maxAgeAtDeath;

                // validate the data is in the storr (local scratch space on clusters - but either way it's defined in application.properties)
                new ValidatePopulationInStorr(populationName, populationSize, populationNumber, corrupted, corruptionProfile)
                        .validate(recordCountsFile);

                LinkageQuality lq = new LinkageQuality("No Experiment Run");
                StringMetric chosenMetric = Constants.get(metric, 4096);

                JobRunnerIO.setupResultsFile(linkageResultsFile);

                long startTime = System.currentTimeMillis();

                LinkageRecipe linkageRecipe = getLinkageRecipe(linkageType, resultsRepo, links_persistent_name, sourceRepo);

                LinkageQuality linkageQuality = new BitBlasterLinkageRunner().run(
                        linkageRecipe, chosenMetric, threshold, preFilter, preFilterRequiredFields,
                        false, false, evaluate_quality, persist_links).getLinkageQuality();

                String fieldsUsed1 = getLinkageFields(1, linkageRecipe, sourceRepo);
                String fieldsUsed2 = getLinkageFields(2, linkageRecipe, sourceRepo);

                long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

                JobRunnerIO.appendToResultsFile(linkageResultsFile, reason, populationName, populationSize, populationNumber,
                        corruptionProfile, corrupted, threshold, metric, linkageType, resultsRepo, links_persistent_name,
                        preFilter, preFilterRequiredFields, persist_links, evaluate_quality, birthsCacheSize,
                        marriagesCacheSize, deathsCacheSize, numROs, maxSiblingAgeDiff, minAgeAtMarriage,
                        minParentAgeAtBirth, maxParentAgeAtBirth, maxAllowableMarriageAgeDiscrepancy, maxAgeAtDeath,
                        sourceRepo, linkageQuality, timeTakenInSeconds, linkageRecipe.getClass().getCanonicalName(),
                        fieldsUsed1, fieldsUsed2, priority, requiredMemory, seed);

                // runs GC to ensure no object left in memory from previous linkages that may skew memory usage logging
                System.gc();

            } else {
                fileChannel.close();
                System.out.println("No suitable jobs in job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                Thread.sleep(60000);
            }
        }
    }

    private static Job pickJob(List<List<String>> jobs, int assignedMemory) {

        List<String> labels = jobs.get(0);

        int highestPriority = Integer.MAX_VALUE;
        Integer rowIndex = null;

        for(int row = 1; row < jobs.size(); row++) {
            var job = new Job(labels, jobs.get(row));
            if(assignedMemory <= job.get("required-memory", Integer.class)) {
                var jobPriority = job.get("priority", Integer.class);
                if(jobPriority < highestPriority) {
                    highestPriority = jobPriority;
                    rowIndex = row;
                }
            }
        }
        if(rowIndex != null) {
            return new Job(labels, jobs.remove(rowIndex.intValue()));
        } else {
            return null;
        }
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

    private static String getLinkageFields(int n, LinkageRecipe linkageRecipe, String sourceRepo) { //String links_persistent_name, String gt_persistent_name, String sourceRepo, String resultsRepo) {

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

    private static FileChannel getFileChannel(Path jobFile) throws IOException {
        Set<StandardOpenOption> options = new HashSet<>(Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE));
        return FileChannel.open(jobFile, options);
    }

    private static String toRepoName(String populationName, String populationSize, String populationNumber, String corruptionNumber, boolean corrupted) {

        if (populationSize.equals("-") && populationNumber.equals("-") && corruptionNumber.equals("-"))
            return populationName;

        String sourceRepoName;
        if (corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
        }
        return sourceRepoName;
    }

    private static List<List<String>> readInJobFile(FileChannel jobFile) throws IOException, InterruptedException {

        ByteBuffer buffer = ByteBuffer.allocate((int) jobFile.size());
        int noOfBytesRead = jobFile.read(buffer);

        List<List<String>> data = new ArrayList<>();

        while (noOfBytesRead != -1) {

            buffer.flip();

            String line = "";

            while (buffer.hasRemaining()) {

                char c = (char) buffer.get();
                if (c == '\n') {

                    data.add(Arrays.asList(line.split(",")));

                    line = "";
                } else {
                    line += c;
                }

            }

            if (!line.equals("")) data.add(Arrays.asList(line.split(",")));

            buffer.clear();
            Thread.sleep(1000);
            noOfBytesRead = jobFile.read(buffer);
        }


        return data;
    }

    public static void overwriteToJobFile(FileChannel jobFile, List<List<String>> jobs) throws IOException {
        jobFile.truncate(0);


        StringBuilder sb = new StringBuilder();

        for (List<String> row : jobs) {
            boolean first = true;
            for (String value : row) {
                if (first) {
                    sb.append(value);
                    first = false;
                } else {
                    sb.append(",").append(value);
                }
            }
            sb.append(System.lineSeparator());
        }

        String toFileString = sb.toString();


        ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
        buf.clear();
        buf.put(toFileString.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            jobFile.write(buf);
        }
    }

    private static boolean getStatus(Path statusPath) throws IOException {
        // read in file
        ArrayList<String> lines = new ArrayList<>(getAllLines(statusPath));

        if (!lines.isEmpty()) {
            switch (lines.get(0)) {
                case "run":
                    return true;
                case "terminate":
                    return false;
            }
        }
        return true;
    }

    public static List<String> getAllLines(Path path) throws IOException {

        List<String> lines = new ArrayList<>();

        // Reads in all lines to a collection of Strings
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.startsWith(COMMENT_INDICATOR) && line.length() != 0) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

}
