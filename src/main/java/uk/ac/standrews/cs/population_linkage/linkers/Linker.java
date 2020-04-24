/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.interfaces.IStoreReference;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public abstract class Linker {

    protected final Metric<LXP> distance_metric;
    protected final ProgressIndicator linkage_progress_indicator;
    private final Function<RecordPair, Boolean> is_viable_link;
    protected double threshold;
    private Iterable<LXP> records1;
    private Iterable<LXP> records2;
    private String link_type;
    private String provenance;
    private String role_type_1;
    private String role_type_2;

    public Linker(Metric<LXP> distance_metric, double threshold, int number_of_progress_updates,
                  String link_type, String provenance, String role_type_1, String role_type_2, Function<RecordPair, Boolean> is_viable_link) {

        this.link_type = link_type;
        this.provenance = provenance;
        this.role_type_1 = role_type_1;
        this.role_type_2 = role_type_2;
        this.is_viable_link = is_viable_link;

        this.distance_metric = distance_metric;
        this.threshold = threshold;
        linkage_progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public void addRecords(Iterable<LXP> records1, Iterable<LXP> records2) {

        this.records1 = records1;
        this.records2 = records2;
    }

    public void terminate() {
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

                        if (next == null) {
                            try {
                                getNextLink();
                            } catch (NoSuchElementException e) {
                                return false;
                            }
                        }

                        return true;
                    }

                    @Override
                    public Link next() {

                        if (next == null) {
                            getNextLink();
                        }

                        Link next_link = next;
                        next = null;

                        return next_link;
                    }
                };
            }

            private void getNextLink() {

                if (matching_pairs.hasNext()) {

                    RecordPair pair;
                    do {
                        pair = matching_pairs.next();
                    } while ((pair.distance > threshold || !is_viable_link.apply(pair)) && matching_pairs.hasNext());

                    if (pair.distance <= threshold && is_viable_link.apply(pair)) {

                        try {
                            next = new Link(pair.record1, getRoleType1(), pair.record2, getRoleType2(), 1.0f,
                                    getLinkType(), pair.distance, getProvenance() + ", distance: " + pair.distance);
                        } catch (PersistentObjectException e) {
                            throw new RuntimeException(e);
                        }
                    } else throw new NoSuchElementException();
                } else throw new NoSuchElementException();
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

    public String getLinkType() {
        return link_type;
    }

    public String getProvenance() {
        return provenance;
    }

    public String getRoleType1() {
        return role_type_1;
    }

    public String getRoleType2() {
        return role_type_2;
    }

    public IStoreReference getIdentifier1(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }

    public IStoreReference getIdentifier2(LXP record) throws PersistentObjectException {
        return record.getThisRef();
    }

    protected int count(final Iterable<LXP> records) {

        int i = 0;
        for (LXP ignored : records) {
            i++;
        }
        return i;
    }
}
