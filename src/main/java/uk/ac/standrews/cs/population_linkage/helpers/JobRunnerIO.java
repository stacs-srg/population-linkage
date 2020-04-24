/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.Path;

public class JobRunnerIO {

    public static void setupResultsFile(Path resultsFile) throws IOException {
        FileManipulation.createFileIfDoesNotExist(resultsFile);
        if(FileManipulation.countLines(resultsFile) == 0) {
            new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                    .appendToFile(
                            "population,size,pop_number,corruption_number,linkage-type,metric,threshold," +
                            "preFilter,max-sibling-gap,tp,fp,fn,precision,recall,f-measure,link-time-seconds," +
                            "max-memory-usage,ROs,births-cache-size,marriages-cache-size,deaths-cache-size," +
                            "code-version,hostname,linkage-fields-1,linkage-fields-2" +
                            System.lineSeparator());
        }
    }

    public static void appendToResultsFile(double threshold, String stringMetric, Integer maxSiblingGap, LinkageQuality lq,
                                           long timeTakenInSeconds, Path resultsFile, String populationName,
                                           String populationSize, String populationNumber, String corruptionNumber,
                                           String linkageApproach, int numberOfReferenceObjects,
                                           String fields1, String fields2, boolean preFilter, int birthsCacheSize,
                                           int marriagesCacheSize, int deathsCacheSize) throws IOException {

        new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                .appendToFile(populationName + "," + populationSize + "," + populationNumber + "," +
                        corruptionNumber + "," + linkageApproach + "," + stringMetric + "," + threshold + "," +
                        preFilter + "," + maxSiblingGap + "," + lq.toCSV() + "," + timeTakenInSeconds + "," +
                        MemoryLogger.getMax()/1000000 + "," + numberOfReferenceObjects + "," + birthsCacheSize + "," +
                        marriagesCacheSize + "," + deathsCacheSize + "," + getGitVersion() + ","  +
                        getHostname() + "," + fields1  + "," + fields2 + System.lineSeparator());
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

}
