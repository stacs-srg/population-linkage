package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.similarity_search;

import uk.ac.standrews.cs.population_linkage.experiments.linkage.RecordPair;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.SimilaritySearchLinker;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.LinkagePostFilter;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class SimilaritySearchSiblingBundlerOverBirthsAndDeaths extends SimilaritySearchLinker {


    public SimilaritySearchSiblingBundlerOverBirthsAndDeaths(SearchStructureFactory<LXP> search_structure_factory, double threshold, Metric<LXP> distance_metric, int number_of_progress_updates) {
        super(search_structure_factory, distance_metric, number_of_progress_updates);
        setThreshold(threshold);
    }

    @Override
    protected String getLinkType() {
        return "birth-death-sibling";
    }

    @Override
    protected String getProvenance() {
        return "threshold match at " + threshold;
    }

    @Override
    protected String getRoleType1() {
        return Birth.ROLE_BABY;
    }

    @Override
    protected String getRoleType2() {
        return Death.ROLE_DECEASED;
    }

    @Override
    protected IStoreReference getIdentifier1(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }

    @Override
    protected IStoreReference getIdentifier2(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }

    protected boolean isViableLink(RecordPair pair) {
        return LinkagePostFilter.isViableBDSiblingLink(pair);
    }

}
