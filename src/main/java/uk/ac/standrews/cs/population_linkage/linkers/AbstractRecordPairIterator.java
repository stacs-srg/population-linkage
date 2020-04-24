/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.Iterator;

abstract class AbstractRecordPairIterator implements Iterator<RecordPair> {

    final Iterable<LXP> records1;
    final Iterable<LXP> records2;
    final boolean datasets_same;
    final ProgressIndicator progress_indicator;
    RecordPair next_pair;

    AbstractRecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

        this.records1 = records1;
        this.records2 = records2;
        datasets_same = records1 == records2;
        this.progress_indicator = progress_indicator;
    }

    abstract boolean match(RecordPair pair);

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
