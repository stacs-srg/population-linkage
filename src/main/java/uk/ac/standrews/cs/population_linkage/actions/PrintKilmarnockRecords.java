package uk.ac.standrews.cs.population_linkage.actions;

import uk.ac.standrews.cs.data.kilmarnock.data.BirthsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.DeathsDataSet;
import uk.ac.standrews.cs.data.kilmarnock.data.MarriagesDataSet;

public class PrintKilmarnockRecords {

    public void run() throws Exception {

        BirthsDataSet birth_records = new BirthsDataSet();
        birth_records.print(System.out);
        System.out.println("Printed " + birth_records.getRecords().size() + " birth records");

        DeathsDataSet death_records = new DeathsDataSet();
        death_records.print(System.out);
        System.out.println("Printed " + death_records.getRecords().size() + " death records");

        MarriagesDataSet marriage_records = new MarriagesDataSet();
        marriage_records.print(System.out);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");
    }
}
