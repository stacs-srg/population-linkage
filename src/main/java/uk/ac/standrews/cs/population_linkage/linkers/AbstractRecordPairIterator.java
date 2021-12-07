/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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
