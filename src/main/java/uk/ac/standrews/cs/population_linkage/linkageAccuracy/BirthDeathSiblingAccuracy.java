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

public class BirthDeathSiblingAccuracy extends AbstractAccuracy {

    private static final String BIRTH_DEATH_SIBLING_TPC = "MATCH (b:Birth)-[r:SIBLING]-(d:Death) WHERE (b)-[:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d) return count(r)";
    private static final String BIRTH_DEATH_SIBLING_FPC = "MATCH (b:Birth)-[r:SIBLING]-(d:Death) WHERE NOT (b)-[:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d) return count(r)";
    private static final String BIRTH_DEATH_SIBLING_FNC = "MATCH (b:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_SIBLING]-(d:Death) WHERE NOT (b)-[:SIBLING]-(d) return count(r)";

    public BirthDeathSiblingAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    private void doqueries() {
        long fpc = doQuery(BIRTH_DEATH_SIBLING_FPC);
        long tpc = doQuery(BIRTH_DEATH_SIBLING_TPC);
        long fnc = doQuery(BIRTH_DEATH_SIBLING_FNC);

        long birth_count = doQuery( ALL_BIRTHS );
        long death_count = doQuery( ALL_DEATHS );
        long all_pair_count = birth_count*death_count;

        report(fpc,tpc,fnc,all_pair_count);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            BirthDeathSiblingAccuracy acc = new BirthDeathSiblingAccuracy(bridge);
            acc.doqueries();
        }
    }
}
