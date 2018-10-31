package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.Iterator;
import java.util.List;

abstract class AbstractRecordPairIterator implements Iterator<RecordPair> {

    final List<LXP> records1;
    final List<LXP> records2;
    final boolean datasets_same;
    final ProgressIndicator progress_indicator;
    private Double threshold;
    RecordPair next_pair;

    AbstractRecordPairIterator(final List<LXP> records1, final List<LXP> records2, ProgressIndicator progress_indicator, Double threshold) {

        this.records1 = records1;
        this.records2 = records2;
        datasets_same = records1 == records2;
        this.progress_indicator = progress_indicator;
        this.threshold = threshold;
    }

    abstract boolean finished();
    abstract void advanceIndices();
    abstract void loadNextPair();

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

        if (!finished()) {

            loadNextPair();

            advanceIndices();

        } else {
            next_pair = null;
        }
    }

    private boolean match(RecordPair pair) {

        return pair.distance <= threshold;
    }
}
