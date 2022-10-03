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

public class DeathDeathSiblingAccuracy extends AbstractAccuracy {

    private static final String DEATH_DEATH_SIBLING_TPC = "MATCH (d1:Death)-[r:SIBLING {actors: \"Deceased-Deceased\"}]-(d2:Death) WHERE (d1)-[:GT_SIBLING {actors: \"Deceased-Deceased\"}]-(d2) return count(r)";
    private static final String DEATH_DEATH_SIBLING_FPC = "MATCH (d1:Death)-[r:SIBLING {actors: \"Deceased-Deceased\"}]-(d2:Death) WHERE NOT (d1)-[:GT_SIBLING {actors: \"Deceased-Deceased\"}]-(d2) return count(r)";
    private static final String DEATH_DEATH_SIBLING_FNC = "MATCH (d1:Death)-[r:GT_SIBLING {actors: \"Deceased-Deceased\"}]-(d2:Death) WHERE NOT (d1)-[:SIBLING {actors: \"Deceased-Deceased\"}]-(d2) return count(r)";

    public DeathDeathSiblingAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    private void doqueries() {
        long fpc = doQuery(DEATH_DEATH_SIBLING_FPC);
        long tpc = doQuery(DEATH_DEATH_SIBLING_TPC);
        long fnc = doQuery(DEATH_DEATH_SIBLING_FNC);

        long death_count = doQuery( ALL_DEATHS );
        long all_pair_count = nChoose2( death_count );

        report(fpc,tpc,fnc,all_pair_count);
    }


    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            DeathDeathSiblingAccuracy acc = new DeathDeathSiblingAccuracy(bridge);
            acc.doqueries();
        }
    }
}
