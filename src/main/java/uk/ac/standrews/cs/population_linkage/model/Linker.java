package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.PercentageProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

public abstract class Linker {

    protected double threshold;
    protected final NamedMetric<LXP> distance_metric;
    protected  List<LXP> records1;
    protected  List<LXP> records2;

    protected final ProgressIndicator linkage_progress_indicator;

    public Linker(NamedMetric<LXP> distance_metric, int number_of_progress_updates) {

        this.distance_metric = distance_metric;
        threshold = Double.MAX_VALUE;
        linkage_progress_indicator = new PercentageProgressIndicator(number_of_progress_updates);
    }

    public void addRecords(List<LXP> records) {

        addRecords(records, records);
    }

    public void addRecords(List<LXP> records1, List<LXP> records2) {

        this.records1 = records1;
        this.records2 = records2;
    }

    public Links link() {

        Links links = new Links();

        for (RecordPair pair : getMatchingRecordPairs(records1, records2)) {

            if (pair.distance <= threshold) {

//                Role role1 = new Role(getIdentifier1(pair.record1), getRoleType1());
//                Role role2 = new Role(getIdentifier2(pair.record2), getRoleType2());
//
//                links.add(new Link(role1, role2, 1.0f, getLinkType(), getProvenance()));
            }
        }

        return links;
    }

    public void setThreshold(double threshold) {

        this.threshold = threshold;
    }

    public Metric<LXP> getMetric() {
        return distance_metric;
    }

    protected abstract Iterable<RecordPair> getMatchingRecordPairs(final List<LXP> records1, final List<LXP> records2);

    protected abstract String getLinkType();

    protected abstract String getProvenance();

    protected abstract String getRoleType1();

    protected abstract String getRoleType2();

    protected abstract String getIdentifier1(LXP record);

    protected abstract String getIdentifier2(LXP record);
}
