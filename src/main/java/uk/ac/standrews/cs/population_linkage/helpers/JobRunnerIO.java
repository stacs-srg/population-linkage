/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.String.valueOf;

public class JobRunnerIO {

    public static void setupResultsFile(Path resultsFile) throws IOException {
        FileManipulation.createFileIfDoesNotExist(resultsFile);
        if (FileManipulation.countLines(resultsFile) == 0) {
            new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                    .appendToFile(
                            "reason,priority,start-time,population,size,pop-number,corruption-profile,corrupted,threshold,metric," +
                                    "linkage-type,results-repo,links-persistent-name,pre-filter," +
                                    "pre-filter-required-fields,persist-links,evaluate-quality,births-cache-size," +
                                    "marriages-cache-size,deaths-cache-size,ros,max-sibling-age-diff,min-marriage-age," +
                                    "min-parenting-age,max-parenting-age,max-marriage-age-discrepancy,max-death-age," +
                                    "source-repo,tp,fp,fn,precision,recall,f-measure,time-take-seconds,linkage-class," +
                                    "max-memory-usage,required-memory,code-version,hostname,seed,fields-used-1,fields-used-2" +
                                    System.lineSeparator());
        }
    }

    public static String getGitVersion() {
        try {
            return execCmd("git rev-parse HEAD").trim();
        } catch (IOException e) {
            return "NA";
        }
    }

    public static String getHostname() {
        try {
            return execCmd("hostname").trim();
        } catch (IOException e) {
            return "NA";
        }
    }

    public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void appendToResultsFile(Path resultsFile, String reason, String populationName, String populationSize, String populationNumber,
            String corruptionNumber, boolean corrupted, Double threshold, String metric, String linkageType,
            String resultsRepo, String links_persistent_name, Boolean preFilter, Integer preFilterRequiredFields,
            Boolean persist_links, Boolean evaluate_quality, Integer birthsCacheSize, Integer marriagesCacheSize,
            Integer deathsCacheSize, Integer numROs, Integer maxSiblingAgeDiff, Integer minAgeAtMarriage,
            Integer minParentAgeAtBirth, Integer maxParentAgeAtBirth, Integer maxAllowableMarriageAgeDiscrepancy,
            Integer maxAgeAtDeath, String sourceRepo, LinkageQuality linkageQuality, long timeTakenInSeconds,
            String canonicalName, String fieldsUsed1, String fieldsUsed2, Integer priority, Integer requiredMemory,
            Long seed) throws IOException {

        new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                .appendToFile(
                        String.join(",", reason, valueOf(priority),
                                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                populationName, populationSize, populationNumber, corruptionNumber,
                                valueOf(corrupted), valueOf(threshold), metric, linkageType, resultsRepo, links_persistent_name,
                                valueOf(preFilter), valueOf(preFilterRequiredFields), valueOf(persist_links), valueOf(evaluate_quality),
                                valueOf(birthsCacheSize), valueOf(marriagesCacheSize), valueOf(deathsCacheSize), valueOf(numROs),
                                valueOf(maxSiblingAgeDiff), valueOf(minAgeAtMarriage), valueOf(minParentAgeAtBirth),
                                valueOf(maxParentAgeAtBirth), valueOf(maxAllowableMarriageAgeDiscrepancy), valueOf(maxAgeAtDeath),
                                sourceRepo, linkageQuality.toCSV(), valueOf(timeTakenInSeconds), canonicalName,
                                valueOf(MemoryLogger.getMax()/1000000), valueOf(requiredMemory), getGitVersion(), getHostname(), valueOf(seed),
                                fieldsUsed1, fieldsUsed2) + System.lineSeparator());

    }
}
