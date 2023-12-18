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
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.Measure;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.ArrayList;
import java.util.List;

public class LXPMeasure extends Measure<LXP> {

    public static final Imputer DEFAULT_IMPUTER = Imputer.RECORD_MEAN;
    public static final AggregatorMean DEFAULT_AGGREGATOR = new AggregatorMean();
    public static final double DEFAULT_CUT_OFF = Double.MAX_VALUE;

    public record FieldComparator(StringMeasure base_measure, double cut_off, boolean normalise, Imputer imputer) {}

    protected List<Integer> field_indices1;
    protected List<Integer> field_indices2;
    private int field_count;

    protected List<FieldComparator> field_comparators;
    protected Aggregator aggregator;

    public LXPMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final List<FieldComparator> field_comparators, final Aggregator aggregator) {

        init(field_indices1, field_indices2, field_comparators, aggregator);
    }

    public LXPMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure) {

        this(field_indices1, field_indices2, base_measure, DEFAULT_IMPUTER);
    }

    public LXPMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure, final Imputer imputer) {

        this(field_indices1, field_indices2, base_measure, imputer, DEFAULT_AGGREGATOR);
    }

    public LXPMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure, final Imputer imputer, final Aggregator aggregator) {

        this(field_indices1, field_indices2, base_measure, DEFAULT_CUT_OFF, true, imputer, aggregator);
    }

    public LXPMeasure(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure, final double cut_off, final boolean normalise, final Imputer imputer, final Aggregator aggregator) {

        FieldComparator field_comparator = new FieldComparator(base_measure, cut_off, normalise, imputer);
        List<FieldComparator> field_comparators = new ArrayList<>();
        for (int i = 0; i < field_indices1.size(); i++) field_comparators.add(field_comparator);

        init(field_indices1, field_indices2, field_comparators, aggregator);
    }

    private void init(List<Integer> field_indices1, List<Integer> field_indices2, List<FieldComparator> field_comparators, Aggregator aggregator) {

        if (field_indices1.size() != field_indices2.size() || field_indices1.size() != field_comparators.size())
            throw new RuntimeException("field index and comparator lists must have the same length");

        this.field_indices1 = field_indices1;
        this.field_indices2 = field_indices2;
        field_count = field_indices1.size();

        this.field_comparators = field_comparators;
        this.aggregator = aggregator;
    }

    @Override
    public double getMaxDistance() {

        double max_max_distance = 0;

        for (FieldComparator comparator : field_comparators) {
            double max_distance = comparator.base_measure.getMaxDistance();
            if (max_distance > max_max_distance)
                max_max_distance = max_distance;
        }

        return max_max_distance;
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {

        final List<Double> field_distances = new ArrayList<>(field_count);
        for (int i = 0; i < field_count; i++) field_distances.add(-1d);

        final List<Double> non_missing_field_distances = new ArrayList<>();

        for (int field = 0; field < field_count; field++) {

            final FieldComparator comparator = field_comparators.get(field);
            final String field_value1 = x.getString(field_indices1.get(field));
            final String field_value2 = y.getString(field_indices2.get(field));

            if (!RecordFiltering.isMissing(field_value1) && !RecordFiltering.isMissing(field_value2)) {

                double field_distance = comparator.base_measure.distance(field_value1, field_value2);

                field_distance = Math.min(field_distance, comparator.cut_off);

                if (comparator.normalise && comparator.base_measure.getMaxDistance() > 1) {
                    field_distance = field_distance / comparator.cut_off;
                }

                field_distances.set(field, field_distance);
                non_missing_field_distances.add(field_distance);
            }
        }

        if (non_missing_field_distances.size() < field_count) {

            for (int field = 0; field < field_count; field++) {

                if (field_distances.get(field) < 0)
                    field_distances.set(field, field_comparators.get(field).imputer.impute(non_missing_field_distances));
            }
        }

        return aggregator.aggregate(field_distances);
    }

    protected void throwExceptionWithDebug(LXP x, LXP y, int field_index, Exception e) {
        throw new RuntimeException("exception comparing fields " + x.getMetaData().getFieldName(field_indices1.get(field_index)) + " and " + y.getMetaData().getFieldName(field_indices2.get(field_index)) + " in records \n" + x + "\n and \n" + y, e);
    }

    protected static void printExceptionDebug(final LXP a, final LXP b, final int field_index, final int id_field_index) {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("Exception in distance calculation");
        System.out.println("field index list: " + field_index);
        System.out.println("a: " + (a == null ? "null" : "not null"));
        System.out.println("b: " + (b == null ? "null" : "not null"));
        System.out.println("id of a: " + a.getString(id_field_index));
        System.out.println("id of b: " + b.getString(id_field_index));
        System.out.println("field name a: " + a.getMetaData().getFieldName(field_index));
        System.out.println("field name b: " + b.getMetaData().getFieldName(field_index));
        System.out.println("field value a: " + a.getString(field_index));
        System.out.println("field value b: " + b.getString(field_index));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    protected static String displayNonEmptyFields(LXP lxp) {

        final StringBuilder builder = new StringBuilder();

        for (final String field : lxp.getMetaData().getFieldNamesInSlotOrder()) {

            String value = (String) lxp.get(field);

            if (value != null && !value.isEmpty()) {

                if (!builder.isEmpty()) builder.append("; ");
                builder.append(field);
                builder.append(":");
                builder.append(value);
            }
        }

        return builder.toString();
    }

    protected static Birth makeBirth(final String forename, final String surname, final String mother_forename, final String mother_maiden_surname,
                                     final String father_forename, final String father_surname, final String parents_place_of_marriage,
                                     final int parents_day_of_marriage, final int parents_month_of_marriage, final int parents_year_of_marriage) {

        final Birth record = new Birth();

        for (int i = 0; i < Birth.getLabels().size(); i++) {
            record.put(i, "");
        }

        record.put(Birth.FORENAME, forename);
        record.put(Birth.SURNAME, surname);
        record.put(Birth.MOTHER_FORENAME, mother_forename);
        record.put(Birth.MOTHER_MAIDEN_SURNAME, mother_maiden_surname);
        record.put(Birth.FATHER_FORENAME, father_forename);
        record.put(Birth.FATHER_SURNAME, father_surname);
        record.put(Birth.PARENTS_PLACE_OF_MARRIAGE, parents_place_of_marriage);
        record.put(Birth.PARENTS_DAY_OF_MARRIAGE, String.valueOf(parents_day_of_marriage));
        record.put(Birth.PARENTS_MONTH_OF_MARRIAGE, String.valueOf(parents_month_of_marriage));
        record.put(Birth.PARENTS_YEAR_OF_MARRIAGE, String.valueOf(parents_year_of_marriage));

        return record;
    }

    protected static Death makeDeath(final String forename, final String surname, final String mother_forename, final String mother_maiden_surname,
                                     final String father_forename, final String father_surname) {

        final Death record = new Death();

        for (int i = 0; i < Death.getLabels().size(); i++) {
            record.put(i, "");
        }

        record.put(Death.FORENAME, forename);
        record.put(Death.SURNAME, surname);
        record.put(Death.MOTHER_FORENAME, mother_forename);
        record.put(Death.MOTHER_MAIDEN_SURNAME, mother_maiden_surname);
        record.put(Death.FATHER_FORENAME, father_forename);
        record.put(Death.FATHER_SURNAME, father_surname);

        return record;
    }

    public static void printExample(LXPMeasure measure, LXP lxp1, LXP lxp2) {

        System.out.println();
        System.out.println(measure + ":");
        System.out.println("normalised: " + (measure.getMaxDistance() <= 1));
        System.out.println();

        System.out.println(displayNonEmptyFields(lxp1) + " / " + displayNonEmptyFields(lxp2) + ":\n" + measure.distance(lxp1, lxp2));
    }

    public static void printExamples(LXPMeasure birth_birth_measure, LXPMeasure birth_death_measure) {

        LXP birth1 = makeBirth("KAROLINA WILHELMINA","NÄS","MAGDAL. JOH:A","TJERNBERG","ERIK DANIEL","NÄS","alnö",24,6,1871);
        LXP birth2 = makeBirth("ERIK","BOMAN","MAJA","JOHANSDR","","BOMAN","",26,12,1848);
        LXP death = makeDeath("LOVISA","HÄGG","MARIANA LOVISA","JOHANSDR.","JOHAN","HÄGG");

        printExample(birth_birth_measure, birth1, birth2);
        printExample(birth_death_measure, birth1, death);
    }
}
