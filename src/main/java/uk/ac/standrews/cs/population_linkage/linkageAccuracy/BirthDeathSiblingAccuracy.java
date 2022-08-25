package uk.ac.standrews.cs.population_linkage.linkageAccuracy;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.standrews.cs.utilities.ClassificationMetrics.*;

public class BirthDeathSiblingAccuracy {

    private static final String BIRTH_DEATH_SIBLING_TPC = "MATCH (b:Birth)-[r:SIBLING]-(d:Death) WHERE (b)-[:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d) return count(r)";
    private static final String BIRTH_DEATH_SIBLING_FPC = "MATCH (b:Birth)-[r:SIBLING]-(d:Death) WHERE NOT (b)-[:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d) return count(r)";
    private static final String BIRTH_DEATH_SIBLING_FNC = "MATCH (b:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d:Death) WHERE NOT (b)-[:SIBLING]-(d) return count(r)";
    private static final String ALL_BIRTHS = "MATCH (r:Birth) return count(r)"; // bad name r but saves writing another method
    private static final String ALL_DEATHS = "MATCH (r:Death) return count(r)"; // bad name r but saves writing another method


    public NeoDbCypherBridge getBridge() {
        return bridge;
    }

    private final NeoDbCypherBridge bridge;

    public BirthDeathSiblingAccuracy(NeoDbCypherBridge bridge) {
        this.bridge = bridge;
    }

    private void doqueries() {

        long fpc = doQuery(BIRTH_DEATH_SIBLING_FPC);
        long tpc = doQuery(BIRTH_DEATH_SIBLING_TPC);
        long fnc = doQuery(BIRTH_DEATH_SIBLING_FNC);
        report(fpc,tpc,fnc);
    }

    private void report(long fpc,long tpc,long fnc) {

        System.out.println( this.getClass().getSimpleName() );


        long birth_count = doQuery( ALL_BIRTHS );
        long death_count = doQuery( ALL_DEATHS );

        long tnc = ( birth_count * death_count ) - fpc - tpc - fnc;

        double prec = precision(tpc, fpc);
        double rec = recall(tpc, fnc);
        double f = F1(tpc, fpc, fnc);
        double spec = specificity( tnc, fpc );

        NumberFormat format = new DecimalFormat("0.00");

        System.out.println( "False positive count\t" + fpc );
        System.out.println("True positive count\t" + tpc  );
        System.out.println("False negative count\t" + fnc  );
        System.out.println("True negative count\t" + tnc  );
        System.out.println();
        System.out.println("Precision\t" + format.format( prec ) );
        System.out.println("Recall\t" + format.format( rec ) );
        System.out.println("F1\t" + format.format( f ) );
        System.out.println("Specificity\t" + format.format( spec ) );
        System.out.println();
        System.out.println("----");
    }

    private long doQuery(String query_string) {
        Map<String, Object> parameters = new HashMap<>();
        Result result = bridge.getNewSession().run(query_string, parameters);
        long value = result.list(r -> r.get("count(r)").asInt()).get(0);
        return value;
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthDeathSiblingAccuracy acc = new BirthDeathSiblingAccuracy(bridge);
            acc.doqueries();
        }
    }






}
