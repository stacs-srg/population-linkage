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
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkViabilityChecker;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.util.Iterator;

public abstract class BruteForceLinker extends Linker {

    /**
     * @param number_of_progress_updates the number of updates to be given, zero or negative to suppress updates
     */
    public BruteForceLinker(LXPMeasure composite_measure, double threshold, int number_of_progress_updates,
                            String link_type, String provenance, String role_type_1, String role_type_2, LinkViabilityChecker link_viability_checker) {

        super(composite_measure, threshold, number_of_progress_updates, link_type, provenance, role_type_1, role_type_2, link_viability_checker);
    }

    @Override
    public Iterable<RecordPair> getMatchingRecordPairs(final Iterable<LXP> records1, final Iterable<LXP> records2) {

        return new Iterable<>() {

            class RecordPairIterator extends AbstractRecordPairIterator {

                int records1_index;
                int records2_index;

                Iterator<LXP> records1_iterator;
                Iterator<LXP> records2_iterator;

                LXP record1;
                LXP record2;

                RecordPairIterator(final Iterable<LXP> records1, final Iterable<LXP> records2, ProgressIndicator progress_indicator) {

                    super(records1, records2, progress_indicator);

                    records1_iterator = records1.iterator();
                    records2_iterator = records2.iterator();

                    record1 = records1_iterator.next();
                    record2 = records2_iterator.next();

                    records1_index = 0;
                    records2_index = 0;

                    // Don't compare record with itself.
                    if (datasets_same) {
                        record2 = records2_iterator.next();
                        records2_index = 1;
                    }

                    progress_indicator.setTotalSteps(count(records1) * count(records2));
                    getNextMatchingPair();
                }

                public boolean match(RecordPair pair) {

                    return pair.distance <= threshold;
                }

                void loadNextPair() {

                    next_pair = (record1 == null || record2 == null) ? null : new RecordPair(record1, record2, composite_measure.distance(record1, record2));
                }

                void advanceIndices() {

                    if (records2_iterator.hasNext()) {

                        record2 = records2_iterator.next();

                        // Don't compare record with itself.
                        if (datasets_same && record1.getId() == record2.getId() ) {
                            if (records2_iterator.hasNext()) {
                                record2 = records2_iterator.next();
                            }
                            else {
                                record2 = null;
                            }
                        }
                    }
                    else {

                        if (records1_iterator.hasNext()) {

                            record1 = records1_iterator.next();
                            records2_iterator = records2.iterator();
                            record2 = records2_iterator.next();
                        }
                        else {
                            record1 = null;
                        }
                    }

                    progress_indicator.progressStep();
                }
            }

            @Override
            public Iterator<RecordPair> iterator() {

                return new RecordPairIterator(records1, records2, linkage_progress_indicator);
            }
        };
    }
}
