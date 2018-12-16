package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.linkage.WeightedAverageLevenshtein;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class BitBlasterLinkageTest extends LinkageTest {

    @Override
    public Linker getLinker() {

        NamedMetric<LXP> metric = new WeightedAverageLevenshtein<>();

        SearchStructureFactory factory = records -> new BitBlasterSearchStructure<>(metric, records);

        return new SimilaritySearchSiblingBundlerOverBirths(factory, 2.0, metric,0);
    }

    @Override
    protected boolean equal(RecordPair pair1, RecordPair pair2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        return pair1.record1.equals(pair2.record1) && pair1.record2.equals(pair2.record2) ||
                pair1.record1.equals(pair2.record2) && pair1.record2.equals(pair2.record1);
    }
}
