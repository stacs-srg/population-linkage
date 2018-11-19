package uk.ac.standrews.cs.population_linkage.data.kilmarnock;

public class PrintKilmarnockRecords {

    public void run() throws Exception {

        new PrintKilmarnockBirthRecords().run();
        new PrintKilmarnockDeathRecords().run();
        new PrintKilmarnockMarriageRecords().run();
    }

    public static void main(String[] args) throws Exception {

        new PrintKilmarnockRecords().run();
    }
}
