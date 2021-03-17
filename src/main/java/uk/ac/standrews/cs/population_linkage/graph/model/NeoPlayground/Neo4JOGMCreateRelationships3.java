/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */

package uk.ac.standrews.cs.population_linkage.graph.model.NeoPlayground;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * use the Object-graph binding from here:
 *
 *     <dependency>
 *     <groupId>org.neo4j</groupId>
 *     <artifactId>neo4j-ogm-core</artifactId>
 *     <version>3.1.2</version>
 *
 *  Examples taken from // see https://github.com/neo4j/neo4j-ogm
 *  and here: https://neo4j.com/developer/neo4j-ogm/
 *
 *  Not so useful: https://github.com/neo4j-examples?q=movies
 *
 * Book? https://neo4j.com/docs/ogm-manual/current/
 *
 */
public class Neo4JOGMCreateRelationships3 implements AutoCloseable
{

    private final Session session;
    private SessionFactory sessionFactory;

    PersonWithFriends albert;  // made global to check for strong ID
    PersonWithFriends victoria;
    PersonWithFriends william;

    public Neo4JOGMCreateRelationships3(String uri, String user, String password )
    {

        Configuration conf = new Configuration.Builder()
                                               .uri(uri)
                                               .credentials(user, password)
                                               .build();
        sessionFactory = new SessionFactory(conf, "uk.ac.standrews.cs.population_linkage.graphDbPlay");
        session = sessionFactory.openSession();
    }

    @Override
    public void close() throws Exception
    {
        sessionFactory.close();
    }

    public void make() {
        albert = new PersonWithFriends("Albert" );
        victoria = new PersonWithFriends("Victoria" );
        william = new PersonWithFriends("William" );
        PersonWithFriends lizzy = new PersonWithFriends("Lizzy" ); // no friends


        Friends f = new Friends();
        f.add( victoria );
        f.add( albert );
        f.add( william );

        // Save all

        session.save( f ); // also saves Victoria William and Albert and the friends which are in closure.
        session.save( lizzy ); // not in closure of f

        // Trace

        System.out.println( "Created:" );
        System.out.println( albert ); System.out.println( victoria ); System.out.println( lizzy ); System.out.println( william );
        System.out.println( "Victoria, Albert and William are friends" );
    }


    /*
     Some Cypher queries for data above:
     These are wrapped in Java below.

        Get everyone who has friends:
        MATCH (f:Friends)-[:FRIEND]->(p:PersonWithFriends) return p

        Just get Victoria:
        MATCH (p:PersonWithFriends) WHERE p.name = "Victoria" return p

        Friends of Victoria - the Friends node not what it is pointing at
        MATCH (f:Friends)-[:FRIEND]->(p:PersonWithFriends) WHERE p.name = "Victoria" return f

        Friends of Victoria - the actual Person nodes - Albert and William but not Victoria
        MATCH (v:PersonWithFriends)<-[:FRIEND]->(:Friends)-[:FRIEND]->(q:PersonWithFriends) WHERE v.name = "Victoria" return q

        As above but including Victoria
        MATCH (v:PersonWithFriends)<-[:FRIEND]->(:Friends)-[:FRIEND]->(q:PersonWithFriends) WHERE v.name = "Victoria" return v,q

     */

    public void doQueries() {
        queryAllWithFriends();
        queryVictoria();
        queryFriendsClassOfVictoria();
        queryFriendsOfVictoria();
        queryFriendsOfVictoriaIncVictoria();
        queryOnId();
    }

    // Get everyone who has friends:
    private void queryAllWithFriends() {
        String query = "MATCH (f:Friends)-[:FRIEND]->(p:PersonWithFriends) return p";
        System.out.println( query );
        Iterable<Map<String, Object>> result = session.query(query, Collections.EMPTY_MAP);
        // The result of the query is a collection of maps
        // Each map has a key - in this case "p" and the results, in this case a PersonWithFriends.
        showResultsAsPersonsWithFriends(result);
    }

    private void queryVictoria() {
        String query = "MATCH (p:PersonWithFriends) WHERE p.name = \"Victoria\" return p";
        // The result of the query is a collection of maps
        // Each map has a key - in this case "p" and the results, in this case a PersonWithFriends.
        System.out.println( query );
        Iterable<Map<String, Object>> result = session.query(query, Collections.EMPTY_MAP);
        showResultsAsPersonsWithFriends(result);
    }

    private void queryFriendsClassOfVictoria() {
        String query = "MATCH (f:Friends)-[:FRIEND]->(p:PersonWithFriends) WHERE p.name = \"Victoria\" return f";
        // The result of the query is a collection of maps
        // Each map has a key - in this case "f" and the results, in this case a Friends.
        System.out.println( query );
        Iterable<Map<String, Object>> result = session.query(query, Collections.EMPTY_MAP);
        System.out.println( query );
        showResultsAsFriends(result);
    }

    private void queryFriendsOfVictoria() {
        String query = "MATCH (v:PersonWithFriends)<-[:FRIEND]->(:Friends)-[:FRIEND]->(q:PersonWithFriends) WHERE v.name = \"Victoria\" return q";
        // The result of the query is a collection of maps
        // Each map has a key - in this case "q" and the results, in this case a PersonWithFriends.
        System.out.println( query );
        Iterable<Map<String, Object>> result = session.query(query, Collections.EMPTY_MAP);
        showResultsAsPersonsWithFriends(result);
    }

    private void queryFriendsOfVictoriaIncVictoria() {
        String query = "MATCH (v:PersonWithFriends)<-[:FRIEND]->(:Friends)-[:FRIEND]->(q:PersonWithFriends) WHERE v.name = \"Victoria\" return v,q";
        // The result of the query is a collection of maps
        // Each map has a key - in this case "v" and "q" and the results, in this case a PersonWithFriends.
        System.out.println( query );
        Iterable<Map<String, Object>> result = session.query(query, Collections.EMPTY_MAP);

        showResultsAsPersonsWithFriends(result);
    }

    private void queryOnId() {
        String query = "MATCH (s) WHERE ID(s) = $node_id RETURN s";
        // The result of the query is a collection of maps
        // Each map has a key - in this case "s" and the results, in this case a PersonWithFriends.
        System.out.println( query );
        Map<String,Object> params = new HashMap<>();
        params.put( "node_id", victoria.getId() );
        Iterable<Map<String, Object>> result = session.query( query, params );
        showResultsAsPersonsWithFriends(result);
    }

    private void showResultsAsPersonsWithFriends(Iterable<Map<String, Object>> result) {
        Iterator<Map<String, Object>> iter = result.iterator();
        while (iter.hasNext()) {
            Map<String, Object> map = iter.next();
            System.out.printf("Found:");
            for( Object v : map.values() ) {
                try {
                    PersonWithFriends p = (PersonWithFriends) v;
                    if( p.getName().equals("Albert")) {
                        System.out.println( p.getName() + " " + ( p == albert ? "referential integrity is preserved" : "no referential integrity" ) );
                    } else  if( p.getName().equals("William")) {
                        System.out.println( p.getName() + " " + ( p == william ? "referential integrity is preserved" : "no referential integrity" ) );
                    } else  if( p.getName().equals("Victoria")) {
                        System.out.println( p.getName() + " " + ( p == victoria ? "referential integrity is preserved" : "no referential integrity" ) );
                    }
                } catch( Exception e ) {
                    System.out.println( "Found: " + v.getClass().toString() + " when expecting a PersonWithFriends" );
                }
            }
        }
    }

    private void showResultsAsFriends(Iterable<Map<String, Object>> result) {
        Iterator<Map<String, Object>> iter = result.iterator();
        while (iter.hasNext()) {
            Map<String, Object> map = iter.next();
            System.out.printf("Found:");
            for( Object v : map.values() ) {
                try {
                    Friends f = (Friends) v;
                    System.out.println( f );
                } catch( Exception e ) {
                    System.out.println( "Found: " + v.getClass().toString() + " when expecting a Friends" );
                }
            }
        }
    }

    public static void main( String... args ) throws Exception
    {
        try ( Neo4JOGMCreateRelationships3 db = new Neo4JOGMCreateRelationships3( "bolt://localhost:7687", "neo4j", "password" ) )
        {
            db.make();
            db.doQueries();
        }
    }
}

