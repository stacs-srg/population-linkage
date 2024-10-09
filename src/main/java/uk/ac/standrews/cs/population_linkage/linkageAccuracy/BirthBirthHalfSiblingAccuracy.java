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

public class BirthBirthHalfSiblingAccuracy extends AbstractAccuracy {

    private static final String BIRTH_BIRTH_HALFSIBLING_TPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE (b1)-[:GT_HALF_SIBLING {actors: \"Child-Child\"}]-(b2) return count(r)";
    private static final String BIRTH_BIRTH_HALFSIBLING_FPC = "MATCH (b1:Birth)-[r:SIBLING {actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:GT_HALF_SIBLING {actors: \"Child-Child\"}]-(b2) return count(r)";
    private static final String BIRTH_BIRTH_HALFSIBLING_FNC = "MATCH (b1:Birth)-[r:GT_HALF_SIBLING { actors: \"Child-Child\"}]->(b2:Birth) WHERE NOT (b1)-[:SIBLING {actors: \"Child-Child\"}]-(b2) return count(r)";

    public BirthBirthHalfSiblingAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
        doqueries();
    }

    private void doqueries() {
        long fpc = doQuery(BIRTH_BIRTH_HALFSIBLING_FPC);
        long tpc = doQuery(BIRTH_BIRTH_HALFSIBLING_TPC);
        long fnc = doQuery(BIRTH_BIRTH_HALFSIBLING_FNC);

        long birth_count = doQuery( ALL_BIRTHS );
        long all_pair_count = nChoose2( birth_count );

        report(fpc,tpc,fnc,all_pair_count);
    }


    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthBirthHalfSiblingAccuracy acc = new BirthBirthHalfSiblingAccuracy(bridge);
            acc.doqueries();
        }
    }
}
