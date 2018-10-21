package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.*;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;

import java.util.Iterator;
import java.util.List;

public abstract class SimilaritySearchLinker extends Linker {

    private final SearchStructure<LXP> search_structure;
    protected final double threshold;
    private final int number_of_records_to_consider;

    public SimilaritySearchLinker(SearchStructure<LXP> search_structure, double threshold, int number_of_records_to_consider, int number_of_progress_updates) {

        super(number_of_progress_updates);

        this.search_structure = search_structure;
        this.threshold = threshold;
        this.number_of_records_to_consider = number_of_records_to_consider;
    }

    @Override
    public Links link(List<LXP> records) {

        return link(records, records);
    }

    @Override
    public Links link(List<LXP> records1, List<LXP> records2) {

        Links links = new Links();

        for (RecordPair record_pair : getRecordPairs(records1, records2)) {

            LXP record1 = record_pair.record1;
            LXP record2 = record_pair.record2;

            String id1 = getIdentifier1(record1);
            String id2 = getIdentifier2(record2);

            if (record_pair.distance <= threshold) {

                Role role1 = new Role(id1, getRoleType1());
                Role role2 = new Role(id2, getRoleType2());
                links.add(new Link(role1, role2, 1.0f, getProvenance()));
            }
        }

        return links;
    }

    private Iterable<RecordPair> getRecordPairs(final List<LXP> records1, final List<LXP> records2) {

        List<LXP> smaller_set = records1.size() < records2.size() ? records1 : records2;
        List<LXP> larger_set = records1.size() < records2.size() ? records2 : records2;

        for (LXP record : larger_set) {
            search_structure.add(record);
        }

        progress_indicator.setTotalSteps(smaller_set.size());

        return () -> new Iterator<RecordPair>() {

            int i = 0;
            int j = 1;
            List<DataDistance<LXP>> nearest_records = search_structure.findNearest(smaller_set.get(i), number_of_records_to_consider);

            @Override
            public boolean hasNext() {
                return i < smaller_set.size() && j < nearest_records.size();
            }

            @Override
            public RecordPair next() {

                RecordPair next_pair = new RecordPair(smaller_set.get(i), nearest_records.get(j).value, nearest_records.get(j).distance);

                j++;

                if (j >= nearest_records.size()) {

                    i++;
                    j = 1;
                    nearest_records = search_structure.findNearest(smaller_set.get(i), number_of_records_to_consider);
                    if (number_of_progress_updates > 0) progress_indicator.progressStep();
                }
                return next_pair;
            }
        };
    }
}
