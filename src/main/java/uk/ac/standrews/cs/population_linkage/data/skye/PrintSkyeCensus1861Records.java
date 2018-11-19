package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeCensus1861DataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintSkyeCensus1861Records {

    public void run() throws Exception {

        DataSet census_records = new SkyeCensus1861DataSet();
        census_records.print(System.out);
        System.out.println("Printed " + census_records.getRecords().size() + " census records");
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeCensus1861Records().run();
    }
}
