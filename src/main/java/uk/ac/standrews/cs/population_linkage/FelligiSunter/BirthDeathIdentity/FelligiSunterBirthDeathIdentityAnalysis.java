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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.Aggregator;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.AggregatorMean;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.Imputer;
import uk.ac.standrews.cs.population_linkage.groundTruth.TwoSourcesLinkageAnalysis;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.supportClasses.Constants.*;

/**
 * Performs linkage analysis on data from births and marriages.
 * It compares the baby's and parents' names on a birth record with the groom and his parents' names from a marriage record.
 * The fields used for comparison are listed in getComparisonFields() and getComparisonFields2().
 * This is identity linkage between the baby and the groom.
 */
public class FelligiSunterBirthDeathIdentityAnalysis extends TwoSourcesLinkageAnalysis {

    // Cutoff record distance for field distance measures that aren't intrinsically normalised;
    // all distances at or above the cutoff will be normalised to 1.0.
    private static final double NORMALISATION_CUTOFF = 30;

    FelligiSunterBirthDeathIdentityAnalysis(final String repo_name, final String[] args) throws IOException {
        super(repo_name, args, getLinkageResultsFilename(), getDistanceResultsFilename(), false);
    }

    @Override
    public Iterable<LXP> getSourceRecords1(final RecordRepository record_repository) {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2(final RecordRepository record_repository) {
        return Utilities.getDeathRecords(record_repository);
    }

    @Override
    public List<Integer> getComparisonFieldIndices1() {
        return BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getComparisonFieldIndices2() {
        return BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS;
    }

//    @Override
//    protected double getNormalisationCutoff() {
//        return NORMALISATION_CUTOFF;
//    }

    @Override
    public LinkStatus isTrueMatch(final LXP record1, final LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(final LXP record1, final LXP record2) {
        return BirthDeathIdentityLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return BirthDeathIdentityLinkageRecipe.isViable(record1, record2);
    }

    @Override
    public String getDatasetName() {
        return Umea.REPOSITORY_NAME;
    }

    @Override
    public String getLinkageType() {
        return "identity linkage between baby on birth record and deceased on death record";
    }

//    private static List<StringMeasure> measures = List.of( COSINE, JACCARD, JENSEN_SHANNON, SED); // true metrics returning between - and 1.
//
//    @Override
//    public List<LXPMeasure> getCombinedMeasures()  {
//
//        final List<LXPMeasure> result = new ArrayList<>();
//        for( StringMeasure base_measure : measures ) {
//            result.add(BirthOwnDeathBuilder.getRecipe(getDatasetName(), String.valueOf(number_of_records_to_be_checked)).getCompositeMeasure(base_measure));
//        }
//        return result;
//    }

    @Override
    protected List<StringMeasure> getBaseMeasures() {
        return List.of(COSINE, JACCARD, JENSEN_SHANNON, SED); // true metrics returning between - and 1.;
    }

    @Override
    protected List<Aggregator> getAggregators() {
        return List.of(new AggregatorMean());
    }

    @Override
    protected List<Imputer> getImputers() {
        return List.of(Imputer.RECORD_MEAN);
    }

    @Override
    protected Map<StringMeasure, List<Double>> getCutOffs() {
        return new HashMap<>();
    }

    @Override
    protected boolean recordLinkDistances() {
        return false;
    }

    public static void main(String[] args) throws Exception {

        new FelligiSunterBirthDeathIdentityAnalysis(Umea.REPOSITORY_NAME, args).run();
    }
}
