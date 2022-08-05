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
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.Iterator;

public abstract class AbstractRecordPairIterator implements Iterator<RecordPair> {

    final Iterable<LXP> records1;
    final Iterable<LXP> records2;
    public final boolean datasets_same;
    public final ProgressIndicator progress_indicator;
    public RecordPair next_pair;

    public AbstractRecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

        this.records1 = records1;
        this.records2 = records2;
        datasets_same = records1 == records2;
        this.progress_indicator = progress_indicator;
    }

    public abstract boolean match(RecordPair pair);

    void advanceIndices() {
    }

    void loadNextPair() {
    }

    @Override
    public boolean hasNext() {
        return next_pair != null;
    }

    @Override
    public RecordPair next() {

        RecordPair result = next_pair;
        getNextMatchingPair();

        return result;
    }

    void getNextMatchingPair() {

        getNextPair();
        while (next_pair != null && !match(next_pair)) {
            getNextPair();
        }
    }

    void getNextPair() {

        loadNextPair();
        advanceIndices();
    }
}
