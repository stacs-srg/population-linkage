/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.population_records.PopulationDataSet;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.util.*;

public class UmeaRecordsAnalysis {

    public void run() throws Exception {

        final PopulationDataSet births = new UmeaBirthsDataSet();

        System.out.println("Number of birth records: " + births.getRecords().size());
        System.out.println("Number of unique child IDs: " + countUnique(births, Birth.CHILD_IDENTITY));
        System.out.println("Number of unique mother IDs: " + countUnique(births, Birth.MOTHER_IDENTITY));
        System.out.println("Number of unique father IDs: " + countUnique(births, Birth.FATHER_IDENTITY));

        final Set<String> unique_person_ids_in_births = new HashSet<>();

        addIds(unique_person_ids_in_births, births, Birth.CHILD_IDENTITY);
        addIds(unique_person_ids_in_births, births, Birth.MOTHER_IDENTITY);
        addIds(unique_person_ids_in_births, births, Birth.FATHER_IDENTITY);

        System.out.println("Number of unique person IDs: " + unique_person_ids_in_births.size());

        final int number_of_missing_birth_names = countMissingNames(
                births,
                Arrays.asList(Birth.FORENAME, Birth.MOTHER_FORENAME, Birth.FATHER_FORENAME),
                Arrays.asList(Birth.SURNAME, Birth.MOTHER_SURNAME, Birth.FATHER_SURNAME));

        final int number_of_inferred_birth_surnames = countInferredSurnames(births);
        final int number_of_remaining_missing_birth_surnames = countMissingNames(births, "SURNAME");

        System.out.println("Number of missing names in birth records: " + number_of_missing_birth_names);
        System.out.println("Number of inferred surnames in birth records: " + number_of_inferred_birth_surnames);
        System.out.println("Number of remaining missing surnames in birth records: " + number_of_remaining_missing_birth_surnames);
        System.out.format("Proportion of baby names on birth records missing surnames: %d%%", proportionAsPercentage(number_of_remaining_missing_birth_surnames + number_of_inferred_birth_surnames, births.getRecords().size()));
        System.out.println();

        //////////////////////////

        final List<String> non_id_mother_names = getNamesWithoutId(births, Birth.MOTHER_IDENTITY, Birth.MOTHER_FORENAME, Birth.MOTHER_MAIDEN_SURNAME, "mothers");
        final List<String> non_id_father_names = getNamesWithoutId(births, Birth.FATHER_IDENTITY, Birth.FATHER_FORENAME, Birth.FATHER_SURNAME, "fathers");

        //////////////////////////

        final DataSet marriages = new UmeaMarriagesDataSet();

        System.out.println();
        System.out.println("Number of marriage records: " + marriages.getRecords().size());
        System.out.println("Number of unique bride IDs: " + countUnique(marriages, Marriage.BRIDE_IDENTITY));
        System.out.println("Number of unique groom IDs: " + countUnique(marriages, Marriage.GROOM_IDENTITY));
        System.out.println("Number of unique bride mother IDs: " + countUnique(marriages, Marriage.BRIDE_MOTHER_IDENTITY));
        System.out.println("Number of unique groom mother IDs: " + countUnique(marriages, Marriage.GROOM_MOTHER_IDENTITY));
        System.out.println("Number of unique bride father IDs: " + countUnique(marriages, Marriage.BRIDE_FATHER_IDENTITY));
        System.out.println("Number of unique groom father IDs: " + countUnique(marriages, Marriage.GROOM_FATHER_IDENTITY));

        final Set<String> unique_person_ids_in_marriages = new HashSet<>();

        addIds(unique_person_ids_in_marriages, marriages, Marriage.BRIDE_IDENTITY);
        addIds(unique_person_ids_in_marriages, marriages, Marriage.GROOM_IDENTITY);
        addIds(unique_person_ids_in_marriages, marriages, Marriage.BRIDE_MOTHER_IDENTITY);
        addIds(unique_person_ids_in_marriages, marriages, Marriage.GROOM_MOTHER_IDENTITY);
        addIds(unique_person_ids_in_marriages, marriages, Marriage.BRIDE_FATHER_IDENTITY);
        addIds(unique_person_ids_in_marriages, marriages, Marriage.GROOM_FATHER_IDENTITY);

        System.out.println("Number of unique person IDs: " + unique_person_ids_in_marriages.size());

        final int missing_marriage_names = countMissingNames(
                marriages,
                Arrays.asList(Marriage.BRIDE_FORENAME, Marriage.GROOM_FORENAME, Marriage.BRIDE_MOTHER_FORENAME, Marriage.BRIDE_FATHER_FORENAME, Marriage.GROOM_MOTHER_FORENAME, Marriage.GROOM_FATHER_FORENAME),
                Arrays.asList(Marriage.BRIDE_SURNAME, Marriage.GROOM_SURNAME, Marriage.BRIDE_MOTHER_MAIDEN_SURNAME, Marriage.BRIDE_FATHER_SURNAME, Marriage.GROOM_MOTHER_MAIDEN_SURNAME, Marriage.GROOM_FATHER_SURNAME));

        System.out.println("Number of missing names in marriage records: " + missing_marriage_names);

        //////////////////////////

        final List<String> non_id_bride_mother_names = getNamesWithoutId(marriages, Marriage.BRIDE_MOTHER_IDENTITY, Marriage.BRIDE_MOTHER_FORENAME, Marriage.BRIDE_MOTHER_MAIDEN_SURNAME, "bride mothers");
        final List<String> non_id_groom_mother_names = getNamesWithoutId(marriages, Marriage.GROOM_MOTHER_IDENTITY, Marriage.GROOM_MOTHER_FORENAME, Marriage.GROOM_MOTHER_MAIDEN_SURNAME, "groom mothers");
        final List<String> non_id_bride_father_names = getNamesWithoutId(marriages, Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_FATHER_FORENAME, Marriage.BRIDE_FATHER_SURNAME, "bride fathers");
        final List<String> non_id_groom_father_names = getNamesWithoutId(marriages, Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_FATHER_FORENAME, Marriage.GROOM_FATHER_SURNAME, "groom fathers");

        //////////////////////////

        final PopulationDataSet deaths = new UmeaDeathsDataSet();

        System.out.println();
        System.out.println("Number of death records: " + deaths.getRecords().size());
        System.out.println("Number of unique deceased IDs: " + countUnique(deaths, Death.DECEASED_IDENTITY));
        System.out.println("Number of unique spouse IDs: " + countUnique(deaths, Death.SPOUSE_IDENTITY));
        System.out.println("Number of unique deceased mother IDs: " + countUnique(deaths, Death.MOTHER_IDENTITY));
        System.out.println("Number of unique deceased father IDs: " + countUnique(deaths, Death.FATHER_IDENTITY));

        final Set<String> unique_person_ids_in_deaths = new HashSet<>();

        addIds(unique_person_ids_in_deaths, deaths, Death.DECEASED_IDENTITY);
        addIds(unique_person_ids_in_deaths, deaths, Death.SPOUSE_IDENTITY);
        addIds(unique_person_ids_in_deaths, deaths, Death.MOTHER_IDENTITY);
        addIds(unique_person_ids_in_deaths, deaths, Death.FATHER_IDENTITY);

        System.out.println("Number of unique person IDs: " + unique_person_ids_in_deaths.size());

        final int missing_death_names = countMissingNames(
                deaths,
                Arrays.asList(Death.FORENAME, Death.SPOUSE_NAMES, Death.MOTHER_FORENAME, Death.FATHER_FORENAME),
                Arrays.asList(Death.SURNAME, Death.SPOUSE_NAMES, Death.MOTHER_MAIDEN_SURNAME, Death.FATHER_SURNAME));

        final int number_of_inferred_death_surnames = countInferredSurnames(deaths);
        final int number_of_remaining_missing_death_surnames = countMissingNames(deaths, "SURNAME");

        System.out.println("Number of missing names in death records: " + missing_death_names);
        System.out.println("Number of inferred surnames in death records: " + number_of_inferred_death_surnames);
        System.out.println("Number of remaining missing surnames in death records: " + number_of_remaining_missing_death_surnames);
        System.out.format("Proportion of deceased names on death records missing surnames: %d%%", proportionAsPercentage(number_of_remaining_missing_death_surnames + number_of_inferred_death_surnames, deaths.getRecords().size()));
        System.out.println();

        //////////////////////////

        final List<String> non_id_deceased_spouse_names = getNamesWithoutId(deaths, Death.SPOUSE_IDENTITY, Death.SPOUSE_NAMES, Death.SPOUSE_NAMES, "deceased spouses");
        final List<String> non_id_deceased_mother_names = getNamesWithoutId(deaths, Death.MOTHER_IDENTITY, Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME, "deceased mothers");
        final List<String> non_id_deceased_father_names = getNamesWithoutId(deaths, Death.FATHER_IDENTITY, Death.FATHER_FORENAME, Death.FATHER_SURNAME, "deceased fathers");

        //////////////////////////

        final Set<String> unique_person_ids = union(unique_person_ids_in_births, unique_person_ids_in_marriages, unique_person_ids_in_deaths);

        System.out.println();
        System.out.println("Number of overall unique person IDs: " + unique_person_ids.size());

        //////////////////////////

        final List<String> non_id_names = append(non_id_mother_names,non_id_father_names,non_id_bride_mother_names,non_id_groom_mother_names,non_id_bride_father_names,non_id_groom_father_names,non_id_deceased_spouse_names,non_id_deceased_mother_names,non_id_deceased_father_names);

        System.out.println();
        System.out.println("Number of overall missing names: " + (number_of_missing_birth_names + missing_marriage_names + missing_death_names));
        System.out.println("Number of overall people without IDs: " + non_id_names.size());
        System.out.println("Number of overall unique names without IDs: " + new HashSet<>(non_id_names).size());
    }

    private int proportionAsPercentage(final int i, final int j) {

        return Math.round(100 * (float)i / (float)j);
    }

    private int countInferredSurnames(final PopulationDataSet births) throws IOException {

        return countMissingNames(births.getSourceDataSet(), "SURNAME") - countMissingNames(births, "SURNAME");
    }

    private int countMissingNames(final DataSet dataset, final List<Integer> forename_indices, final List<Integer> surname_indices) {

        int count = 0;

        for (List<String> row : dataset.getRecords()) {
            count += countMissingNames(row, forename_indices, surname_indices);
        }

        return count;
    }

    private int countMissingNames(final List<String> row, final List<Integer> forename_indices, final List<Integer> surname_indices) {

        int count = 0;

        for (int i = 0; i < forename_indices.size(); i++) {

            if (empty(row.get(forename_indices.get(i))) && empty(row.get(surname_indices.get(i)))) {
                count++;
            }
        }

        return count;
    }

    private int countMissingNames(final DataSet dataset, final String field_name) {

        int count = 0;

        for (List<String> row : dataset.getRecords()) {
            if (empty(dataset.getValue(row, field_name))) {
                count++;
            }
        }

        return count;
    }

    private boolean empty(final String s) {
        return s == null || s.isEmpty();
    }

    @SafeVarargs
    private final Set<String> union(final Set<String>... sets) {

        final Set<String> result = new HashSet<>();

        for (Set<String> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    @SafeVarargs
    private final List<String> append(final List<String>... lists) {

        final List<String> result = new ArrayList<>();

        for (List<String> set : lists) {
            result.addAll(set);
        }
        return result;
    }

    private List<String> getNamesWithoutId(final DataSet data_set, final int identity_index, final int forename_index, final int surname_index, final String field_description) {

        final List<String> non_id_names = new ArrayList<>();

        for (List<String> row : data_set.getRecords()) {

            if (row.get(identity_index).isEmpty()) {
                final String name = row.get(forename_index) + " " + row.get(surname_index);
                if (name.length() > 1) {
                    non_id_names.add(name);
                }
            }
        }

        System.out.println();
        System.out.println("Number of " + field_description + " without IDs: " + non_id_names.size());
        System.out.println("Number of unique names of " + field_description + " without IDs: " + new HashSet<>(non_id_names).size());

        return non_id_names;
    }

    private int countUnique(final DataSet data_set, final int field_index) {

        Set<String> field_values = new HashSet<>();

        addIds(field_values, data_set, field_index);

        return field_values.size();
    }

    private void addIds(final Set<String> unique_person_ids, final DataSet data_set, final int field_index) {

        for (List<String> row : data_set.getRecords()) {

            String field_value = row.get(field_index);
            if (field_value.length() > 0) {
                unique_person_ids.add(field_value);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        new UmeaRecordsAnalysis().run();
    }
}
