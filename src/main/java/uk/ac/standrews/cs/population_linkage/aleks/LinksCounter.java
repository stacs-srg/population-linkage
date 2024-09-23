package uk.ac.standrews.cs.population_linkage.aleks;
import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.graph.Query;

import java.util.List;

public class LinksCounter {

    public static void main(String[] args) {
        NeoDbCypherBridge bridge = Store.getInstance().getBridge();

        System.out.println("Number of links for each algorithm:");
        long[] numLinks = countLinks(bridge);
        calculateBasicStats(numLinks);
    }

    private static long[] countLinks(NeoDbCypherBridge bridge) {
        List <String> queries = Query.getAllLinkQueries();
        List <String> algorithms = Query.getAllLinkerNames();
        long[] numLinks = new long[queries.size()];

        for (int i = 0; i < queries.size(); i++) {
            Result result = bridge.getNewSession().run(queries.get(i));
            List<Long> counts = result.list(r -> r.get("link_count").asLong());
            if (!counts.isEmpty()) {
                long count = counts.get(0);
                numLinks[i] = count;
                System.out.println((i+1) + ". " + algorithms.get(i) + " -> " + count);
            }
        }

        return numLinks;
    }

    private static void calculateBasicStats(long[] numLinks) {
        List <String> algorithms = Query.getAllLinkerNames();
        long lowestNum = 999999999;
        int lowestIndex = -1;
        long highestNum = 0;
        int highestIndex = -1;
        long totalNum = 0;

        for (int i = 0; i < numLinks.length; i++) {
            if(numLinks[i] < lowestNum) {
                lowestNum = numLinks[i];
                lowestIndex = i;
            }else if(numLinks[i] > highestNum) {
                highestNum = numLinks[i];
                highestIndex = i;
            }

            totalNum += numLinks[i];
        }

        System.out.println("Lowest number of links: " + algorithms.get(lowestIndex) + " -> " + lowestNum);
        System.out.println("Highest number of links: " + algorithms.get(highestIndex) + " -> " + highestNum);
        System.out.println("Average number of links: " + totalNum / numLinks.length);
    }
}
