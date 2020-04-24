/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.searchStructures;

import uk.ac.standrews.cs.utilities.m_tree.MTree;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.List;

public class MTreeSearchStructure<T> implements SearchStructure<T> {

    private MTree<T> m_tree;

    public MTreeSearchStructure(Metric<T> distance_metric, Iterable<T> records) {

        m_tree = new MTree<>(distance_metric);
        for (T record : records) {
            m_tree.add(record);
        }
    }

    @Override
    public List<DataDistance<T>> findWithinThreshold(final T record, final double threshold) {

        return m_tree.rangeSearch(record, threshold);
    }

    public void terminate() {}
}
