/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class Debug {

    public static void showLXP(LXP lxp) {
        System.out.println(lxp.getString(Birth.FORENAME) + " " + lxp.getString(Birth.SURNAME) + " // "
                + lxp.getString(Birth.FATHER_FORENAME) + " " + lxp.getString(Birth.FATHER_SURNAME) + " " + lxp.getString(Birth.FAMILY));
    }

    private static void showLink(Link calculated_link) {

        try {
            LXP person1 = calculated_link.getRecord1().getReferend();
            LXP person2 = calculated_link.getRecord2().getReferend();

            System.out.println("B1: " + person1.getString(Birth.FORENAME) + " " + person1.getString(Birth.SURNAME) + " // "
                    + "B1F: " + person1.getString(Birth.FATHER_FORENAME) + " " + person1.getString(Birth.FATHER_SURNAME) + " " + person1.getString(Birth.FAMILY) + " -> " +
                    "B2: " + person2.getString(Birth.FORENAME) + " " + person2.getString(Birth.SURNAME) + " // " +
                    "B2F: " + person2.getString(Birth.FATHER_FORENAME) + " " + person2.getString(Birth.FATHER_SURNAME) + " " + person2.getString(Birth.FAMILY));

        } catch (Exception e) {}
    }

    private static void printLink(Link link, String classification, LinkageRecipe linkageRecipe) {

        try {
            LXP person1 = link.getRecord1().getReferend();
            LXP person2 = link.getRecord2().getReferend();

            System.out.printf("-%s------------------------------------------------------------------------------------------------------------\n", classification);

            for(int i = 0 ; i < linkageRecipe.getLinkageFields().size(); i++) {
                String r1FieldName = Utilities.getLabels(person1).get(linkageRecipe.getLinkageFields().get(i));
                String r2FieldName = Utilities.getLabels(person2).get(linkageRecipe.getSearchMappingFields().get(i));

                String r1FieldContent = person1.getString(linkageRecipe.getLinkageFields().get(i));
                String r2FieldContent = person2.getString(linkageRecipe.getSearchMappingFields().get(i));

                String isEquals = "≠";
                if(r1FieldContent.equals(r2FieldContent)) isEquals = "=";

                System.out.printf("%30s | %20s |%s| %-20s | %-30s \n", r1FieldName, r1FieldContent, isEquals, r2FieldContent, r2FieldName);
            }

            System.out.println("---------------------------------------------------------------------------------------------------------------");

        } catch (Exception ignored) { }
    }
}
