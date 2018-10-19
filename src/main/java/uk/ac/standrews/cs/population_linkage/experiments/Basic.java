package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.data.ImportKilmarnockRecordsToStore;
import uk.ac.standrews.cs.population_linkage.data.PrintKilmarnockRecordsFromStoreSample;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;

import java.nio.file.Path;

public class Basic {

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();
        String repository_name = ApplicationProperties.getRepositoryName();

        new ImportKilmarnockRecordsToStore(store_path, repository_name).run();
        new PrintKilmarnockRecordsFromStoreSample(store_path, repository_name).run();
    }
}
