package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    public static void main(String[] args) {

        String sourceRepoName = args[0];
        String resultsRepoName = args[1];
        double threshold = Double.valueOf(args[2]);

        new SyntheticBirthBirthSiblingLinkageRunner().run("BirthBirthSiblingLinks", "BirthBirthSiblingGroundTruth", sourceRepoName, resultsRepoName, threshold);
    }

}
