package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;

public class BitBlasterLinkageTest extends SimilaritySearchLinkageTest {

    @Override
    public Linker getLinker() {

        return new TestLinker(records -> new BitBlasterSearchStructure<>(metric, records), 2.0, metric, 0);
    }
}
