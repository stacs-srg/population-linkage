package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.LinkageConfig;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

public class SimilaritySearchSiblingBundlerOverBirths extends SimilaritySearchLinker {

    public SimilaritySearchSiblingBundlerOverBirths(SearchStructureFactory<LXP> search_structure_factory, double threshold, Metric<LXP> distance_metric, int number_of_progress_updates) {

        super(search_structure_factory, distance_metric, number_of_progress_updates);
        setThreshold(threshold);
    }

    public SimilaritySearchSiblingBundlerOverBirths(SearchStructureFactory<LXP> search_structure_factory, double threshold, Metric<LXP> distance_metric, int number_of_progress_updates, int numberOfReferenceObjects) {

        super(search_structure_factory, distance_metric, number_of_progress_updates, numberOfReferenceObjects);
        setThreshold(threshold);
    }

    @Override
    protected String getLinkType() {
        return "sibling";
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
        return Birth.ROLE_BABY;
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
        return LinkageConfig.isViableLink(pair);
    }


}
