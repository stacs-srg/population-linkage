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
package uk.ac.standrews.cs.population_linkage.aleks.resolvers;

import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.util.*;

public abstract class IdentityOpenTriangleResolver {
    protected NeoDbCypherBridge bridge;
    protected RecordRepository record_repository;

    protected final int MIN_MARRIAGE_AGE = 15;
    protected final int MAX_MARRIAGE_AGE = 60;

    public IdentityOpenTriangleResolver(String sourceRepo) {
        bridge = Store.getInstance().getBridge();
        record_repository= new RecordRepository(sourceRepo);
    }

    /**
     * Method to create a delete link between two records, used in testing
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_y standardised id of record y
     */
    protected void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y, String actor, String prov, String query){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y, prov, actor);
            tx.run(query, parameters);
            tx.commit();
        }
    }

    /**
     * Method to get map of parameters to be used in cypher queries
     *
     * @param standard_id_from record ID to link from
     * @param standard_id_to record ID to link to
     * @param prov provenance of resolver
     * @return map of parameters
     */
    protected Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String prov, String actor) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", prov);
        parameters.put("actor", actor);
        return parameters;
    }

    protected double getDistance(LXP id1, LXP id2, LXPMeasure composite_measure) throws BucketException {
        return composite_measure.distance(id1, id2);
    }
}
