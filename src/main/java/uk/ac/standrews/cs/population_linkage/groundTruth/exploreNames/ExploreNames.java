package uk.ac.standrews.cs.population_linkage.groundTruth.exploreNames;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.Pair;

import java.util.List;

public class ExploreNames {


    public ExploreNames() {

    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (b:Birth)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]-(m:Marriage) RETURN b,m";


    public static List<Pair<Node,Node>> getPairs(NeoDbCypherBridge bridge) {

        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> new Pair<>(r.get("b").asNode(),r.get("m").asNode()));
    }

    private void explore(NeoDbCypherBridge bridge) {
        List<Pair<Node,Node>> nodes = getPairs(bridge);
        examineNodes( nodes );
    }

    private void examineNodes(List<Pair<Node, Node>> nodes) {
        int different_first_names = 0;
        int different_surnames = 0;
        int both_different = 0;
        int same = 0;
        for( Pair<Node,Node> p : nodes ) {
            Node birth = p.X();
            Node marriage = p.Y();
            String baby_firstname = birth.get("FORENAME").toString(); // this is in Node space from Cypher not LXP!
            String baby_surname = birth.get("SURNAME").toString();
            String bride_firstname = marriage.get("BRIDE_FORENAME").toString();
            String bride_surname = marriage.get("BRIDE_SURNAME").toString();
            if( baby_firstname.equals( bride_firstname ) && baby_surname.equals( bride_surname ) ) {
                // showPair(same,"both same", baby_firstname,baby_surname,bride_firstname,bride_surname);
                same++;
            } else if( ! baby_firstname.equals( bride_firstname )  && ! baby_surname.equals( bride_surname ) ) {
                showPair(both_different,"both different",baby_firstname,baby_surname,bride_firstname,bride_surname);
                both_different++;
            } else if( ! baby_firstname.equals( bride_firstname ) ) {
                showPair(different_first_names,"different firstname",baby_firstname,baby_surname,bride_firstname,bride_surname);
                different_first_names++;
            } else if( ! baby_surname.equals( bride_surname ) ) {
                showPair(different_surnames,"different surname", baby_firstname,baby_surname,bride_firstname,bride_surname);
                different_surnames++;
            }
        }
        System.out.println( "Number of pairs examined: " + nodes.size() );
        System.out.println( "Number of identical names baby-bride: " + same );
        System.out.println( "Number of different first names baby-bride: " + different_first_names );
        System.out.println( "Number of different surnames baby-bride: " + different_surnames );
        System.out.println( "Number of names both different baby-bride: " + both_different );
    }

    private void showPair(int counter, String label, String baby_firstname, String baby_surname, String bride_firstname, String bride_surname) {
        if( counter < 10 ) {
            System.out.println( label + ":" );
            System.out.println( baby_firstname + "/" + baby_surname);
            System.out.println( bride_firstname + "/" + bride_surname);
        }
    }


    public static void main(String[] args) throws BucketException {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {
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
