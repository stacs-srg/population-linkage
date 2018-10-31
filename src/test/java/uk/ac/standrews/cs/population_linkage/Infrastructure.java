package uk.ac.standrews.cs.population_linkage;

import org.junit.Test;
import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;
import uk.ac.standrews.cs.data.kilmarnock.importer.KilmarnockDataSetImporter;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.importer.DataSetImporter;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.nio.file.Files;
import java.nio.file.Path;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class Infrastructure {

    @Test
    public void loadAndRead() throws Exception {

        Path store_path = Files.createTempDirectory("");
        String repo_name = "kilmarnock_repository";

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        DataSetImporter importer = new KilmarnockDataSetImporter();

        BirthsDataSet birth_records = new BirthsDataSet();
        DeathsDataSet death_records = new DeathsDataSet();
        MarriagesDataSet marriage_records = new MarriagesDataSet();

        importer.importBirthRecords(record_repository, birth_records);
        importer.importDeathRecords(record_repository, death_records);
        importer.importMarriageRecords(record_repository, marriage_records);

        int births_read = 0;
        for (Birth birth : record_repository.getBirths()) {
            assertNotNull(birth);
            births_read++;
        }

        int deaths_read = 0;
        for (Death death : record_repository.getDeaths()) {
            assertNotNull(death);
            deaths_read++;
        }

        int marriages_read = 0;
        for (Marriage marriage : record_repository.getMarriages()) {
            assertNotNull(marriage);
            marriages_read++;
        }

        assertEquals(birth_records.getRecords().size(), births_read);
        assertEquals(death_records.getRecords().size(), deaths_read);
        assertEquals(marriage_records.getRecords().size(), marriages_read);
    }
}
