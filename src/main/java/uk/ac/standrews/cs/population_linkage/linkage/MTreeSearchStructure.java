package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.m_tree.MTree;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

public class MTreeSearchStructure<T extends LXP> implements SearchStructure<T> {

    MTree<T> m_tree;

    public MTreeSearchStructure(NamedMetric<T> distance_metric, List<T> records) {

        m_tree = new MTree<>(distance_metric);
        for (T record : records) {
            m_tree.add(record);
        }
    }

    public List<DataDistance<T>> findNearest(T record, int number_of_records) {

        return m_tree.nearestN(record, number_of_records);
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        return m_tree.rangeSearch(record, threshold);
    }
}
