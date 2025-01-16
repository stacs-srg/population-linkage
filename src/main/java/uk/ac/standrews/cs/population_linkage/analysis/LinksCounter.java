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
package uk.ac.standrews.cs.population_linkage.analysis;
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

    /**
     * Method to get the number of links for each linkage algorithm
     *
     * @param bridge Neo4j bride
     * @return array of number of links
     */
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

    /**
     * Method to calculate some overall statistics on the linkage algorithms
     * @param numLinks number of links made per each algorithm
     */
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
