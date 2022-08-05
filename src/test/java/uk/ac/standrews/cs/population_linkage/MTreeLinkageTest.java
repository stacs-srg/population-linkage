/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
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
                return new MTreeSearchStructure<>(measure, records);
            }
            @Override
            public SearchStructure<LXP> newSearchStructure(Iterable<LXP> records, List<LXP> reference_objects) {
                return new MTreeSearchStructure<>(measure, records);
            }
        };

        return new TestLinker(factory, 2.0, measure,0);
    }
}
