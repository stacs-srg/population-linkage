package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.actions.RecordsImporter;
import uk.ac.standrews.cs.population_linkage.actions.RecordsPrinter;
import uk.ac.standrews.cs.utilities.crypto.CryptoException;

import java.nio.file.Files;
import java.nio.file.Path;

public class Basic {

    public static void main(String[] args) throws Exception, CryptoException {

        Path store_path = Files.createTempDirectory("");
        String repository_name = "kilmarnock_data";

        new RecordsImporter(store_path, repository_name).run();
        new RecordsPrinter(store_path, repository_name).run();
    }
}
