package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

public class GTUtil {

//    private static final String BD_BIRTH_DEATH_GT_IDENTITY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(b:Death) return r";
//    private static final String BD_BIRTH_DEATH_GT_IDENTITY_COUNT = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(b:Death) return COUNT(r)"
//    private static final String BD_BIRTH_DEATH_GT_IDENTITY_COUNT_START = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]->(b:Death) ";
//    private static final String BD_BIRTH_DEATH_GT_IDENTITY_COUNT_END = "return COUNT(r)";
//
//    private static final String BD_DEATH_GT_IDENTITY_EXISTS_QUERY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from AND b.STANDARDISED_ID = $standard_id_to  RETURN r";
//
//    private static final String BD_DEATH_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN r";
//
//
//    public static Map getBirthDeathIdentityLinks( ) {  // TODO Do we need?
//        return null;
//    }
//
//    public static int countBirthDeathIdentityGTLinks(NeoDbCypherBridge bridge, LXP birth_record ) {
//        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID );
//
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("standard_id_from", standard_id_from);
//        Result result = bridge.getNewSession().run(BD_DEATH_GT_IDENTITY_LINKS_QUERY,parameters);
//        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
//        return relationships.size();
//    }
//
//    private static Map<String, Object> getparams(String standard_id_from, String standard_id_to) {
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("standard_id_from", standard_id_from);
//        parameters.put("standard_id_to", standard_id_to);
//        return parameters;
//    }


}
