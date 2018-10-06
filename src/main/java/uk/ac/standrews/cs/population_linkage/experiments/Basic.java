package uk.ac.standrews.cs.population_linkage.experiments;

import uk.ac.standrews.cs.population_linkage.actions.RecordsImporter;
import uk.ac.standrews.cs.population_linkage.actions.RecordsPrinter;

public class Basic {

    public static void main(String[] args) throws Exception {

        new RecordsImporter().run();
        new RecordsPrinter().run();
    }
}
