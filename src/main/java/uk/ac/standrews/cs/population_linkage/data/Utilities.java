package uk.ac.standrews.cs.population_linkage.data;

import uk.ac.standrews.cs.population_linkage.linkage.BirthLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.DeathLinkageSubRecord;
import uk.ac.standrews.cs.population_linkage.linkage.MarriageLinkageSubRecord;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utilities {

    private final static int NUMBER_TO_PRINT = 5;

    public static void printSampleRecords(DataSet data_set, String record_type) {

        Utilities.printRow(data_set.getColumnLabels());
        List<List<String>> records = data_set.getRecords();

        for (int i = 0; i < NUMBER_TO_PRINT; i++) {
            Utilities.printRow(records.get(i));
        }

        System.out.println("Printed " + NUMBER_TO_PRINT + " of " + records.size() + " " + record_type + " records");
    }

    private static void printRow(List<String> row) {

        boolean first = true;
        for (String element : row) {
            if (!first) {
                System.out.print(",");
            }
            first = false;
            System.out.print(element);
        }
        System.out.println();
    }

    public static Collection<BirthLinkageSubRecord> getBirthLinkageSubRecords(RecordRepository record_repository) {

        Collection<BirthLinkageSubRecord> sub_records = new ArrayList<>();

        for (Birth birth : record_repository.getBirths()) {

            sub_records.add(new BirthLinkageSubRecord(birth));
        }

        return sub_records;
    }

    public static Collection<DeathLinkageSubRecord> getDeathLinkageSubRecords(RecordRepository record_repository) {

        Collection<DeathLinkageSubRecord> sub_records = new ArrayList<>();

        for (Death death : record_repository.getDeaths()) {

            sub_records.add(new DeathLinkageSubRecord(death));
        }

        return sub_records;
    }

    public static Collection<MarriageLinkageSubRecord> getMarriageLinkageSubRecords(RecordRepository record_repository) {

        Collection<MarriageLinkageSubRecord> sub_records = new ArrayList<>();

        for (Marriage marriage : record_repository.getMarriages()) {

            sub_records.add(new MarriageLinkageSubRecord(marriage));
        }

        return sub_records;
    }
}
