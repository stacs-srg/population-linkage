package uk.ac.standrews.cs.population_linkage.data.umea;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaDeathsDataSet;
import uk.ac.standrews.cs.data.umea.UmeaMarriagesDataSet;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UmeaRecordsAnalysis {

    public void run() throws Exception {

        final DataSet births = new UmeaBirthsDataSet();

        System.out.println("Number of birth records: " + births.getRecords().size());
        System.out.println("Number of unique child IDs: " + countUnique(births, Birth.CHILD_IDENTITY));
        System.out.println("Number of unique mother IDs: " + countUnique(births, Birth.MOTHER_IDENTITY));
        System.out.println("Number of unique father IDs: " + countUnique(births, Birth.FATHER_IDENTITY));

        final Set<String> unique_person_ids_in_births = new HashSet<>();

        addIds(unique_person_ids_in_births, births, Birth.CHILD_IDENTITY);
        addIds(unique_person_ids_in_births, births, Birth.MOTHER_IDENTITY);
        addIds(unique_person_ids_in_births, births, Birth.FATHER_IDENTITY);

        System.out.println("Number of unique person IDs: " + unique_person_ids_in_births.size());

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

        //////////////////////////

        final List<String> non_id_bride_mother_names = getNamesWithoutId(marriages, Marriage.BRIDE_MOTHER_IDENTITY, Marriage.BRIDE_MOTHER_FORENAME, Marriage.BRIDE_MOTHER_MAIDEN_SURNAME, "bride mothers");
        final List<String> non_id_groom_mother_names = getNamesWithoutId(marriages, Marriage.GROOM_MOTHER_IDENTITY, Marriage.GROOM_MOTHER_FORENAME, Marriage.GROOM_MOTHER_MAIDEN_SURNAME, "groom mothers");
        final List<String> non_id_bride_father_names = getNamesWithoutId(marriages, Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_FATHER_FORENAME, Marriage.BRIDE_FATHER_SURNAME, "bride fathers");
        final List<String> non_id_groom_father_names = getNamesWithoutId(marriages, Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_FATHER_FORENAME, Marriage.GROOM_FATHER_SURNAME, "groom fathers");

        //////////////////////////

        final DataSet deaths = new UmeaDeathsDataSet();

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
        System.out.println("Number of overall people without IDs: " + non_id_names.size());
        System.out.println("Number of overall unique names without IDs: " + new HashSet<>(non_id_names).size());
    }

    private Set<String> union(final Set<String>... sets) {

        final Set<String> result = new HashSet<>();

        for (Set<String> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    private List<String> append(final List<String>... lists) {

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
