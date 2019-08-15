package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    public static void main(String[] args) {

        String sourceRepoName = args[0];
        double threshold = Double.valueOf(args[1]);

        String stringMetric = args[2];
        StringMetric metric = Constants.get(stringMetric);

        Path resultsFile = Paths.get(args[3]);
        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                Files.write(resultsFile, ("dataset,threshold,metric,tp,fp,fn,precision,recall,f-measure" +
                        System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }

            LinkageQuality lq = new SyntheticBirthBirthSiblingLinkageRunner().evaluateOnly(sourceRepoName, threshold, metric);

            Files.write(resultsFile, (sourceRepoName + "," + threshold + "," + stringMetric + "," + lq.toCSV() +
                    System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }






    }

}
