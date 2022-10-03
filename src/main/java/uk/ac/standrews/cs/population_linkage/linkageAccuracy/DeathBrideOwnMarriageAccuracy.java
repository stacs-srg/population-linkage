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

public class DeathBrideOwnMarriageAccuracy extends AbstractAccuracy {

    private static final String DEATH_BRIDE_ID_TPC = "MATCH (d:Death)-[r:ID {actors: \"Deceased-Bride\"}]-(m:Marriage) WHERE (d)-[:GT_ID {actors: \"Deceased-Bride\"}]-(m) return count(r)";
    private static final String DEATH_BRIDE_ID_FPC = "MATCH (d:Death)-[r:ID {actors: \"Deceased-Bride\"}]-(m:Marriage) WHERE NOT (d)-[:GT_ID {actors: \"Deceased-Bride\"}]-(m) return count(r)";
    private static final String DEATH_BRIDE_ID_FNC = "MATCH (d:Death)-[r:GT_ID {actors: \"Deceased-Bride\"}]-(m:Marriage) WHERE NOT (d)-[:ID {actors: \"Deceased-Bride\"}]-(m) return count(r)";

    public DeathBrideOwnMarriageAccuracy(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    private void doqueries() {
        long tpc = doQuery(DEATH_BRIDE_ID_TPC);
        long fpc = doQuery(DEATH_BRIDE_ID_FPC);
        long fnc = doQuery(DEATH_BRIDE_ID_FNC);

        long death_count = doQuery( ALL_DEATHS );
        long marriage_count = doQuery( ALL_MARRIAGES );
        long all_pair_count = death_count*marriage_count;

        report(fpc,tpc,fnc,all_pair_count);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            DeathBrideOwnMarriageAccuracy acc = new DeathBrideOwnMarriageAccuracy(bridge);
            acc.doqueries();
        }
    }
}
