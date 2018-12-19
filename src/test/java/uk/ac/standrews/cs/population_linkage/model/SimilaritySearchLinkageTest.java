package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.linkage.SimilaritySearchLinker;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public abstract class SimilaritySearchLinkageTest extends LinkageTest {

    @Override
    protected boolean equal(RecordPair pair1, RecordPair pair2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        return pair1.record1.equals(pair2.record1) && pair1.record2.equals(pair2.record2) ||
                pair1.record1.equals(pair2.record2) && pair1.record2.equals(pair2.record1);
    }

    @Override
    protected boolean equal(final Link link, final String id1, final String id2) {

        // Don't care which way round the records are in the pair.
        // The order will depend on which of the record sets was the largest and hence got put into the search structure.

        String link_id1 = link.getRole1().getRecordId();
        String link_id2 = link.getRole2().getRecordId();

        return (link_id1.equals(id1) && link_id2.equals(id2)) || (link_id1.equals(id2) && link_id2.equals(id1));
    }

     class TestLinker extends SimilaritySearchLinker {

        public TestLinker(SearchStructureFactory<LXP> search_structure_factory, double threshold, NamedMetric<LXP> metric, int number_of_progress_updates) {

            super(search_structure_factory, metric, number_of_progress_updates);
            setThreshold(threshold);
        }

        @Override
        protected String getLinkType() {
            return "link type";
        }

        @Override
        protected String getProvenance() {
            return "provenance";
        }

        @Override
        protected String getRoleType1() {
            return "role1";
        }

        @Override
        protected String getRoleType2() {
            return "role2";
        }

        @Override
        public String getIdentifier1(LXP record) {
            return record.getString(2);
        }

        @Override
        public String getIdentifier2(LXP record) {
            return record.getString(2);
        }
    }
}
