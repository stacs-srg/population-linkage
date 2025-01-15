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
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.resolverExperiments.msed.Binomials;
import uk.ac.standrews.cs.population_linkage.resolverExperiments.msed.MSED;
import uk.ac.standrews.cs.population_linkage.resolverExperiments.msed.OrderedList;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.util.*;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe.list;

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

    /**
     * Method of adding children to family sets
     *
     * @param familySets family sets to add to
     * @param bs combination of children
     */
    protected void addFamilyMSED(List<Set<LXP>> familySets, List<LXP> bs) {
        if(familySets.isEmpty()) { //if set is empty, add children automatically
            familySets.add(new HashSet<>(bs));
        }else{
            boolean familyFound = false;
            for(Set<LXP> fSet : familySets) { //if not, loop through sets
                for (int i = 0; i < bs.size(); i++) {
                    if (!Collections.disjoint(fSet, bs) && fSet.size() < 24) { //if a child is present in a set, add the rest as well
                        fSet.addAll(bs);
                        familyFound = true;
                        break;
                    }
                }
            }
            if(!familyFound) { //if not child is found, create a new set
                familySets.add(new HashSet<>(bs));
            }
        }
    }

    /**
     * Method to get MSED distance for a combination of nodes
     *
     * @param family a set of records in a family
     * @param k number of records in a combination
     * @param recipe recipe to get linkage fields to get distance
     * @return a list of combinations and their distances in order of distance
     * @throws BucketException
     */
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

    /**
     * Method to calculate the MESD for the cluster represented by the indices choices into bs
     *
     * @param choices records to get distance
     * @param recipe recipe to get linkage fields to get distance
     * @return MSED distance for cluster
     */
    protected double getMSEDForCluster(List<LXP> choices, LinkageRecipe recipe) {
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
     * Method to convert string representation of birthdays into LocalDate
     *
     * @param child child node
     * @param isDead check if it is a death certificate
     * @return birthday as LocalDate
     */
    protected LocalDate getBirthdayAsDate(LXP child, boolean isDead){
        int day = 1;

        if(isDead){
            //if missing day, set to first of month
            if(!Objects.equals(child.getString(Death.DATE_OF_BIRTH).substring(0, 2), "--")){
                day = Integer.parseInt(child.getString(Death.DATE_OF_BIRTH).substring(0, 2));
            }

            //get date
            return LocalDate.of(Integer.parseInt(child.getString(Death.DATE_OF_BIRTH).substring(6)), Integer.parseInt(child.getString(Death.DATE_OF_BIRTH).substring(3, 5)), day);
        }else{
            //if missing day, set to first of month
            if(!Objects.equals(child.getString(Birth.BIRTH_DAY), "--")){
                day = Integer.parseInt(child.getString(Birth.BIRTH_DAY));
            }

            //get date
            return LocalDate.of(Integer.parseInt(child.getString(Birth.BIRTH_YEAR)), Integer.parseInt(child.getString(Birth.BIRTH_MONTH)), day);
        }

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


    /**
     * Get list of records from an array of choices. Used to generated binomial combinations
     *
     * @param bs list of records
     * @param choices list of choices as index
     * @return combinations of records
     */
    protected List<LXP> getRecordsFromChoices(List<LXP> bs, List<Integer> choices) {
        List<LXP> records = new ArrayList<>();
        for (int index : choices) {
            records.add( bs.get(index) );
        }
        return records;
    }

    /**
     * Method to get composite measure for dates to calculate distance
     *
     * @param base_measure base measure to be used
     * @return composite measure
     */
    protected LXPMeasure getCompositeMeasureDate(StringMeasure base_measure) {
        final List<Integer> LINKAGE_FIELDS = list(
                Birth.PARENTS_DAY_OF_MARRIAGE,
                Birth.PARENTS_MONTH_OF_MARRIAGE,
                Birth.PARENTS_YEAR_OF_MARRIAGE
        );

        return new SumOfFieldDistances(base_measure, LINKAGE_FIELDS);
    }

    /**
     * Method to get distance between two nodes based on their storr ID
     *
     * @param id1 ID of record 1
     * @param id2 ID of record 2
     * @param composite_measure measure to be used
     * @param births births bucket
     * @return distance between two records
     * @throws BucketException
     */
    protected double getDistance(long id1, long id2, LXPMeasure composite_measure, IBucket births) throws BucketException {
        LXP b1 = (LXP) births.getObjectById(id1);
        LXP b2 = (LXP) births.getObjectById(id2);
        return composite_measure.distance(b1, b2);
    }

    /**
     * Method to resolve open triangles using MSED
     *
     * @param triangleChain chain of open triangles
     * @param x x node in the cluster
     * @param recipe recipe for particular linkage
     * @param dPred predicate number to delete
     * @throws BucketException
     */
    public void resolveTrianglesMSED(List<List<Long>> triangleChain, Long x, LinkageRecipe recipe, String dPred, int standID, String query) throws BucketException {
        double THRESHOLD = 0.03;
        double TUPLE_THRESHOLD = 0.01;

        List<Set<LXP>> familySets = new ArrayList<>();
        List<List<LXP>> toDelete = new ArrayList<>();

        for (List<Long> chain : triangleChain){
            List<Long> listWithX = new ArrayList<>(Arrays.asList(x));
            listWithX.addAll(chain);
            List<LXP> bs = getRecords(listWithX, record_repository);

            cleanStrings(bs);

            //If below threshold, add all children to family
            double distance = getMSEDForCluster(bs, recipe);
            double distanceXY = getMSEDForCluster(bs.subList(0, 2), recipe);
            double distanceZY = getMSEDForCluster(bs.subList(1, 3), recipe);

            if(distance < THRESHOLD) { //if above threshold, delete triangle links
                addFamilyMSED(familySets, bs);
            }else if(distance > THRESHOLD){
                toDelete.add(bs);
                if(distanceXY < TUPLE_THRESHOLD){ //check if xy are potential siblings
                    addFamilyMSED(familySets, bs.subList(0, 2));
                }
                if (distanceZY < TUPLE_THRESHOLD){ //check if zy are potential siblings
                    addFamilyMSED(familySets, bs.subList(1, 3));
                }
            }
        }

        List<Set<LXP>> setsToRemove = new ArrayList<>();
        List<Set<LXP>> setsToAdd = new ArrayList<>();

        //filter families further
        for (Set<LXP> fSet : familySets) {
            int k = 3;
            if (fSet.size() >= k) { //filter only if family set is bigger than 3 siblings
                OrderedList<List<LXP>,Double> familySetMSED = getMSEDForK(fSet, k, recipe); //get distances for all possible combinations of families
                List<Double> distances = familySetMSED.getComparators();
                List<List<LXP>> records = familySetMSED.getList();
                List<Set<LXP>> newSets = new ArrayList<>();

                newSets.add(new HashSet<>(records.get(0)));

                //loop through each distance
                for (int i = 1; i < distances.size(); i++) {
                    //if distance increases dramatically or exceeds 0.01, assume one child is odd one out and dont add to family set
                    if ((distances.get(i) - distances.get(i - 1)) / distances.get(i - 1) > 0.5 || distances.get(i) > 0.01) {
                        break;
                    } else {
                        boolean familyFound = false;
                        for (Set<LXP> nSet : newSets) {
                            if (!Collections.disjoint(nSet, records.get(i))) {
                                nSet.addAll(records.get(i));
                                familyFound = true;
                                break;
                            }
                        }

                        if (!familyFound) {
                            newSets.add(new HashSet<>(records.get(i)));
                        }
                    }
                }

                //add new sets
                setsToRemove.add(fSet);
                setsToAdd.addAll(newSets);
            }
        }

        //reset current family sets
        familySets.removeAll(setsToRemove);
        familySets.addAll(setsToAdd);

        //loop through all triangles to delete
        for (List<LXP> triangleToDelete : toDelete) {
            for(Set<LXP> fSet : familySets) {
                if(fSet.size() > 1){
                    int kidsFound = 0;
                    List<Integer> kidsIndex = new ArrayList<>(Arrays.asList(0, 1, 2));
                    for (int i = 0; i < triangleToDelete.size(); i++) { //identify if any of the family sets contain at least 2 children from triangle to delete
                        if(fSet.contains(triangleToDelete.get(i))) {
                            kidsIndex.remove((Integer.valueOf(i)));
                            kidsFound++;
                        }
                    }

                    //if the children were located, delete the odd one out
                    if(kidsFound == 2 && kidsIndex.size() == 1) {
                        if(kidsIndex.get(0) == 0){
                            deleteLink(bridge, triangleToDelete.get(0).getString(standID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), dPred, query);
                            break;
                        } else if (kidsIndex.get(0) == 2) {
                            deleteLink(bridge, triangleToDelete.get(2).getString(standID), triangleToDelete.get(1).getString(Birth.STANDARDISED_ID), dPred, query);
                            break;
                        }
                    }
                }
            }
        }
    }
    protected abstract boolean mostCommonBirthPlacePredicate(OpenTriangleCluster cluster, boolean hasChanged, LXP[] tempKids, int predNumber);

    protected abstract boolean minBirthIntervalPredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber);

    protected abstract boolean maxRangePredicate(OpenTriangleCluster cluster, LXP[] tempKids, boolean hasChanged, int predNumber);

    protected abstract void cleanStrings(List<LXP> bs);

    protected abstract List<LXP> getRecords(List<Long> sibling_ids, RecordRepository record_repository) throws BucketException;
}
