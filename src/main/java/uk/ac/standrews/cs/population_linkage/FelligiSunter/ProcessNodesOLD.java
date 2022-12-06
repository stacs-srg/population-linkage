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
package uk.ac.standrews.cs.population_linkage.FelligiSunter;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.utilities.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ProcessNodesOLD {

    private ArrayList<String> fieldnames = new ArrayList();

    public List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge, String query) {
        Result result = bridge.getNewSession().run(query);
        return result.list(r -> new Pair<>(r.get("first_result").asNode(), r.get("second_result").asNode()));
    }


    public List<Pair<Node, Node>> getPairs(NeoDbCypherBridge bridge, String query1, String query2 ) {

        Result result = bridge.getNewSession().run(query1);
        List<Node> births = result.list(r -> r.get("result").asNode());
        result = bridge.getNewSession().run(query2);
        List<Node> marriages = result.list(r -> r.get("result").asNode());
        return zip(births,marriages);
    }

    public void calculatePriorsOLDVERSION(NeoDbCypherBridge bridge, String all_the_ms_query, String sample_from_set1_query, String us_from_set2_query,
                                          String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key,
                                          String report_field1, String report_field2) {

        List<Pair<Node, Node>> u_pairs = getPairs(bridge,sample_from_set1_query,us_from_set2_query);

        processPairs(u_pairs,
                node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key,
                report_field1,  report_field2, "u");

        List<Pair<Node, Node>> m_pairs = getPairs(bridge,all_the_ms_query);

        processPairs(m_pairs,
                node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key,
                report_field1,  report_field2, "m");

        fieldnames.add( report_field1 );
        fieldnames.add( report_field2 );
    }

    public static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)))
                .collect(Collectors.toList());
    }


    public void processPairs2(NeoDbCypherBridge bridge, String query,
                          String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key,
                          String report_field1, String report_field2, String u_or_m ) {

        PairProcessor pp = new PairProcessor(bridge, u_or_m, report_field1, report_field2);

        Result result = bridge.getNewSession().run(query);
        while( result.hasNext() ) {
            Record next = result.next();
            Node first = next.get("first_result").asNode();
            Node second = next.get("second_result").asNode();
            pp.processPair( first, second, node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key );
        }
//        pp.showPairs();
//        pp.generatePriorArrays();
    }


    public void processPairs(List<Pair<Node, Node>> nodes,
                             String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key,
                             String report_field1, String report_field2,
                             String u_or_m) {

        int first_matched = 0;
        int second_matched = 0;
        int total = nodes.size();

        for (Pair<Node, Node> p : nodes) {
            Node node1 = p.X();
            Node node2 = p.Y();
            String field1_node1 = node1.get(node1_field1_key).toString(); // this is in Node space from Cypher not LXP!
            String field2_node1 = node1.get(node1_field2_key).toString();

            String field1_node2 = node2.get(node2_field1_key).toString();
            String field2_node2 = node2.get(node2_field2_key).toString();

            if ( field1_node1.equals(field1_node2) ) {
                first_matched++;
            }

            if( field2_node1.equals(field2_node2) ) {
                second_matched++;
            }
        }

        int first_unmatched = total - first_matched;
        int second_unmatched = total - second_matched;

        showPair( u_or_m, report_field1, first_matched, first_unmatched );
        showPair( u_or_m, report_field2, second_matched, second_unmatched );
    }

    public void generatePriorArrays() {
        System.out.println();
        System.out.print( "public static final List<Double> m_priors = Arrays.asList( new Double[]{ " );
        System.out.print( generateList("m") );
        System.out.println( "} );" );
        System.out.print( "public static final List<Double> u_priors = Arrays.asList( new Double[]{ " );
        System.out.print( generateList("u") );
        System.out.println( " } );" );
    }

    private String generateList(String prefix) {
        StringBuffer sb = new StringBuffer();
        for( String fieldname : fieldnames ) {
            sb.append( prefix + "_prior_" + fieldname );
            sb.append( "," );
        }
        sb.delete( sb.length() - 1, sb.length() );
        return sb.toString();
    }


    public void showPair( String u_or_m, String fieldname, int matched, int unmatched ) {
        int total = matched + unmatched;
        System.out.println( "// " + u_or_m + " " + fieldname + " match = " + matched );
        System.out.println( "// " + u_or_m + " " + fieldname + " unmatched = " + unmatched );
        System.out.println( "// " + u_or_m + " Total = " + ( matched + unmatched ) );
        System.out.println( "private static final double " + u_or_m + "_prior_" + fieldname + " = " + (float) matched / ( (float) total ) + ";" );
    }

    public void calculatePriors22(NeoDbCypherBridge bridge, String all_the_ms_query, String sample_from_set1_query, String us_from_set2_query,
                                String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key,
                                String report_field1, String report_field2) {

      //  List<Pair<Node, Node>> u_pairs = getPairs(bridge,sample_from_set1_query,us_from_set2_query);

        PairProcessor pp = new PairProcessor(bridge, "u", report_field1, report_field2);
        pp.processQueries( sample_from_set1_query,us_from_set2_query,node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key );
        pp.showPairs();

        pp = new PairProcessor(bridge, "m", report_field1, report_field2);
        pp.processQuery( all_the_ms_query,node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key );
        pp.showPairs();

        fieldnames.add( report_field1 );
        fieldnames.add( report_field2 );

    }

    /**
     *
     * Prints the oddsPrior
     * @param bridge
     * @param size_set1_query
     * @param size_set2_query
     */
    public static void calculateOddsPrior(NeoDbCypherBridge bridge, String count_expected_matches_query, String size_set1_query, String size_set2_query) {

        Result r = bridge.getNewSession().run(count_expected_matches_query);
        int expected_matches = r.stream().findFirst().get().get( "count(r)" ).asInt();
        System.out.println( "// expected_matches = " + expected_matches );

        r = bridge.getNewSession().run(size_set1_query);
        long count1 = r.stream().findFirst().get().get( "count(r)" ).asInt();

        r = bridge.getNewSession().run(size_set2_query);
        long count2 = r.stream().findFirst().get().get( "count(r)" ).asInt();

        double m_prior = ( expected_matches * 1.0d ) / ( count1 * count2 );
        double u_prior = 1.0d - m_prior;

        System.out.println( "private static final double m_prior = " + m_prior + ";" );
        System.out.println( "private static final double u_prior = 1d - m_prior;" );
        System.out.println( "public static final double odds_prior = m_prior/u_prior;" );
        System.out.println();

    }

    protected void provenanceComment() {
        System.out.println( "// Generated by " + this.getClass().getName() + " on " + LocalDate.now() );
        System.out.println();
    }

    private class PairProcessor {

        private final String u_or_m;
        private final NeoDbCypherBridge bridge;

        private int first_matched = 0;
        private int second_matched = 0;
        private int first_unmatched;
        private int second_unmatched;
        private int total = 0;

        private final String report_field1;
        private final String report_field2;

        public PairProcessor(NeoDbCypherBridge bridge, String u_or_m, String report_field1, String report_field2) {
            this.bridge = bridge;
            this.u_or_m = u_or_m;
            this.report_field1 = report_field1;
            this.report_field2 = report_field2;
        }

        private void processPair(Node first, Node second, String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key) {

            String field1_node1 = first.get(node1_field1_key).toString(); // this is in Node space from Cypher not LXP!
            String field2_node1 = first.get(node1_field2_key).toString();

            String field1_node2 = second.get(node2_field1_key).toString();
            String field2_node2 = second.get(node2_field2_key).toString();

            if (field1_node1.equals(field1_node2)) {
                first_matched++;
            }

            if (field2_node1.equals(field2_node2)) {
                second_matched++;
            }

            total = total + 1;
        }

        public void showPairs() {
            first_unmatched = total - first_matched;
            second_unmatched = total - second_matched;
            showPair(u_or_m, report_field1, first_matched, first_unmatched);
            showPair(u_or_m, report_field2, second_matched, second_unmatched);
        }

        public void processQueries(String query1, String query2, String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key) {
            Result result1 = bridge.getNewSession().run(query1);
            Result result2 = bridge.getNewSession().run(query2);
            while (result1.hasNext() && result2.hasNext()) {
                Record r1 = result1.next();
                Record r2 = result2.next();

                Node first = r1.get("result").asNode();
                Node second = r2.get("result").asNode();
                processPair(first, second, node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key);
            }
        }

        public void processQuery(String query, String node1_field1_key, String node1_field2_key, String node2_field1_key, String node2_field2_key) {
            Result result = bridge.getNewSession().run(query);
            while (result.hasNext()) {
                Record r = result.next();

                Node first = r.get("first_result").asNode();
                Node second = r.get("second_result").asNode();
                processPair(first, second, node1_field1_key, node1_field2_key, node2_field1_key, node2_field2_key);
            }
        }
    }
}
