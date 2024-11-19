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
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolver.msed.Binomials;
import uk.ac.standrews.cs.population_linkage.resolver.msed.MSED;
import uk.ac.standrews.cs.population_linkage.resolver.msed.OrderedList;
import uk.ac.standrews.cs.population_records.RecordRepository;

import java.util.*;

public abstract class SiblingOpenTriangleResolver {
    protected static NeoDbCypherBridge bridge;
    protected static RecordRepository record_repository;

    //Various constants for predicates
    protected static final int MAX_AGE_DIFFERENCE  = 24;
    protected static final double DATE_THRESHOLD = 0.5;
    protected static final double NAME_THRESHOLD = 0.5;
    protected static final int BIRTH_INTERVAL = 270;

    public SiblingOpenTriangleResolver(String sourceRepo) {
        bridge = Store.getInstance().getBridge();
        record_repository= new RecordRepository(sourceRepo);
    }

    protected void addFamilyMSED(List<Set<LXP>> familySets, List<LXP> bs) {
        if(familySets.isEmpty()) {
            familySets.add(new HashSet<>(bs));
        }else{
            boolean familyFound = false;
            for(Set<LXP> fSet : familySets) {
                if(familyFound){
                    break;
                }
                for (int i = 0; i < bs.size(); i++) {
                    if(fSet.contains(bs.get(i))) {
                        fSet.addAll(bs);
                        familyFound = true;
                        break;
                    }
                }
            }
            if(!familyFound) {
                familySets.add(new HashSet<>(bs));
            }
        }
    }

    protected OrderedList<List<LXP>,Double> getMSEDForK(Set<LXP> family, int k, LinkageRecipe recipe) throws BucketException {
        OrderedList<List<LXP>,Double> all_mseds = new OrderedList<>(Integer.MAX_VALUE); // don't want a limit!
        List<LXP> bs = new ArrayList<>(family);

        List<List<Integer>> indices = Binomials.pickAll(bs.size(), k);
        for (List<Integer> choices : indices) {
            List<LXP> births = getRecordsFromChoices(bs, choices);
            double distance = getMSEDForCluster(births, recipe);
            all_mseds.add(births,distance);
        }
        return all_mseds;
    }

    protected double getMSEDForCluster(List<LXP> choices, LinkageRecipe recipe) {
        /* Calculate the MESD for the cluster represented by the indices choices into bs */
        List<String> fields_from_choices = new ArrayList<>(); // a list of the concatenated linkage fields from the selected choices.
        List<Integer> linkage_fields = recipe.getLinkageFields(); // the linkage field indexes to be used
        for (LXP a_birth : choices) {
            StringBuilder sb = new StringBuilder();              // make a string of values for this record drawn from the recipe linkage fields
            for (int field_selector : linkage_fields) {
                sb.append(a_birth.get(field_selector) + "/");
            }
            fields_from_choices.add(sb.toString()); // add the linkage fields for this choice to the list being assessed
        }
        return MSED.distance(fields_from_choices);
    }

    /**
     * Method to create a link between two records
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_z standardised id of record z
     * @param prov provenance of resolver
     */
    protected void createLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_z, String prov, String createQuery) {
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction()) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_z, prov);
            tx.run(createQuery, parameters);
            tx.commit();
        }
    }

    /**
     * Method to create a delete link between two records, used in testing
     *
     * @param bridge Neo4j bridge
     * @param std_id_x standardised id of record x
     * @param std_id_y standardised id of record y
     */
    protected void deleteLink(NeoDbCypherBridge bridge, String std_id_x, String std_id_y, String prov, String deleteQuery){
        try (Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = getCreationParameterMap(std_id_x, std_id_y, prov);
            tx.run(deleteQuery, parameters);
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
    protected Map<String, Object> getCreationParameterMap(String standard_id_from, String standard_id_to, String prov) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        parameters.put("standard_id_to", standard_id_to);
        parameters.put("prov", prov);
        return parameters;
    }


    protected List<LXP> getRecordsFromChoices(List<LXP> bs, List<Integer> choices) {
        List<LXP> births = new ArrayList<>();
        for (int index : choices) {
            births.add( bs.get(index) );
        }
        return births;
    }

    protected abstract void resolveTrianglesMSED(List<List<Long>> triangleChain, Long x, LinkageRecipe recipe, int cPred, int dPred) throws BucketException;

    protected abstract boolean mostCommonBirthPlacePredicate(OpenTriangleCluster cluster, boolean hasChanged, LXP[] tempKids, int predNumber);

    protected abstract boolean minBirthIntervalPredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber);

    protected abstract boolean maxRangePredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber);
}
