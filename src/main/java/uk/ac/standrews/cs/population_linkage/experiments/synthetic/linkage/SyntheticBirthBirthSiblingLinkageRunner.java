package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    public static void main(String[] args) {

        String sourceRepoName = args[0];
        double threshold = Double.valueOf(args[1]);

        String stringMetric = args[2];
        StringMetric metric = Constants.get(stringMetric);

        Path resultsFile = Paths.get(args[3]);
        OutputStreamWriter osw;


        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            osw = FileManipulation.getOutputStreamWriter(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                osw.write("dataset,threshold,metric,tp,fp,fn,precision,recall,f-measure\n");
            }

            LinkageQuality lq = new SyntheticBirthBirthSiblingLinkageRunner().evaluateOnly(sourceRepoName, threshold, metric);

            osw.write(sourceRepoName + "," + threshold + "," + stringMetric + "," + lq.toCSV() + "\n");
            osw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }






    }

}
