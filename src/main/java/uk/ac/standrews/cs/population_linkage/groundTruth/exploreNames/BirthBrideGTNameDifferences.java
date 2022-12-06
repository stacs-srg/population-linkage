/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.exploreNames;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.Pair;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

public class BirthBrideGTNameDifferences {

    protected final PrintWriter writer;
    protected StringMeasure jensenShannon = Constants.JENSEN_SHANNON;
    public static final String OUTPUT_FILE_PATH = "/Users/al/Desktop/bride_babies_gt_names.csv";

    public BirthBrideGTNameDifferences(PrintWriter writer) {
        this.writer = writer;
    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (b:Birth)-[r:GT_ID { actors: \"Child-Bride\" }]-(m:Marriage) RETURN b,m";

    public static List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge) {

        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> new Pair<>(r.get("b").asNode(), r.get("m").asNode()));
    }

    private void show(NeoDbCypherBridge bridge) {
        List<Pair<Node, Node>> nodes = getPairs(bridge);
        showNodes(nodes);
    }

    private void showNodes(List<Pair<Node, Node>> nodes) {

        for (Pair<Node, Node> p : nodes) {
            Node birth = p.X();
            Node marriage = p.Y();
            String baby_firstname = birth.get("FORENAME").toString(); // this is in Node space from Cypher not LXP!
            String baby_surname = birth.get("SURNAME").toString();
            String baby_id = birth.get("CHILD_IDENTITY").toString();
            String bride_firstname = marriage.get("BRIDE_FORENAME").toString();
            String bride_surname = marriage.get("BRIDE_SURNAME").toString();
            String bride_id = marriage.get("BRIDE_IDENTITY").toString();
            int code = 0;
            if (baby_firstname.equals(bride_firstname) && baby_surname.equals(bride_surname)) {
            } else if (!baby_firstname.equals(bride_firstname) && !baby_surname.equals(bride_surname)) {
                code = 3;
            } else if (!baby_firstname.equals(bride_firstname)) {
                code = 1;
            } else if (!baby_surname.equals(bride_surname)) {
                code = 2;
            }
            showPair(code, baby_firstname, baby_surname, baby_id, bride_firstname, bride_surname, bride_id);
        }
    }

    protected void header() {
        StringBuilder line = new StringBuilder();
        line.append("code");
        line.append(",");
        line.append("baby_firstname");
        line.append(",");
        line.append("bride_firstname");
        line.append(",");
        line.append("baby_surname");
        line.append(",");
        line.append("bride_surname");
        line.append(",");
        line.append("JS first name");
        line.append(",");
        line.append("JS surname");
        line.append(",");
        line.append("mean JS dist");
        writer.println(line);
    }

    protected void showPair(int code, String baby_firstname, String baby_surname, String baby_id, String bride_firstname, String bride_surname, String bride_id) {

        String qs_baby_firstname = quoteStrip(baby_firstname);
        String qs_baby_surname = quoteStrip(baby_surname);
        String qs_bride_firstname = quoteStrip(bride_firstname);
        String qs_bride_surname = quoteStrip(bride_surname);

        if (!baby_id.equals(bride_id)) {
            System.out.println("GT MATCH ERROR");
            System.exit(-1);
        }

        double fnd = jensenShannon.distance(qs_baby_firstname, qs_bride_firstname);
        double snd = jensenShannon.distance(qs_baby_surname, qs_bride_surname);

        printPair(code, qs_baby_firstname, qs_baby_surname, qs_bride_firstname, qs_bride_surname, fnd, snd);
    }

    protected void printPair(int code, String qs_baby_firstname, String qs_baby_surname, String qs_bride_firstname, String qs_bride_surname, double fnd, double snd) {
        StringBuilder line = new StringBuilder();
        line.append(code);
        line.append(",");
        line.append(qs_baby_firstname);
        line.append(",");
        line.append(qs_bride_firstname);
        line.append(",");
        line.append(qs_baby_surname);
        line.append(",");
        line.append(qs_bride_surname);
        line.append(",");
        line.append(fnd);
        line.append(",");
        line.append(snd);
        line.append(",");
        line.append((fnd + snd) / 2);

        writer.println(line);
    }

    private String quoteStrip(String name) {
        if( name.contains("\"") ) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    public static void main(String[] args) throws BucketException {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH)))) {

            BirthBrideGTNameDifferences en = new BirthBrideGTNameDifferences(writer);
            en.header();
            en.show(bridge);

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }
}
