/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.Arrays;
import java.util.List;

/*
 *  An index class for creating indexes to enhance the searching performance of neo4j
 *  Run the index class before running class EstablishGTLinks
 *
 * @author Jianheng Huang jh377@st-andrews.ac.uk
 *         Bolun Wang bw93@st-andrews.ac.uk
 */
public class Index {
     /**
     * Cypher to Create Indexes
     */
    private static final String CREATE_CHILD_IDENTITY_INDEX = "CREATE INDEX CHILD_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.CHILD_IDENTITY)";
    private static final String CREATE_BIRTH_MOTHER_IDENTITY_INDEX = "CREATE INDEX BIRTH_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.MOTHER_IDENTITY)";
    private static final String CREATE_BIRTH_FATHER_IDENTITY_INDEX = "CREATE INDEX BIRTH_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (b:Birth) on (b.FATHER_IDENTITY)";
    private static final String CREATE_MARRIAGE_GROOM_IDENTITY_INDEX = "CREATE INDEX GROOM_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_IDENTITY)";
    private static final String CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX = "CREATE INDEX BRIDE_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_IDENTITY)";
    private static final String CREATE_BRIDE_MOTHER_IDENTITY_INDEX = "CREATE INDEX BRIDE_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.BRIDE_MOTHER_IDENTITY)";
    private static final String CREATE_GROOM_MOTHER_IDENTITY_INDEX = "CREATE INDEX GROOM_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (m:Marriage) on (m.GROOM_MOTHER_IDENTITY)";
    private static final String CREATE_DECEASED_IDENTITY_INDEX = "CREATE INDEX DECEASED_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.DECEASED_IDENTITY)";
    private static final String CREATE_DEATH_FATHER_IDENTITY_INDEX = "CREATE INDEX DEATH_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.FATHER_IDENTITY)";
    private static final String CREATE_DEATH_MOTHER_IDENTITY_INDEX = "CREATE INDEX DEATH_MOTHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.MOTHER_IDENTITY)";
    private static final String CREATE_BIRTH_RECORD_IDENTITY_INDEX = "CREATE INDEX BIRTH_RECORD_IDENTITY_INDEX IF NOT EXISTS FOR (d:Death) on (d.BIRTH_RECORD_IDENTITY)";
    private static final String CREATE_BRIDE_FATHER_IDENTITY_INDEX = "CREATE INDEX BRIDE_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Marriage) on (d.BRIDE_FATHER_IDENTITY)";
    private static final String CREATE_GROOM_FATHER_IDENTITY_INDEX = "CREATE INDEX GROOM_FATHER_IDENTITY_INDEX IF NOT EXISTS FOR (d:Marriage) on (d.GROOM_FATHER_IDENTITY)";


    private static void runQueries(NeoDbCypherBridge bridge, List<String> queries){
        try (Session session = bridge.getNewSession()){
            for (String query : queries){
                try{
                    Result result = session.run(query);
                    System.out.println("Established index: " + query);
                }
                catch (RuntimeException e){
                    System.out.println("Exception in index: " + e.getMessage());
                }
            }
        }
        finally {
            System.out.println("Run finished");
            System.exit(0);
        }
    }
// add Create Bride Identity index
    public static List<String> initializeQueries(){
        return Arrays.asList(
                CREATE_BIRTH_FATHER_IDENTITY_INDEX,
                CREATE_BIRTH_MOTHER_IDENTITY_INDEX,
                CREATE_BRIDE_MOTHER_IDENTITY_INDEX,
                CREATE_CHILD_IDENTITY_INDEX,
                CREATE_DEATH_FATHER_IDENTITY_INDEX,
                CREATE_DEATH_MOTHER_IDENTITY_INDEX,
                CREATE_GROOM_MOTHER_IDENTITY_INDEX,
                CREATE_DECEASED_IDENTITY_INDEX,
                CREATE_MARRIAGE_BRIDE_IDENTITY_INDEX,
                CREATE_MARRIAGE_GROOM_IDENTITY_INDEX,

                CREATE_BIRTH_RECORD_IDENTITY_INDEX,
                CREATE_GROOM_FATHER_IDENTITY_INDEX,
                CREATE_BRIDE_FATHER_IDENTITY_INDEX);
    }

    public static void main(String []args){
        NeoDbCypherBridge bridge = new NeoDbCypherBridge();
        List<String> queries = Index.initializeQueries();

        Index.runQueries(bridge, queries);
    }
}
