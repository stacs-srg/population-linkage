package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class SimilaritySearchLinker extends Linker {

    private SearchStructureFactory<LXP> search_structure_factory;
    private SearchStructure<LXP> search_structure;
    private Iterable<LXP> searchSet;
    private int smaller_set_size;
//    private boolean recordOrderFlipped = false;
    private LinkageRecipe linkageRecipe;

    public SimilaritySearchLinker(SearchStructureFactory<LXP> search_structure_factory, Metric<LXP> distance_metric, double threshold, int number_of_progress_updates,
                                  String link_type, String provenace, String role_type_1, String role_type_2, Function<RecordPair, Boolean> isViableLink, LinkageRecipe linkageRecipe) {

        super(distance_metric, threshold, number_of_progress_updates, link_type, provenace, role_type_1, role_type_2, isViableLink);

        this.search_structure_factory = search_structure_factory;
        this.linkageRecipe = linkageRecipe;
    }

    public void addRecords(Iterable<LXP> storedSet, Iterable<LXP> searchSet) {
        super.addRecords(storedSet, searchSet);
        this.searchSet = searchSet;

        search_structure = search_structure_factory.newSearchStructure(storedSet);
    }

    public void terminate() {
        search_structure.terminate();
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2) {

        return new Iterable<RecordPair>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                private int result_index; // this is the index into the result_records
                private List<DataDistance<LXP>> result_records; // these are the results we get back from doing a search from findWithInThreshold

                private LXP next_record_from_search_set; // this is the current record from the search set being processed
                private Iterator<LXP> search_set_iterator; // these are the records we are using as key to search (i.e. we're searching for the nearest thing to these in the stored records)

                private LXP converted_record; // the next_record_from_search_set converted into the same type as the stored records


                RecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    search_set_iterator = searchSet.iterator();

                    loadNextSearchResults();

                    progress_indicator.setTotalSteps(linkageRecipe.getSearchSetSize());

                    getNextPair();
                }

                private void loadNextSearchResults() {
                    next_record_from_search_set = search_set_iterator.next();
                    converted_record = linkageRecipe.convertToOtherRecordType(next_record_from_search_set);

                    result_index = 0;
                    result_records = search_structure.findWithinThreshold(converted_record, threshold);
                }

                @Override
                boolean match(final RecordPair pair) {
                    return true;
                }

                void getNextPair() {

                    while (search_set_iterator.hasNext() && !moreResultsAvailiable()) {
                        getNextRecordFromSearchSet();
                    }

                    loadPair();

                    if (pairShouldBeSkipped()) {
                        next_pair = null;
                    }
                }

                private void loadPair() {

                    do {
                        if (moreLinksToConsider()) {

                            DataDistance<LXP> data_distance = result_records.get(result_index++);
                            next_pair = new RecordPair(data_distance.value, next_record_from_search_set, data_distance.distance);

                            if (!moreResultsAvailiable())
                                getNextRecordFromSearchSet();

                        } else {
                            next_pair = null;
                        }
                    }
                    while (moreLinksToConsider() && pairShouldBeSkipped());
                }

                private boolean moreLinksToConsider() {

                    return search_set_iterator.hasNext() || moreResultsAvailiable();
                }

                private boolean moreResultsAvailiable() {

                    return result_index < result_records.size();
                }

                private boolean pairShouldBeSkipped() {

                    return next_pair == null || (datasets_same && next_pair.record1.getId() == next_pair.record2.getId());
                }

                private void getNextRecordFromSearchSet() {

                    if (search_set_iterator.hasNext()) {

                        progress_indicator.progressStep();
                        loadNextSearchResults();

                    }
                }


            }

            @Override
            public Iterator<RecordPair> iterator() {
                return new RecordPairIterator(records1, records2, linkage_progress_indicator);
            }
        };
    }
}
