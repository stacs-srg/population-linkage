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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

public class BirthBrideGTAllNameDifferencesWithCleaning extends BirthBrideGTNameDifferences {

    private int identical_count = 0;
    private int different_count = 0;

    private StringMeasure lev = Constants.LEVENSHTEIN;
    private StringMeasure jacc = Constants.JACCARD;
    private StringMeasure cos = Constants.COSINE;

    public BirthBrideGTAllNameDifferencesWithCleaning(PrintWriter writer) {
        super(writer);
    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (b:Birth)-[r:GT_ID { actors: \"Child-Bride\" } ]-(m:Marriage) RETURN b,m";

    public static List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge) {

        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> new Pair<>(r.get("b").asNode(), r.get("m").asNode()));
    }

    private void show(NeoDbCypherBridge bridge) {
        List<Pair<Node, Node>> nodes = getPairs(bridge);
        examineNodes(nodes);
        System.out.println( "Different = " + different_count );
        System.out.println( "Identical = " + identical_count );
    }

    private void examineNodes(List<Pair<Node, Node>> nodes) {

        for (Pair<Node, Node> p : nodes) {
            Node birth = p.X();
            Node marriage = p.Y();

            String baby_firstname = quoteStrip(birth.get("FORENAME").toString()); // this is in Node space from Cypher not LXP!
            String baby_surname = quoteStrip(birth.get("SURNAME").toString());
            String baby_id = quoteStrip(birth.get("CHILD_IDENTITY").toString());
            String baby_mother_firstname = quoteStrip(birth.get("MOTHER_FORENAME").toString());
            String baby_mother_surname = quoteStrip(birth.get("MOTHER_MAIDEN_SURNAME").toString());
            String baby_father_firstname = quoteStrip(birth.get("FATHER_FORENAME").toString());
            String baby_father_surname = quoteStrip(birth.get("FATHER_SURNAME").toString());

            String bride_firstname = quoteStrip(marriage.get("BRIDE_FORENAME").toString());
            String bride_surname = quoteStrip(marriage.get("BRIDE_SURNAME").toString());
            String bride_id = quoteStrip(marriage.get("BRIDE_IDENTITY").toString());
            String bride_mother_firstname = quoteStrip(marriage.get("BRIDE_MOTHER_FORENAME").toString());
            String bride_mother_surname = quoteStrip(marriage.get("BRIDE_MOTHER_MAIDEN_SURNAME").toString());
            String bride_father_firstname = quoteStrip(marriage.get("BRIDE_FATHER_FORENAME").toString());
            String bride_father_surname = quoteStrip(marriage.get("BRIDE_FATHER_SURNAME").toString());

            examineNames(baby_firstname, baby_surname, baby_id, baby_mother_firstname, baby_mother_surname, baby_father_firstname, baby_father_surname,
                    bride_firstname, bride_surname, bride_id, bride_mother_firstname, bride_mother_surname, bride_father_firstname, bride_father_surname);
        }
    }

    private void examineNames(String baby_firstname, String baby_surname, String baby_id, String baby_mother_firstname, String baby_mother_surname, String baby_father_firstname, String baby_father_surname,
                              String bride_firstname, String bride_surname, String bride_id, String bride_mother_firstname, String bride_mother_surname, String bride_father_firstname, String bride_father_surname) {

        examineNames( baby_firstname,baby_surname,baby_id,bride_firstname,bride_surname,bride_id );
    }

    private void examineNames(String baby_firstname, String baby_surname, String baby_id, String bride_firstname, String bride_surname, String bride_id ) {
        if (!baby_id.equals(bride_id)) {
            System.out.println("GT MATCH ERROR");
            System.exit(-1);
        }

        bride_firstname = cleanWholeFirstname( bride_firstname );
        baby_firstname = cleanWholeFirstname( baby_firstname );
        baby_surname = cleanWholeSurname( baby_surname );
        bride_surname = cleanWholeSurname( bride_surname );

        if( baby_surname.equals( bride_surname ) && baby_firstname.equals( bride_firstname ) ) {
            identical_count = identical_count + 1;
        } else {
            int code = 0;
            if (baby_firstname.equals(bride_firstname) && baby_surname.equals(bride_surname)) {
            } else if (!baby_firstname.equals(bride_firstname) && !baby_surname.equals(bride_surname)) {
                code = 3;
            } else if (!baby_firstname.equals(bride_firstname)) {
                code = 1;
            } else if (!baby_surname.equals(bride_surname)) {
                code = 2;
            }
            showPair( code, baby_firstname, baby_surname, baby_id, bride_firstname, bride_surname, bride_id);
            different_count = different_count + 1;
        }
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

        double js1 = jensenShannon.distance(qs_baby_firstname, qs_bride_firstname);
        double js2 = jensenShannon.distance(qs_baby_surname, qs_bride_surname);

        double lev1 = lev.distance(qs_baby_firstname, qs_bride_firstname);
        double lev2 = lev.distance(qs_baby_surname, qs_bride_surname);

        double jacc1 = jacc.distance(qs_baby_firstname, qs_bride_firstname);
        double jacc2 = jacc.distance(qs_baby_surname, qs_bride_surname);

        double cos1 = cos.distance(qs_baby_firstname, qs_bride_firstname);
        double cos2 = cos.distance(qs_baby_surname, qs_bride_surname);

        printPair(code, qs_baby_firstname, qs_baby_surname, qs_bride_firstname, qs_bride_surname, js1, js2, lev1, lev2, jacc1, jacc2, cos1, cos2);
    }

    protected void printPair(int code, String qs_baby_firstname, String qs_baby_surname, String qs_bride_firstname, String qs_bride_surname, double js1, double js2, double lev1, double lev2, double jacc1, double jacc2, double cos1, double cos2) {
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
        line.append(js1);
        line.append(",");
        line.append(js2);
        line.append(",");
        line.append(lev1);
        line.append(",");
        line.append(lev2);
        line.append(",");
        line.append(jacc1);
        line.append(",");
        line.append(jacc2);
        line.append(",");
        line.append(cos1);
        line.append(",");
        line.append(cos2);

        writer.println(line);
    }


    private String cleanWholeFirstname(String first_name) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(first_name," ");
        while( st.hasMoreTokens() ) {
            String lexeme = st.nextToken();
            sb.append( cleanIndividualFirstname(lexeme) );
            sb.append( " " );
        }
        if( sb.length() > 1 ) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String cleanIndividualFirstname(String first_name) {
        first_name = eliminateBrackets( first_name );

        // Different order from surname due to names like CATHAR.

        first_name = replace( first_name, "THARINA", "THAR." );
        first_name = replace( first_name, "ARLOTTA", "ARL." );
        first_name = replace( first_name, "LOVISA", "LOV." );
        first_name = replace( first_name, "DLENA", "DAL." );
        first_name = replace( first_name, "ISTINA", "IST." );
        first_name = replace( first_name, "OHANNA", "OH." );
        first_name = replace( first_name, "OLINA", "OL." );
        first_name = replace( first_name, "RGRETA", "RG." );
        first_name = replace( first_name, "GRETTA", "GR." );
        first_name = replace( first_name, "DRIKA", "DRIK." );
        first_name = replace( first_name, "DRIKA", "DR." );
        first_name = replace( first_name, "RITTA", "R." );
        first_name = replace( first_name, "ATHARINA", "ATH." );
        first_name = replace( first_name, "ABET", "AB." );
        first_name = replace( first_name, "HELENA", "HEL." );
        first_name = replace( first_name, "ROLINA", "ROL." );
        first_name = replace( first_name, "CHRISTINA", "CHR." );

        if( first_name.endsWith(":") ) { // some do!
            first_name = first_name.substring(0,first_name.length()-1); // eliminate trailing :
        }
        if( first_name.endsWith(".") ) {
            first_name = first_name.substring(0,first_name.length()-1); // eliminate trailing .
        }
        first_name.replace(" ",""); // eliminate any spaces in the name

        return first_name;
    }


    private String cleanWholeSurname(String surname) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(surname," ");
        while( st.hasMoreTokens() ) {
            String lexeme = st.nextToken();
            sb.append( cleanIndividualSurname(lexeme) );
            sb.append( " " );
        }
        if( sb.length() > 1 ) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String cleanIndividualSurname(String surname) {
        surname = eliminateBrackets( surname );
        if( surname.endsWith(".") ) {
            surname = surname.substring(0,surname.length()-1); // eliminate trailing .
        }
        if( surname.endsWith(":") ) { // some do!
            surname = surname.substring(0,surname.length()-1); // eliminate trailing :
        }
        surname.replaceAll(" ",""); // eliminate any spaces in the name

        surname = replace( surname, "DERSDOTTER", "D.SDR", "D:SDR", "D:SD:R" );
        surname = replace( surname,"DERSDOTTER","D.D.R", "D.SDOTTER" );
        surname = replace( surname, "SDOTTER", ".SDR", ":S:DR", "S:DR", "SDR", "SD", "S:DÅT", "S.DOTTER" );
        surname = replace( surname,"DOTTER","DR", "D.R", "D:R", "DOTT", "DOTR", ":DR", "DOT", ":DÅT", ".DÅT", "DÅT", "DTR", "D.TR", "DOTT.R" );

        return surname;
    }

    private String eliminateBrackets(String surname) {
        if( surname.contains( "(" ) ) {
            surname = surname.replace("(","");
        }
        if( surname.contains( ")" ) ) {
            surname = surname.replace(")","");
        }
        if( surname.contains( "[" ) ) {
            surname = surname.replace("[","");
        }
        if( surname.contains( "]" ) ) {
            surname = surname.replace("]","");
        }
        return surname;
    }

    private String replace(String surname, String with, String... endings ) {
        for( String ending : endings ) {
            surname = replace(surname, with, ending);
        }
        return surname;
    }

    private String replace(String surname, String with, String ending) {
        if( surname.endsWith(ending) ) {
            surname = surname.substring(0, surname.length()- ending.length() );
            surname = surname + with;
        }
        return surname;
    }

    private String quoteStrip(String name) {
        if( name.contains("\"") ) {
            return name.substring(1, name.length() - 1);
        }
        return name;
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
        line.append("Lev first name");
        line.append(",");
        line.append("Lev surname");
        line.append(",");
        line.append("Jacc first name");
        line.append(",");
        line.append("Jacc surname");
        line.append(",");
        line.append("Cos first name");
        line.append(",");
        line.append("Cos surname");


        writer.println(line);
    }

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ; PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH) ) ) ) {

            BirthBrideGTAllNameDifferencesWithCleaning en = new BirthBrideGTAllNameDifferencesWithCleaning(writer);
            en.header();
            en.show(bridge);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
