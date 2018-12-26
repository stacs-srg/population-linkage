package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Linker {

    protected double threshold;
    protected final NamedMetric<LXP> distance_metric;
    private Iterable<LXP> records1;
    private Iterable<LXP> records2;

    protected final ProgressIndicator linkage_progress_indicator;

    public Linker(NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        this.distance_metric = distance_metric;
        threshold = Double.MAX_VALUE;
        linkage_progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public void addRecords(Iterable<LXP> records1, Iterable<LXP> records2) {

        this.records1 = records1;
        this.records2 = records2;
    }

    public Iterable<Link> getLinks() {

        final Iterator<RecordPair> matching_pairs = getMatchingRecordPairs(records1, records2).iterator();

        return new Iterable<Link>() {

            private Link next = null;

            @Override
            public Iterator<Link> iterator() {

                return new Iterator<Link>() {

                    @Override
                    public boolean hasNext() {

                        return matching_pairs.hasNext();
                    }

                    @Override
                    public Link next() {

                        getNextLink();
                        return next;
                    }
                };
            }

            private void getNextLink() {

                if (matching_pairs.hasNext()) {

                    RecordPair pair;
                    do {
                        pair = matching_pairs.next();
                    }
                    while (pair.distance > threshold && matching_pairs.hasNext());

                    if (pair.distance <= threshold) {

                        Role role1 = new Role(getIdentifier1(pair.record1), getRoleType1());
                        Role role2 = new Role(getIdentifier2(pair.record2), getRoleType2());

                        next = new Link(role1, role2, 1.0f, getLinkType(), getProvenance());
                    }
                    else throw new NoSuchElementException();
                }
                else throw new NoSuchElementException();
            }
        };
    }

    public void setThreshold(double threshold) {

        this.threshold = threshold;
    }

    public Metric<LXP> getMetric() {
        return distance_metric;
    }

    protected abstract Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2);

    protected abstract String getLinkType();

    protected abstract String getProvenance();

    protected abstract String getRoleType1();

    protected abstract String getRoleType2();

    protected abstract String getIdentifier1(LXP record);

    protected abstract String getIdentifier2(LXP record);

    protected int count(final Iterable<LXP> records) {

        int i = 0;
        for (LXP ignored : records) {
            i++;
        }
        return i;
    }
}
