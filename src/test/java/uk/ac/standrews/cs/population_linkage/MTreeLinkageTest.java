/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.searchStructures.MTreeSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.neoStorr.impl.LXP;

import java.util.List;

public class MTreeLinkageTest extends SimilaritySearchLinkageTest {

    @Override
    public Linker getLinker() {

        SearchStructureFactory<LXP> factory = new SearchStructureFactory<LXP>() {

            @Override
            public String getSearchStructureType() {
                return "MTree";
            }

            @Override
            public SearchStructure<LXP> newSearchStructure(Iterable<LXP> records) {
                return new MTreeSearchStructure<>(metric, records);
            }
            @Override
            public SearchStructure<LXP> newSearchStructure(Iterable<LXP> records, List<LXP> reference_objects) {
                return new MTreeSearchStructure<>(metric, records);
            }
        };

        return new TestLinker(factory, 2.0, metric,0);
    }
}
