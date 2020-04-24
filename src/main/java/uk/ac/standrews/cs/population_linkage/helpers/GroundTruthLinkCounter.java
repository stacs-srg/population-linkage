/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.LinkageRunner;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GroundTruthLinkCounter {

    private String sourceRepoName;
    private String populationName;
    private String populationSize = "";
    private String populationNumber = "";
    private String corruptionNumber = "";
    private Path gtCountsFile;

    public GroundTruthLinkCounter(String populationName, String populationSize, String populationNumber,
                                  boolean corrupted, String corruptionNumber, Path gtCountsFile) {

        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.gtCountsFile = gtCountsFile;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

    }

    public GroundTruthLinkCounter(String sourceRepoName, Path gtCountsFile) {
        this.sourceRepoName = sourceRepoName;
        this.populationName = sourceRepoName;
        this.gtCountsFile = gtCountsFile;
    }

    public int count(LinkageRecipe linkageRecipe) { //, String linkageApproach) {
        System.out.println("Count ground truth links in population: " + sourceRepoName);

        String linkageApproach = linkageRecipe.getClass().getName();

        try {
            FileManipulation.createFileIfDoesNotExist(gtCountsFile);
            if(FileManipulation.countLines(gtCountsFile) == 0) {
                new FileChannelHandle(gtCountsFile, FileChannelHandle.optionsWA)
                        .appendToFile("population,size,pop#,corruption#,linkage-approach,#gtLinks,count-time-seconds" +
                                System.lineSeparator());
            }

            // check if count in file
            int numberOfGTLinks = getCountFromLog(gtCountsFile, populationName, populationSize, populationNumber,
                    corruptionNumber, linkageApproach);

            if(numberOfGTLinks == -1) { // if count not already done then do count
                System.out.println("Ground truth links count not in file will count from repo: " + sourceRepoName);
                long startTime = System.currentTimeMillis();
                numberOfGTLinks = linkageRecipe.getNumberOfGroundTruthTrueLinks();
                long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

                new FileChannelHandle(gtCountsFile, FileChannelHandle.optionsWA)
                        .appendToFile(populationName + "," + populationSize + "," + populationNumber + "," +
                                corruptionNumber + "," + linkageApproach + "," + numberOfGTLinks + "," +
                                timeTakenInSeconds + System.lineSeparator());

            }

            return numberOfGTLinks;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getCountFromLog(Path recordCounts, String populationName, String populationSize,
                                String populationNumber, String corruptionNumber, String linkageApproach) throws IOException {

        List<String> counts = FileManipulation.readAllLines(FileManipulation.getInputStream(recordCounts));

        if(counts.size() == 0) {
            return -1;
        }

        List<String> columnLabels = Arrays.asList(counts.get(0).split(","));

        for(int i = 1; i < counts.size(); i++) {
            List<String> row = Arrays.asList(counts.get(i).split(","));

            if(populationName.equals(row.get(columnLabels.indexOf("population"))) &&
                    populationSize.equals(row.get(columnLabels.indexOf("size"))) &&
                    populationNumber.equals(row.get(columnLabels.indexOf("pop#"))) &&
                    corruptionNumber.equals(row.get(columnLabels.indexOf("corruption#"))) &&
                    linkageApproach.equals(row.get(columnLabels.indexOf("linkage-approach"))))
            {
                return Integer.parseInt(row.get(columnLabels.indexOf("#gtLinks")));
            }
        }

        return -1;
    }


//    public static void countAll(Path gtCountsFile) {
//
//        String[] populationNames   = {"synthetic-scotland"};
//        String[] populationSizes   = {"13k","133k","530k"};
//        String[] populationNumbers = {"1","2","3","4","5"};
//        String[] corruptionNumbers = {"0","1","2"};
//
//        for(String populationName : populationNames)
//            for (String populationSize : populationSizes)
//                for(String populationNumber : populationNumbers)
//                    for(String corruptionNumber : corruptionNumbers)
//                        new GroundTruthLinkCounter(populationName, populationSize,
//                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber, gtCountsFile).count();
//
//
//    }

}
