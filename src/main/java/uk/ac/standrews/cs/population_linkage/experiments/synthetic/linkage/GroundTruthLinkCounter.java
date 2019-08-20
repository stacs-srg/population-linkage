package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.data.synthetic.ImportSyntheticScotlandRecordsToStore;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkage;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GroundTruthLinkCounter extends SyntheticBirthBirthSiblingLinkageRunner {

    private String sourceRepoName;
    private String populationName;
    private String populationSize;
    private String populationNumber;
    private boolean corrupted;
    private String corruptionNumber;
    private Path resultsFile;

    public GroundTruthLinkCounter(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber, Path resultsFile) {

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

    public void count() {
        System.out.println("Count ground truth links in population: " + sourceRepoName);

        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                Files.write(resultsFile, ("population,size,pop#,corruption#,#gtLinks,count-time-seconds" +
                        System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }

            long startTime = System.currentTimeMillis();
            int numberOfGTLinks = countNumberOfGroundTruthLinks(sourceRepoName);
            long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

            Files.write(resultsFile, (populationName + "," + populationSize + "," + populationNumber + "," +
                            corruptionNumber + "," + numberOfGTLinks + "," +
                            timeTakenInSeconds + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
//        new GroundTruthLinkCounter(args[0], args[1], args[2], args[3].equals("true"), args[4], Paths.get(args[5])).count();
        countAll(Paths.get(args[5]));
    }


    public static void countAll(Path resultsFile) {

        String[] populationNames   = {"synthetic-scotland"};
        String[] populationSizes   = {"13k","133k"}; // ,"530k"
        String[] populationNumbers = {"1"}; // ,"2","3","4","5"
        String[] corruptionNumbers = {"0"}; // ,"1","2"

        for(String populationName : populationNames)
            for (String populationSize : populationSizes)
                for(String populationNumber : populationNumbers)
                    for(String corruptionNumber : corruptionNumbers)
                        new GroundTruthLinkCounter(populationName, populationSize,
                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber, resultsFile).count();


    }
}
