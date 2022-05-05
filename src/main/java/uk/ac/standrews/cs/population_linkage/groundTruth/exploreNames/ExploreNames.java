/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.exploreNames;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.Pair;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.text.DecimalFormat;
import java.util.List;

public class ExploreNames {

    private StringMeasure measure = Constants.JENSEN_SHANNON;
    private DecimalFormat df = new DecimalFormat("0.00");

    public ExploreNames() {
    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (b:Birth)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]-(m:Marriage) RETURN b,m";

    public static List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge) {

        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> new Pair<>(r.get("b").asNode(), r.get("m").asNode()));
    }

    private void explore(NeoDbCypherBridge bridge) {
        List<Pair<Node, Node>> nodes = getPairs(bridge);
        examineNodes(nodes);
    }

    private void examineNodes(List<Pair<Node, Node>> nodes) {
        int different_first_names = 0;
        int different_surnames = 0;
        int both_different = 0;
        int same = 0;
        for (Pair<Node, Node> p : nodes) {
            Node birth = p.X();
            Node marriage = p.Y();
            String baby_firstname = birth.get("FORENAME").toString(); // this is in Node space from Cypher not LXP!
            String baby_surname = birth.get("SURNAME").toString();
            String baby_id = birth.get("CHILD_IDENTITY").toString();
            String bride_firstname = marriage.get("BRIDE_FORENAME").toString();
            String bride_surname = marriage.get("BRIDE_SURNAME").toString();
            String bride_id = marriage.get("BRIDE_IDENTITY").toString();
            if (baby_firstname.equals(bride_firstname) && baby_surname.equals(bride_surname)) {
                same++;
            } else if (!baby_firstname.equals(bride_firstname) && !baby_surname.equals(bride_surname)) {
                showPair(both_different, "both different", baby_firstname, baby_surname, baby_id, bride_firstname, bride_surname, bride_id);
                both_different++;
            } else if (!baby_firstname.equals(bride_firstname)) {
                showPair(different_first_names, "different firstname", baby_firstname, baby_surname, baby_id, bride_firstname, bride_surname, bride_id);
                different_first_names++;
            } else if (!baby_surname.equals(bride_surname)) {
                showPair(different_surnames, "different surname", baby_firstname, baby_surname, baby_id, bride_firstname, bride_surname, bride_id);
                different_surnames++;
            }
        }
        System.out.println("Number of pairs examined: " + nodes.size());
        System.out.println("Number of identical names baby-bride: " + same);
        System.out.println("Number of different first names baby-bride: " + different_first_names);
        System.out.println("Number of different surnames baby-bride: " + different_surnames);
        System.out.println("Number of names both different baby-bride: " + both_different);
    }

    private void showPair(int counter, String label, String baby_firstname, String baby_surname, String baby_id, String bride_firstname, String bride_surname, String bride_id) {
        if (!baby_id.equals(bride_id)) {
            System.out.println("GT MATCH ERROR");
            System.exit(-1);
        }
        if (counter < 10) {
            System.out.println("*** " + label + ":");
            System.out.println("baby:  " + baby_firstname + "/" + baby_surname);
            System.out.println("bride: " + bride_firstname + "/" + bride_surname);
            double fnd = measure.distance(baby_firstname, bride_firstname);
            double snd = measure.distance(baby_surname, bride_surname);
            System.out.print("Firstname distance = " + df.format(fnd));
            System.out.print(" Surname distance = " + df.format(snd));
            System.out.println(" Combined distance = " + df.format((fnd + snd) / 2));
        }
    }

    public static void main(String[] args) throws BucketException {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            ExploreNames en = new ExploreNames();
            en.explore(bridge);

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
