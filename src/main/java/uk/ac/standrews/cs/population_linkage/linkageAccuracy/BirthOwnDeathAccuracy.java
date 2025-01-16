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

public class BirthOwnDeathAccuracy extends AbstractAccuracy {

    private static final String BIRTH_DEATH_ID_TPC = "MATCH (b:Birth)-[r:ID {actors: \"Child-Deceased\"}]->(d:Death) WHERE (b)-[:GT_ID {actors: \"Child-Deceased\"}]-(d) AND NOT (b)-[:DELETED]-(d) return count(r)";
    private static final String BIRTH_DEATH_ID_FPC = "MATCH (b:Birth)-[r:ID {actors: \"Child-Deceased\"}]->(d:Death) WHERE NOT (b)-[:GT_ID {actors: \"Child-Deceased\"}]-(d) AND NOT (b)-[:DELETED]-(d) return count(r)";
    private static final String BIRTH_DEATH_ID_FNC = "MATCH (b:Birth)-[r:GT_ID {actors: \"Child-Deceased\"}]->(d:Death) WHERE NOT (b)-[:ID {actors: \"Child-Deceased\"}]-(d) OR (b)-[:DELETED]-(d) return count(r)";

    public BirthOwnDeathAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
        doqueries();
    }

    private void doqueries() {
        long tpc = doQuery(BIRTH_DEATH_ID_TPC);
        long fpc = doQuery(BIRTH_DEATH_ID_FPC);
        long fnc = doQuery(BIRTH_DEATH_ID_FNC);

        long birth_count = doQuery( ALL_BIRTHS );
        long death_count = doQuery( ALL_DEATHS );
        long all_pair_count = birth_count*death_count;

        report(fpc,tpc,fnc,all_pair_count);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthOwnDeathAccuracy acc = new BirthOwnDeathAccuracy(bridge);
        }
    }
}
