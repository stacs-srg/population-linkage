/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd.builders;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import uk.ac.standrews.cs.population_linkage.graph.model.BirthRecord;
import uk.ac.standrews.cs.population_linkage.graph.util.NeoDbOGMBridge;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;


public class Lookup implements AutoCloseable {

    private final NeoDbOGMBridge bridge = new NeoDbOGMBridge();
    private final Session session;

    public Lookup() {
        session = bridge.getNewSession();

    }

    public void close() throws Exception {
        bridge.close();
    }

    public BirthRecord doQuery() throws Exception {
        String query = "MATCH (n:BirthRecord) WHERE n.STANDARDISED_ID = \"452634\" return n";

        Result res = session.query(query, Collections.EMPTY_MAP);

        Iterator<Map<String, Object>> iter = res.queryResults().iterator();
        int count = 0;
        Map<String, Object> result_map = null;
        for ( ; iter.hasNext() ; count++ ) {
            result_map = iter.next();
        }
        if( count == 1 ) {
            return (BirthRecord) result_map.get("n");
        } else {
            throw new Exception();
        }
    }


    public static void main(String... args) throws Exception {
        try (Lookup db = new Lookup()) {
            System.out.println("Looking up record");
            BirthRecord our_record = db.doQuery();
            System.out.println( "Surname on record is: " + our_record.SURNAME );

        }
    }
}


