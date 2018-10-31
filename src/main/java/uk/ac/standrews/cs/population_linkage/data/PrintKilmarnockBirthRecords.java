package uk.ac.standrews.cs.population_linkage.data;


import uk.ac.standrews.cs.data.kilmarnock.KilmarnockBirthsDataSet;

public class PrintKilmarnockBirthRecords {

    public void run() throws Exception {

        KilmarnockBirthsDataSet birth_records = new KilmarnockBirthsDataSet();
        birth_records.print(System.out);
        System.out.println("Printed " + birth_records.getRecords().size() + " birth records");
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockBirthRecords().run();
    }
}
