package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.data.umea.UmeaBirthsDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.util.List;

public class UmeaExample {

    public static void main(String[] args) throws IOException {

        DataSet birth_records = new UmeaBirthsDataSet();

        for (List<String> record : birth_records.getRecords()) {

            String year = birth_records.getValue(record, "BIRTH_YEAR");
            System.out.println(year);
        }
    }
}
