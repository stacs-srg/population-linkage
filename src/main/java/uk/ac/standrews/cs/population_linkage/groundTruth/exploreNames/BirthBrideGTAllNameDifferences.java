package uk.ac.standrews.cs.population_linkage.groundTruth.exploreNames;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.Pair;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

public class BirthBrideGTAllNameDifferences {

    private final PrintWriter writer;
    private StringMetric metric = new JensenShannon(2048);
    public static final String OUTPUT_FILE_PATH = "/Users/al/Desktop/bride_babies_gt_all_names.csv";

    public BirthBrideGTAllNameDifferences(PrintWriter writer) {
        this.writer = writer;
    }

    private static final String BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY = "MATCH (b:Birth)-[r:GROUND_TRUTH_BIRTH_BRIDE_IDENTITY]-(m:Marriage) RETURN b,m";


    public static List<Pair<Node,Node>> getPairs(NeoDbCypherBridge bridge) {

        Result result = bridge.getNewSession().run(BIRTH_BRIDE_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> new Pair<>(r.get("b").asNode(),r.get("m").asNode()));
    }

    private void show(NeoDbCypherBridge bridge) {
        List<Pair<Node,Node>> nodes = getPairs(bridge);
        examineNodes( nodes );
    }

    private void examineNodes(List<Pair<Node, Node>> nodes) {

        for( Pair<Node,Node> p : nodes ) {
            Node birth = p.X();
            Node marriage = p.Y();

            String baby_firstname = quoteStrip( birth.get("FORENAME").toString()); // this is in Node space from Cypher not LXP!
            String baby_surname = quoteStrip(  birth.get("SURNAME").toString());
            String baby_id = quoteStrip(  birth.get("CHILD_IDENTITY").toString());
            String baby_mother_firstname = quoteStrip(  birth.get("MOTHER_FORENAME").toString());
            String baby_mother_surname = quoteStrip(  birth.get("MOTHER_MAIDEN_SURNAME").toString());
            String baby_father_firstname = quoteStrip(  birth.get("FATHER_FORENAME").toString());
            String baby_father_surname = quoteStrip(  birth.get("FATHER_SURNAME").toString());
            
            String bride_firstname = quoteStrip(  marriage.get("BRIDE_FORENAME").toString());
            String bride_surname = quoteStrip(  marriage.get("BRIDE_SURNAME").toString());
            String bride_id = quoteStrip(  marriage.get("BRIDE_IDENTITY").toString());
            String bride_mother_firstname = quoteStrip(  marriage.get("BRIDE_MOTHER_FORENAME").toString());
            String bride_mother_surname = quoteStrip(  marriage.get("BRIDE_MOTHER_MAIDEN_SURNAME").toString());
            String bride_father_firstname = quoteStrip(  marriage.get("BRIDE_FATHER_FORENAME").toString());
            String bride_father_surname = quoteStrip(  marriage.get("BRIDE_FATHER_SURNAME").toString());

            showNames(baby_firstname,baby_surname,baby_id,baby_mother_firstname,baby_mother_surname,baby_father_firstname,baby_father_surname,
                      bride_firstname,bride_surname,bride_id,bride_mother_firstname,bride_mother_surname,bride_father_firstname,bride_father_surname);
        }
    }

    private void header() {
        StringBuilder line = new StringBuilder();
        line.append( "baby_firstname" ); line.append( "," );
        line.append( "bride_firstname" ); line.append( "," );
        line.append( "baby_surname" ) ;   line.append( "," );
        line.append( "bride_surname" );   line.append( "," );
        line.append( "baby_mother_firstname" ) ; line.append( "," );
        line.append( "bride_mother_firstname" ) ; line.append( "," );
        line.append( "baby_mother_surname" ) ; line.append( "," );
        line.append( "bride_mother_surname" ) ; line.append( "," );
        line.append( "baby_father_firstname" ) ; line.append( "," );
        line.append( "bride_father_firstname" ) ; line.append( "," );
        line.append( "baby_father_surname" ) ; line.append( "," );
        line.append( "bride_father_surname" ) ; line.append( "," );
        
        line.append( "JS first name" );   line.append( "," );
        line.append( "JS surname" );      line.append( "," );
        line.append( "JS father first name" );  line.append( "," );
        line.append( "JS father surname" );  line.append( "," );
        line.append( "JS  mother first name" );  line.append( "," );
        line.append( "JS mother surname" ); line.append( "," );

        line.append( "mean JS dist" );
        writer.println(line);
    }

    private void showNames(String baby_firstname, String baby_surname, String baby_id, String baby_mother_firstname,String baby_mother_surname,String baby_father_firstname,String baby_father_surname,
                           String bride_firstname, String bride_surname, String bride_id,String bride_mother_firstname,String bride_mother_surname,String bride_father_firstname,String bride_father_surname) {
        
        if( !baby_id.equals(bride_id) ) {
            System.out.println( "GT MATCH ERROR");
            System.exit(-1);
        }

        double fnd = metric.distance(baby_firstname, bride_firstname);
        double snd = metric.distance(baby_surname,bride_surname);
        double ffnd = metric.distance(baby_father_firstname, bride_father_firstname);
        double fsnd = metric.distance(baby_father_surname,bride_father_surname);
        double mfnd = metric.distance(baby_mother_firstname, bride_mother_firstname);
        double msnd = metric.distance(baby_mother_surname, bride_mother_surname);

        StringBuilder line = new StringBuilder();
        line.append( baby_firstname ) ; line.append( "," );
        line.append( bride_firstname ); line.append( "," );
        line.append( baby_surname ) ;   line.append( "," );
        line.append( bride_surname );   line.append( "," );
        line.append( baby_mother_firstname ) ; line.append( "," );
        line.append( bride_mother_firstname ) ; line.append( "," );
        line.append( baby_mother_surname ) ; line.append( "," );
        line.append( bride_mother_surname ) ; line.append( "," );
        line.append( baby_father_firstname  ) ; line.append( "," );
        line.append( bride_father_firstname ) ; line.append( "," );
        line.append( baby_father_surname ) ; line.append( "," );
        line.append( bride_father_surname ) ; line.append( "," );
        
        line.append( fnd );                line.append( "," );
        line.append( snd );                line.append( "," );
        line.append( ffnd );               line.append( "," );
        line.append( fsnd );               line.append( "," );
        line.append( mfnd );               line.append( "," );
        line.append( msnd );               line.append( "," );
        
        line.append( (fnd+snd+ffnd+fsnd+mfnd+msnd)/6);

        writer.println( line );
    }

    private String quoteStrip(String name) {
        return name.substring(1,name.length()-1);
    }


    public static void main(String[] args) throws BucketException {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge(); PrintWriter writer = new PrintWriter(FileManipulation.getOutputStreamWriter(Paths.get(OUTPUT_FILE_PATH)))) {

            BirthBrideGTAllNameDifferences en = new BirthBrideGTAllNameDifferences(writer);
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
