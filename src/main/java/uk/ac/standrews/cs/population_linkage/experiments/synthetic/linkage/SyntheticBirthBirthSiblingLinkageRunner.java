package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.umea.linkage.UmeaBirthBirthSiblingLinkageRunner;

public class SyntheticBirthBirthSiblingLinkageRunner extends UmeaBirthBirthSiblingLinkageRunner {

    public static void main(String[] args) {
        double match_threshold = 0.67;                          // from R metric power table [FRobustness2] - original 2.03 remapped to 0.67 by normalisation.

        new UmeaBirthBirthSiblingLinkageRunner().run("BirthBirthSiblingLinks", "BirthBirthSiblingGroundTruth", "scot-test_570k_1", "scot-test_570k_1_results", match_threshold);
    }

}
