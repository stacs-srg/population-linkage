/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

/**
 * Defines a distance function between LXP records as the sum of the distances between values of specified fields.
 */
public class SumOfFieldDistances extends LXPMeasure {

    public SumOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list) {

        this(base_measure, field_list, field_list);
    }

    public SumOfFieldDistances(final StringMeasure base_measure, final List<Integer> field_list1, final List<Integer> field_list2) {

        super(base_measure, field_list1, field_list2);
    }

    @Override
    public String getMeasureName() {
        return "Sum of field distances using: " + base_measure.getMeasureName();
    }

    @Override
    public boolean maxDistanceIsOne() {
        return false;
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {

        return sumOfFieldDistances(x, y);
    }

    public static void main(String[] args) {

        final SumOfFieldDistances birth_birth_measure1 = new SumOfFieldDistances(Constants.LEVENSHTEIN, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
        final SumOfFieldDistances birth_death_measure1 = new SumOfFieldDistances(Constants.LEVENSHTEIN, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);

        final SumOfFieldDistances birth_birth_measure2 = new SumOfFieldDistances(Constants.SED, BirthSiblingLinkageRecipe.LINKAGE_FIELDS);
        final SumOfFieldDistances birth_death_measure2 = new SumOfFieldDistances(Constants.SED, BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS, BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS);

        printExamples(birth_birth_measure1, birth_death_measure1);
        printExamples(birth_birth_measure2, birth_death_measure2);
    }
}
