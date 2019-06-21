package uk.ac.standrews.cs.population_linkage.experiments_old;

import uk.ac.standrews.cs.population_linkage.linkage.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.nio.file.Path;

public abstract class MTreeSiblingBundling extends SimilaritySearchSiblingBundling {

     MTreeSiblingBundling(Path store_path, String repo_name) {

        super(store_path, repo_name);
    }

    @Override
    protected SearchStructureFactory<LXP> getSearchStructureFactory() {

        return new SearchStructureFactory<LXP>() {

            @Override
            public SearchStructure<LXP> newSearchStructure(final Iterable<LXP> records) {
                return new MTreeSearchStructure<>(getCompositeMetric(), records);
            }

            @Override
            public String getSearchStructureType() {
                return "MTree";
            }
        };
    }
}
