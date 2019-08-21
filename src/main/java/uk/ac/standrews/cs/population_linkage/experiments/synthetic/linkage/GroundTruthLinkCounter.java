package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.utilities.FileManipulation;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GroundTruthLinkCounter extends SyntheticBirthBirthSiblingLinkageRunner {

    private String sourceRepoName;
    private String populationName;
    private String populationSize;
    private String populationNumber;
    private boolean corrupted;
    private String corruptionNumber;
    private Path resultsFile;

    public GroundTruthLinkCounter(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber, Path resultsFile) {
        super(populationName, populationSize, populationNumber, corrupted, corruptionNumber, resultsFile, 10000, 70);

        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corrupted = corrupted;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

    }

    public int count() {
        System.out.println("Count ground truth links in population: " + sourceRepoName);

        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                        .appendToFile("population,size,pop#,corruption#,#gtLinks,count-time-seconds" +
                                System.lineSeparator());
            }

            // check if count in file
            int numberOfGTLinks = getCountFromLog(resultsFile, populationName, populationSize, populationNumber, corruptionNumber);

            if(numberOfGTLinks == -1) { // if count not already done then do count
                long startTime = System.currentTimeMillis();
                numberOfGTLinks = countNumberOfGroundTruthLinks(sourceRepoName);
                long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

                new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                        .appendToFile(populationName + "," + populationSize + "," + populationNumber + "," +
                                corruptionNumber + "," + numberOfGTLinks + "," +
                                timeTakenInSeconds + System.lineSeparator());

            }

            return numberOfGTLinks;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
//        new GroundTruthLinkCounter(args[0], args[1], args[2], args[3].equals("true"), args[4], Paths.get(args[5])).count();
        countAll(Paths.get(args[0]));
    }

    private int getCountFromLog(Path recordCounts, String populationName, String populationSize, String populationNumber, String corruptionNumber) throws IOException {

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
                    corruptionNumber.equals(row.get(columnLabels.indexOf("corruption#"))))
            {
                return Integer.parseInt(row.get(columnLabels.indexOf("#gtLinks")));
            }

        }

        return -1;

    }


    public static void countAll(Path resultsFile) {

        String[] populationNames   = {"synthetic-scotland"};
        String[] populationSizes   = {"13k","133k","530k"};
        String[] populationNumbers = {"1","2","3","4","5"};
        String[] corruptionNumbers = {"0","1","2"};

        for(String populationName : populationNames)
            for (String populationSize : populationSizes)
                for(String populationNumber : populationNumbers)
                    for(String corruptionNumber : corruptionNumbers)
                        new GroundTruthLinkCounter(populationName, populationSize,
                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber, resultsFile).count();


    }
}
