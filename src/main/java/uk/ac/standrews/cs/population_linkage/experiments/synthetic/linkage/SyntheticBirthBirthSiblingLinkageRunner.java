package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    public static void main(String[] args) {

        String population = args[0];
        String populationSize = args[1];
        String populationNumber = args[2];
        boolean corrupted = args[3].equals("true");
        String corruptionNumber = args[4];

        String sourceRepoName;

        if(corrupted)
            sourceRepoName = population + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = population + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

        System.out.println("Linking population: " + sourceRepoName);

        double threshold = Double.valueOf(args[5]);

        String stringMetric = args[6];
        StringMetric metric = Constants.get(stringMetric);

        Path resultsFile = Paths.get(args[7]);



        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                Files.write(resultsFile, ("population,size,pop#,corruption#,threshold,metric,tp,fp,fn,precision,recall,f-measure,link-time-seconds" +
                        System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }

            long startTime = System.currentTimeMillis();
            LinkageQuality lq = new SyntheticBirthBirthSiblingLinkageRunner().evaluateOnly(sourceRepoName, threshold, metric);
            long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

            Files.write(resultsFile, (population + "," + populationSize + "," + populationNumber + "," +
                    corruptionNumber + "," + threshold + "," + stringMetric + "," + lq.toCSV() + "," +
                    timeTakenInSeconds + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
