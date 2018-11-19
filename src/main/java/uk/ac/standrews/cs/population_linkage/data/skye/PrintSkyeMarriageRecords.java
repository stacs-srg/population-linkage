package uk.ac.standrews.cs.population_linkage.data.skye;

import uk.ac.standrews.cs.data.skye.SkyeMarriagesDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintSkyeMarriageRecords {

    public void run() throws Exception {

        DataSet marriage_records = new SkyeMarriagesDataSet();
        marriage_records.print(System.out);
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintSkyeMarriageRecords().run();
    }
}
