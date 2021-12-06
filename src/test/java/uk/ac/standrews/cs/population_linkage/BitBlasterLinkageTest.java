/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage;

import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.searchStructures.BitBlasterSearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.neoStorr.impl.LXP;

import java.util.List;

public class BitBlasterLinkageTest extends SimilaritySearchLinkageTest {

    @Override
    public Linker getLinker() {

        SearchStructureFactory<LXP> factory = new SearchStructureFactory<>() {

            @Override
            public String getSearchStructureType() {
                return "BitBlaster";
            }

            @Override
            public SearchStructure<LXP> newSearchStructure(Iterable<LXP> records, List<LXP> reference_objects)  {
                return new BitBlasterSearchStructure<LXP>(metric, reference_objects, records);
            }
        };

        return new TestLinker(factory, 2.0, metric, 0);
    }
}
