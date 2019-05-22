package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchSiblingBundlerOverBirths;
import uk.ac.standrews.cs.population_linkage.model.Linker;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;

public abstract class SimilaritySearchSiblingBundling extends SiblingBundling {

    SimilaritySearchSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    protected abstract SearchStructureFactory<LXP> getSearchStructureFactory();

    @Override
    protected void printHeader() {

        System.out.println("Sibling bundling using " + getSearchStructureFactory().getSearchStructureType() + ", " + getBaseMetric().getMetricName() + " with threshold " + getMatchThreshold() + " from repository: " + repo_name);
    }

    @Override
    protected Linker getLinker() {

        SearchStructureFactory<LXP> factory = getSearchStructureFactory();

        return new SimilaritySearchSiblingBundlerOverBirths(factory, getMatchThreshold(), getCompositeMetric(), getNumberOfProgressUpdates());
    }

    protected RecordRepository getRecordRepository()  {
        System.out.println( "Using RecordRepository named - " + repo_name + " from " + store_path );
        return new RecordRepository(store_path, repo_name);
    }

    protected Iterable<LXP> getRecords(RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }
}
