package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;

public class MTreeLinkageTest extends SimilaritySearchLinkageTest {

    @Override
    public Linker getLinker() {

        return new TestLinker(records -> new MTreeSearchStructure<>(metric, records), 2.0, metric,0);
    }
}
