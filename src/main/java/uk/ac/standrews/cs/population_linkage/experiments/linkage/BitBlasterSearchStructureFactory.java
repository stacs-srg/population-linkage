package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class BitBlasterSearchStructureFactory<T> implements SearchStructureFactory<T> {

    private final Metric<T> composite_metric;

    public BitBlasterSearchStructureFactory(Metric<T> composite_metric) {
        this.composite_metric = composite_metric;
    }

    @Override
    public SearchStructure<T> newSearchStructure(final Iterable<T> records) {
        return new BitBlasterSearchStructure<>(composite_metric, records);
    }

    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}
