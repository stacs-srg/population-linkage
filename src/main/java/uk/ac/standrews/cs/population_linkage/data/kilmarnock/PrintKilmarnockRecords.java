/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
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
