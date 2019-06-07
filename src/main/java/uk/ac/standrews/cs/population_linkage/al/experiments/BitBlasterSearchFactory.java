package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.linkage.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class BitBlasterSearchFactory<LXP> implements SearchStructureFactory<LXP> {

    private final NamedMetric<LXP> composite_metric;

    public BitBlasterSearchFactory( NamedMetric<LXP> composite_metric ) {
        this.composite_metric = composite_metric;
    }

    @Override
    public SearchStructure<LXP> newSearchStructure(final Iterable<LXP> records) {
        return new BitBlasterSearchStructure<>(composite_metric, records);
        }


    @Override
    public String getSearchStructureType() {
        return "BitBlaster";
    }
}
