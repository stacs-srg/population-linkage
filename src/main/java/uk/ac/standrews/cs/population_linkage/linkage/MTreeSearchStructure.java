package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.m_tree.MTree;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

public class MTreeSearchStructure<T extends LXP> implements SearchStructure<T> {

    MTree<T> m_tree;

    public MTreeSearchStructure(NamedMetric<T> distance_metric) {

        m_tree = new MTree<>(distance_metric);
    }

    @Override
    public void add(T record) {

        m_tree.add(record);
    }

    @Override
    public List<DataDistance<T>> findNearest(T record, int number_of_records) {

        return m_tree.nearestN(record, number_of_records);
    }
}
