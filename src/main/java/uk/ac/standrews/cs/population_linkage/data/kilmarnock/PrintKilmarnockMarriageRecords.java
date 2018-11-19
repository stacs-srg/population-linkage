package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

import uk.ac.standrews.cs.data.kilmarnock.KilmarnockMarriagesDataSet;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

public class PrintKilmarnockMarriageRecords {

    public void run() throws Exception {

        DataSet marriage_records = new KilmarnockMarriagesDataSet();
        marriage_records.print(System.out);
        System.out.println("Printed " + marriage_records.getRecords().size() + " marriage records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockMarriageRecords().run();
    }
}
