/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.graph;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.impl.PersistentObject;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoUtil {

    private static final String FIND_BY_NEO_ID = "MATCH (a) WHERE Id( a ) = $node_id RETURN a";

    public static <T extends PersistentObject> T getByNeoId(Long neo_id, IBucket<T> bucket,NeoDbCypherBridge bridge) throws BucketException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("node_id", neo_id);
        Result result = bridge.getNewSession().run(FIND_BY_NEO_ID,parameters);
        List<Node> nodes = result.list(r -> r.get("a").asNode());
        if( nodes.size() == 1 ) {
            long storr_id = nodes.get(0).get("STORR_ID").asLong();
            return bucket.getObjectById(storr_id);
        } else {
            System.out.println( "Error finding entry with neo_id " + neo_id + " found " + nodes.size() + " nodes" );
            throw new BucketException( "Wrong number of nodes found" );
        }
    }
}
