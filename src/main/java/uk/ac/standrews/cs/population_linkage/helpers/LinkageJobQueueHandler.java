package uk.ac.standrews.cs.population_linkage.helpers;

import com.google.common.collect.Sets;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthBirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthFatherIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthMotherIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthParentsMarriageLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBirthIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathBrideOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathDeathSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.DeathGroomOwnMarriageIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.FatherGroomIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomBirthIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomBrideSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.GroomGroomSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.*;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

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
    // population,size,pop_number,corruption_number,linkage-type,metric,threshold,preFilter,max-sibling-gap,evaluate_quality,ROs,births-cache-size,marriages-cache-size,deaths-cache-size,persist_links,results-repo,links_persistent_name,gt_persistent_name
    //
    // Empty fields should contain a - (i.e. a single dash)
    // The job queue can be used for both synthetic populations and the umea data
    // in the case of umea specify the population as 'umea' and put a dash in each of size,pop_number,corruption_number
    //
    // Linkage type defines the 'type' of linkage to be performed - the provided string should be the same as the
    // linkageType field in the relevant linkage runner class

    public static void main(String[] args) throws Exception {
        Path jobQ = Paths.get(args[0]);
        Path linkageResultsFile = Paths.get(args[1]);
        Path recordCountsFile = Paths.get(args[2]);
        Path statusFile = Paths.get(args[3]);

        while(getStatus(statusFile)) {

            FileChannel fileChannel = getFileChannel(jobQ);
            System.out.println("Locking job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileChannel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Locked job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            List<List<String>> jobs = readInJobFile(fileChannel);

            if (jobs.size() > 1) {
                // jobs in queue
                List<String> columnLabels = jobs.get(0);
                List<String> job = jobs.remove(1);

                System.out.println("Job taken: " + job);

                overwriteToJobFile(fileChannel, jobs);
                fileChannel.close();
                System.out.println("Released job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // At this point we have a linkage job no body else has - now we need to:
                // put job info into variables

                String populationName = job.get(columnLabels.indexOf("population")).trim();
                String populationSize = job.get(columnLabels.indexOf("size")).trim();
                String populationNumber = job.get(columnLabels.indexOf("pop_number")).trim();
                String corruptionNumber = job.get(columnLabels.indexOf("corruption_number")).trim();
                boolean corrupted = !corruptionNumber.equals("0");
                double threshold = Double.parseDouble(job.get(columnLabels.indexOf("threshold")).trim());
                String metric = job.get(columnLabels.indexOf("metric")).trim();
                String maxSiblingGapString = job.get(columnLabels.indexOf("max-sibling-gap")).trim();
                Integer maxSiblingGap = (maxSiblingGapString.equals("")) ? null : Integer.valueOf(maxSiblingGapString);
                int birthsCacheSize = Integer.parseInt(job.get(columnLabels.indexOf("births-cache-size")).trim());
                int marriagesCacheSize = Integer.parseInt(job.get(columnLabels.indexOf("marriages-cache-size")).trim());
                int deathsCacheSize = Integer.parseInt(job.get(columnLabels.indexOf("deaths-cache-size")).trim());
                int numROs = Integer.parseInt(job.get(columnLabels.indexOf("ROs")).trim());
                String linkageType = job.get(columnLabels.indexOf("linkage-type")).trim();

                String resultsRepo = job.get(columnLabels.indexOf("results-repo")).trim();
                String links_persistent_name = job.get(columnLabels.indexOf("links_persistent_name")).trim();

                boolean preFilter = job.get(columnLabels.indexOf("preFilter")).trim().toLowerCase().equals("true");
                int preFilterRequiredFields = Integer.parseInt(job.get(columnLabels.indexOf("preFilterRequiredFields")).trim());
                boolean persist_links = job.get(columnLabels.indexOf("persist_links")).trim().toLowerCase().equals("true");
                boolean evaluate_quality = job.get(columnLabels.indexOf("evaluate_quality")).trim().toLowerCase().equals("true");

                String sourceRepo = toRepoName(populationName, populationSize, populationNumber, corruptionNumber, corrupted);

                LinkageConfig.birthCacheSize = birthsCacheSize;
                LinkageConfig.marriageCacheSize = marriagesCacheSize;
                LinkageConfig.deathCacheSize = deathsCacheSize;
                LinkageConfig.numberOfROs = numROs;
                LinkageConfig.MAX_SIBLING_AGE_DIFF = maxSiblingGap;

                // validate the data is in the storr (local scratch space on clusters - but anyway, it's defined in application.properties)
                new ValidatePopulationInStorr(populationName, populationSize, populationNumber, corrupted, corruptionNumber).validate(recordCountsFile);

                StringMetric chosenMetric = Constants.get(metric, 4096);

                JobRunnerIO.setupResultsFile(linkageResultsFile);

                long startTime = System.currentTimeMillis();

                LinkageRecipe linkageRecipe = getLinkageRecipe(linkageType, resultsRepo, links_persistent_name, sourceRepo);
                String linkageApproach = linkageRecipe.getLinkageType();

                LinkageQuality linkageQuality = new BitBlasterLinkageRunner().run(
                        linkageRecipe, chosenMetric, threshold, preFilter, preFilterRequiredFields,
                        false, false, evaluate_quality, persist_links).getLinkageQuality();

                String fieldsUsed1 = getLinkageFields(1, linkageRecipe, sourceRepo);
                String fieldsUsed2 = getLinkageFields(2, linkageRecipe, sourceRepo);

                long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

                JobRunnerIO.appendToResultsFile(threshold, metric, LinkageConfig.MAX_SIBLING_AGE_DIFF, linkageQuality, timeTakenInSeconds,
                        linkageResultsFile, populationName, populationSize, populationNumber, corruptionNumber,
                        linkageApproach, numROs, fieldsUsed1, fieldsUsed2, preFilter,
                        birthsCacheSize, marriagesCacheSize, deathsCacheSize);

            } else {
                fileChannel.close();
                System.out.println("No jobs in job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                Thread.sleep(60000);
            }
        }
    }

    private static LinkageRecipe getLinkageRecipe(final String linkageType, final String resultsRepo, final String links_persistent_name, final String sourceRepo) {

        // TODO Replace with reflective call.

        switch (linkageType) {
            case BirthBirthSiblingLinkageRecipe.linkageType:
                return new BirthBirthSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthDeathIdentityLinkageRecipe.linkageType:
                return new BirthDeathIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthDeathSiblingLinkageRecipe.linkageType:
                return new BirthDeathSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthFatherIdentityLinkageRecipe.linkageType:
                return new BirthFatherIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthMotherIdentityLinkageRecipe.linkageType:
                return new BirthMotherIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BirthParentsMarriageLinkageRecipe.linkageType:
                return new BirthParentsMarriageLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BrideBirthIdentityLinkageRecipe.linkageType:
                return new BrideBirthIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BrideBrideSiblingLinkageRecipe.linkageType:
                return new BrideBrideSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case BrideGroomSiblingLinkageRecipe.linkageType:
                return new BrideGroomSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathBrideOwnMarriageIdentityLinkageRecipe.linkageType:
                return new DeathBrideOwnMarriageIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathDeathSiblingLinkageRecipe.linkageType:
                return new DeathDeathSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case DeathGroomOwnMarriageIdentityLinkageRecipe.linkageType:
                return new DeathGroomOwnMarriageIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case FatherGroomIdentityLinkageRecipe.linkageType:
                return new FatherGroomIdentityLinkageRecipe(links_persistent_name, sourceRepo, resultsRepo);
            case GroomBirthIdentityLinkageRecipe.linkageType:
                return new GroomBirthIdentityLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case GroomBrideSiblingLinkageRecipe.linkageType:
                return new GroomBrideSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            case GroomGroomSiblingLinkageRecipe.linkageType:
                return new GroomGroomSiblingLinkageRecipe(sourceRepo, resultsRepo, links_persistent_name);
            default:
                throw new RuntimeException("LinkageType not found");
        }
    }

    private static String getLinkageFields(int n, LinkageRecipe linkageRecipe, String sourceRepo) { //String links_persistent_name, String gt_persistent_name, String sourceRepo, String resultsRepo) {

        Class record;
        List<Integer> fields;

        if(n == 1) {
            record = linkageRecipe.getStoredType();
            fields = linkageRecipe.getLinkageFields();
        } else {
            record = linkageRecipe.getSearchType();
            fields = linkageRecipe.getSearchMappingFields();
        }

        List<String> recordLabels = getRecordLabels(record);

        return Constants.stringRepresentationOf(fields, record, recordLabels);
    }

    private static List<String> getRecordLabels(Class record) {

        if(record.equals(Birth.class)) {
            return Birth.getLabels();
        }

        if(record.equals(Marriage.class)) {
            return Marriage.getLabels();
        }

        if(record.equals(Death.class)) {
            return Death.getLabels();
        }

        throw new RuntimeException("Record type not resolved:" + record);
    }

    private static FileChannel getFileChannel(Path jobFile) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<>(Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE));
        return FileChannel.open(jobFile, options);
    }

    private static String toRepoName(String populationName, String populationSize, String populationNumber, String corruptionNumber, boolean corrupted) {

        if(populationSize.equals("-") && populationNumber.equals("-") && corruptionNumber.equals("-"))
            return populationName;

        String sourceRepoName;
        if(corrupted)
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
                if(c == '\n') {

                    data.add(Arrays.asList(line.split(",")));

                    line = "";
                } else {
                    line += c;
                }

            }

            if(!line.equals("")) data.add(Arrays.asList(line.split(",")));

            buffer.clear();
            Thread.sleep(1000);
            noOfBytesRead = jobFile.read(buffer);
        }


        return data;
    }


    public static void overwriteToJobFile(FileChannel jobFile, List<List<String>> jobs) throws IOException {
        jobFile.truncate(0);


        StringBuilder sb = new StringBuilder();

        for(List<String> row : jobs) {
            boolean first = true;
            for (String value : row) {
                if(first) {
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

        while(buf.hasRemaining()) {
            jobFile.write(buf);
        }
    }

    private static boolean getStatus(Path statusPath) throws IOException {
        // read in file
        ArrayList<String> lines = new ArrayList<>(getAllLines(statusPath));

        if(!lines.isEmpty()) {
            switch (lines.get(0)) {
                case "run": return true;
                case "terminate" : return false;
            }
        }

        return true;
    }

    private static final String COMMENT_INDICATOR = "#";

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
            reader.close();
        }

        return lines;
    }

}
