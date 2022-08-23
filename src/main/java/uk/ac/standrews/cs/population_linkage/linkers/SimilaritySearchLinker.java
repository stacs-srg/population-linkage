/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.linkers;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructure;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.utilities.ProgressIndicator;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.DataDistance;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimilaritySearchLinker extends Linker {

    private final SearchStructureFactory<LXP> search_structure_factory;
    private final LinkageRecipe linkage_recipe;
    protected SearchStructure<LXP> search_structure;
    protected Iterable<LXP> search_set;

    public SimilaritySearchLinker(SearchStructureFactory<LXP> search_structure_factory, LXPMeasure distance_measure, double threshold, int number_of_progress_updates,
                                  String link_type, String provenance, String role_type_1, String role_type_2, LinkageRecipe linkage_recipe) {

        super(distance_measure, threshold, number_of_progress_updates, link_type, provenance, role_type_1, role_type_2, linkage_recipe);

        this.search_structure_factory = search_structure_factory;
        this.linkage_recipe = linkage_recipe;
    }

    public void addRecords(Iterable<LXP> storedSet, Iterable<LXP> searchSet) {

        super.addRecords(storedSet, searchSet);
        this.search_set = searchSet;

        search_structure = search_structure_factory.newSearchStructure(storedSet);
    }

    public void addRecords(Iterable<LXP> storedSet, Iterable<LXP> searchSet, List<LXP> reference_objects) {

        super.addRecords(storedSet, searchSet);
        this.search_set = searchSet;

        search_structure = search_structure_factory.newSearchStructure(storedSet, reference_objects);
    }

    public void close() {
        if( search_structure != null ) { // if it were not initialised
            search_structure.terminate();
        }
    }

    @Override
    public Iterable<List<RecordPair>> getMatchingLists() {

        Iterator<LXP> search_set_iterator = search_set.iterator(); // these are the records we are using as key to search (i.e. we're searching for the nearest thing to these in the stored records)

        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return search_set_iterator.hasNext();
            }

            @Override
            public List<RecordPair> next() {

                final LXP next_record_from_search_set = search_set_iterator.next();
                // the next_record_from_search_set converted into the same type as the stored records
                final LXP converted_record = linkage_recipe != null ? linkage_recipe.convertToOtherRecordType(next_record_from_search_set) : next_record_from_search_set;

                return toRecordPairList(next_record_from_search_set, search_structure.findWithinThreshold(converted_record, threshold)).collect(Collectors.toList());
            }
        };
    }

    private Stream<RecordPair> toRecordPairList(LXP search_record, List<DataDistance<LXP>> withinThreshold) {
        return withinThreshold.stream().map( dd -> new RecordPair(dd.value, search_record, dd.distance) );
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2) {

        return new Iterable<>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                private int result_index; // this is the index into the result_records
                private List<DataDistance<LXP>> result_records; // these are the results we get back from doing a search from findWithInThreshold

                private LXP next_record_from_search_set; // this is the current record from the search set being processed
                private final Iterator<LXP> search_set_iterator; // these are the records we are using as key to search (i.e. we're searching for the nearest thing to these in the stored records)

                RecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    search_set_iterator = search_set.iterator();

                    loadNextSearchResults();

                    if (linkage_recipe != null) progress_indicator.setTotalSteps(linkage_recipe.getQuerySetSize());

                    getNextPair();
                }

                private void loadNextSearchResults() {

                    next_record_from_search_set = search_set_iterator.next();
                    // the next_record_from_search_set converted into the same type as the stored records
                    final LXP converted_record = linkage_recipe != null ? linkage_recipe.convertToOtherRecordType(next_record_from_search_set) : next_record_from_search_set;

                    result_index = 0;
                    result_records = search_structure.findWithinThreshold(converted_record, threshold);
                }

                @Override
                public boolean match(final RecordPair pair) {
                    return true;
                }

                void getNextPair() {

                    while (search_set_iterator.hasNext() && !moreResultsAvailable()) {
                        getNextRecordFromSearchSet();
                    }

                    loadPair();

                    if (pairShouldBeSkipped()) {
                        next_pair = null;
                    }
                }

                private void loadPair() {

                    do {
                        if (moreLinksToConsider()) { // either something in outer loop to process or there are more results

                            if (!moreResultsAvailable()) {
                                // Case when result records empty - there are not more results to process
                                getNextRecordFromSearchSet();
                            }

                            DataDistance<LXP> data_distance = result_records.get(result_index++);
                            next_pair = new RecordPair(data_distance.value, next_record_from_search_set, data_distance.distance);

                            if (!moreResultsAvailable())
                                getNextRecordFromSearchSet();

                        } else {
                            next_pair = null;
                        }
                    }
                    while (moreLinksToConsider() && pairShouldBeSkipped());
                }

                private boolean moreLinksToConsider() {

                    return search_set_iterator.hasNext() || moreResultsAvailable();
                }

                private boolean moreResultsAvailable() {

                    return result_index < result_records.size();
                }

                private boolean pairShouldBeSkipped() {

                    return next_pair == null || (datasets_same && next_pair.stored_record.getId() == next_pair.query_record.getId());
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
