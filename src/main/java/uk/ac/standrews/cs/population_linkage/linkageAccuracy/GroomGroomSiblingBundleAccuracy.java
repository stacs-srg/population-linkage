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
package uk.ac.standrews.cs.population_linkage.linkageAccuracy;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

public class GroomGroomSiblingBundleAccuracy extends AbstractAccuracy {

    private static final String GROOM_GROOM_SIBLING_TPC = "MATCH (m1:Marriage)-[r:SIBLING {actors: \"Groom-Groom\"}]-(m2:Marriage) WHERE (m1)-[:GT_SIBLING {actors: \"Groom-Groom\"}]-(m2) return count(r)";
    private static final String GROOM_GROOM_SIBLING_FPC = "MATCH (m1:Marriage)-[r:SIBLING {actors: \"Groom-Groom\"}]-(m2:Marriage) WHERE NOT (m1)-[:GT_SIBLING {actors: \"Groom-Groom\"}]-(m2) return count(r)";
    private static final String GROOM_GROOM_SIBLING_FNC = "MATCH (m1:Marriage)-[r:GT_SIBLING {actors: \"Groom-Groom\"}]-(m2:Marriage) WHERE NOT (m1)-[:SIBLING {actors: \"Groom-Groom\"}]-(m2) return count(r)";

    public GroomGroomSiblingBundleAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    private void doqueries() {
        long tpc = doQuery(GROOM_GROOM_SIBLING_TPC);
        long fpc = doQuery(GROOM_GROOM_SIBLING_FPC);
        long fnc = doQuery(GROOM_GROOM_SIBLING_FNC);

        long marriage_count = doQuery( ALL_MARRIAGES );
        long all_pair_count = nChoose2(marriage_count);

        report(fpc,tpc,fnc,all_pair_count);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            GroomGroomSiblingBundleAccuracy acc = new GroomGroomSiblingBundleAccuracy(bridge);
            acc.doqueries();
        }
    }
}
