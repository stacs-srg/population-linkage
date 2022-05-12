/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkViabilityChecker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public abstract class Linker {

    protected final LXPMeasure composite_measure;
    protected final ProgressIndicator linkage_progress_indicator;
    protected double threshold;
    protected Iterable<LXP> records1;
    protected Iterable<LXP> records2;
    private final String link_type;
    private final String provenance;
    private final String role_type_1;
    private final String role_type_2;
    protected LinkViabilityChecker link_viability_checker;

    public Linker(LXPMeasure composite_measure, double threshold, int number_of_progress_updates,
                  String link_type, String provenance, String role_type_1, String role_type_2, LinkViabilityChecker link_viability_checker) {

        this.link_type = link_type;
        this.provenance = provenance;
        this.role_type_1 = role_type_1;
        this.role_type_2 = role_type_2;

        this.composite_measure = composite_measure;
        this.threshold = threshold;
        this.link_viability_checker = link_viability_checker;

        linkage_progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public void addRecords(Iterable<LXP> records1, Iterable<LXP> records2) {

        this.records1 = records1;
        this.records2 = records2;
    }

    public void terminate() {
    }

    public abstract Iterable<List<RecordPair>> getMatchingLists();

    public Iterable<Link> getLinks() {

        final Iterator<RecordPair> matching_pairs = getMatchingRecordPairs(records1, records2).iterator();

        return new Iterable<>() {

            private Link next = null;

            @Override
            public Iterator<Link> iterator() {

                return new Iterator<>() {

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

                        final Link next_link = next;
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
                    } while (notFinished(pair));

                    if (pair.distance <= threshold && (link_viability_checker == null || link_viability_checker.isViableLink(pair.stored_record, pair.query_record))) {

                        try {
                            next = new Link(pair.stored_record, getRoleType1(), pair.query_record, getRoleType2(), 1.0f,
                                    getLinkType(), pair.distance, getProvenance() + ", distance: " + pair.distance);
                        } catch (PersistentObjectException e) {
                            throw new RuntimeException(e);
                        }
                    } else throw new NoSuchElementException();
                } else throw new NoSuchElementException();
            }

            private boolean notFinished(final RecordPair pair) {

                final boolean non_viable = link_viability_checker != null && !link_viability_checker.isViableLink(pair.stored_record, pair.query_record);
                return (pair.distance > threshold || non_viable) && matching_pairs.hasNext();
            }
        };
    }

    /**
     * @return all the links per query rather than returning indvidual links as getLinks does.
     */
    public Iterable<List<Link>> getListsOfLinks() {

        final Iterator<List<RecordPair>> iter = getMatchingLists().iterator();

        return () -> new Iterator<>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public List<Link> next() {
                return toLinkList(iter.next());
            }

            private List<Link> toLinkList(List<RecordPair> pairs) {
                return pairs.stream().map(pair -> {
                    try {
                        return new Link(pair.stored_record, getRoleType1(), pair.query_record, getRoleType2(), 1.0f,
                                getLinkType(), pair.distance, getProvenance() + ", distance: " + pair.distance);
                    } catch (PersistentObjectException e) {
                        throw new RuntimeException(e);
                    }
                } ).collect(Collectors.toList());
            }
        };
    }

    public void setThreshold(double threshold) {

        this.threshold = threshold;
    }

    public LXPMeasure getMeasure() {
        return composite_measure;
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

    protected int count(final Iterable<LXP> records) {

        int i = 0;
        for (LXP ignored : records) {
            i++;
        }
        return i;
    }
}
