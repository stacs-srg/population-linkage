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

public class BrideBrideSiblingBundleAccuracy extends AbstractAccuracy {

    private static final String BRIDE_BRIDE_SIBLING_TPC = "MATCH (m1:Marriage)-[r:SIBLING]-(m2:Marriage) WHERE (m1)-[:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]-(m2) return count(r)";
    private static final String BRIDE_BRIDE_SIBLING_FPC = "MATCH (m1:Marriage)-[r:SIBLING]-(m2:Marriage) WHERE NOT (m1)-[:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]-(m2) return count(r)";
    private static final String BRIDE_BRIDE_SIBLING_FNC = "MATCH (m1:Marriage)-[r:GROUND_TRUTH_BRIDE_BRIDE_SIBLING]-(m2:Marriage) WHERE NOT (m1)-[:SIBLING]-(m2) return count(r)";

    public BrideBrideSiblingBundleAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    private void doqueries() {
        long tpc = doQuery(BRIDE_BRIDE_SIBLING_TPC);
        long fpc = doQuery(BRIDE_BRIDE_SIBLING_FPC);
        long fnc = doQuery(BRIDE_BRIDE_SIBLING_FNC);

        long marriage_count = doQuery( ALL_MARRIAGES );
        long all_pair_count = nChoose2( marriage_count );

        report(fpc,tpc,fnc,all_pair_count);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BrideBrideSiblingBundleAccuracy acc = new BrideBrideSiblingBundleAccuracy(bridge);
            acc.doqueries();
        }
    }
}
