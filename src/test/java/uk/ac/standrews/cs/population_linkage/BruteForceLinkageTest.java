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

import uk.ac.standrews.cs.neoStorr.interfaces.IStoreReference;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.linkers.BruteForceLinker;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;

import java.util.List;

public class BruteForceLinkageTest extends LinkageTest {

    @Override
    public Linker getLinker() {

        return new TestLinker(Double.MAX_VALUE, measure);
    }

    @Override
    protected boolean equal(final Link link, final IStoreReference id1, final IStoreReference id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        IStoreReference link_id1 = link.getRecord1();
        IStoreReference link_id2 = link.getRecord2();

        return (link_id1.equals(id1) && link_id2.equals(id2) || link_id2.equals(id1) && link_id1.equals(id2) );
    }

    class TestLinker extends BruteForceLinker {

        TestLinker(double threshold, final LXPMeasure measure) {

            super(measure, 0.67, 0, "link type", "provenance", "role1", "role2", null);
            setThreshold(threshold);
        }

        @Override
        public void close() {
        }

        @Override
        public Iterable<List<RecordPair>> getMatchingLists() {
            return null;
        }
    }
}
