/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.Storr;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.StandardEvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.Evaluation.list;

public abstract class LinkageRecipe {

    protected Storr storr;

    private int pre_filtering_required_populated_linkage_fields = 0;

    protected LinkageRecipe(Storr storr) {
        this.storr = storr;
        addEvaluationsApproach(new StandardEvaluationApproach(this));
    }

    protected LinkageRecipe() { }

    public abstract String getLinkageType();
    public abstract boolean isSiblingLinkage();
    public abstract Class<? extends LXP> getStoredType();
    public abstract Class<? extends LXP> getSearchType();
    public abstract String getStoredRole();
    public abstract String getSearchRole();
    public abstract List<Integer> getLinkageFields();
    public abstract List<Integer> getSearchMappingFields();
    public abstract List<List<Pair>> getTrueMatchMappings();
    public abstract boolean isViableLink(RecordPair proposedLink);

    public List<List<Pair>> getExcludedMatchMappings() {
        return list();
        // this is for excluding certain 'matches' from evaluation - override this for symmetric sibling linkage to prevent reward/penalisation wrt linking a person to themselves as its the same record
        // for other linkage recipes this will likely be an empty list
    }

    private HashMap<EvaluationApproach.Type, EvaluationApproach> evaluationApproaches = new HashMap<>();

    public HashMap<EvaluationApproach.Type, EvaluationApproach> getEvaluationsApproaches() {
        return evaluationApproaches;
    }

    public void addEvaluationsApproach(EvaluationApproach evaluationApproach) {
        evaluationApproaches.put(evaluationApproach.getEvaluationDescription(), evaluationApproach);
    }

    public Iterable<LXP> getStoredRecords() {
        return storr.getIterable(getStoredType());
    }

    public Iterable<LXP> getSearchRecords() {
        return storr.getIterable(getSearchType());
    }

    public Storr getStorr() {
        return storr;
    }

    private Iterable<LXP> preFilteredRecords = null;

    public synchronized Iterable<LXP> getPreFilteredStoredRecords() {
        if (isLinkageSymmetric()) {
            if (preFilteredRecords == null) {
                // we do this for symmetric linkage recipes as it ensures the iterables
                // returned by this method and the one for records 2 is the same object - this is required by the
                // implementation of similarity search - otherwise we link to people to themselves
                preFilteredRecords = filterSourceRecords(getStoredRecords(), getLinkageFields());
            }
            return preFilteredRecords;
        } else {
            return filterSourceRecords(getStoredRecords(), getLinkageFields());
        }
    }

    public Iterable<LXP> getPreFilteredSearchRecords() {
        if (isLinkageSymmetric()) {
            return getPreFilteredStoredRecords();
        } else {
            return filterSourceRecords(getSearchRecords(), getSearchMappingFields());
        }
    }

    public boolean isLinkageSymmetric() {
        // A linkage is symmetric if both record sets being linked have the same: record type AND role
        // (By definition this must mean that the chosen linkage fields are the same for both records)
        return getStoredType().equals(getSearchType()) && getStoredRole().equals(getSearchRole());
    }

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, List<Integer> filterOn, int reqPopulatedFields) {
        Collection<LXP> filteredRecords = new HashSet<>();

        for (LXP record : records) {

            int numberOfEmptyFieldsPermitted = filterOn.size() - reqPopulatedFields + 1;

            for (int attribute : filterOn) {
                String value = record.getString(attribute).toLowerCase().trim();
                if (value.equals("") || value.contains("missing")) {
                    numberOfEmptyFieldsPermitted--;
                }

                if (numberOfEmptyFieldsPermitted == 0) {
                    break;
                }

            }

            if (numberOfEmptyFieldsPermitted > 0) { // this is a data-full record that we want to keep
                filteredRecords.add(record);
            }
        }

        return filteredRecords;
    }

    protected Iterable<LXP> filterBySex(Iterable<LXP> records, int sexField, String keepSex) {
        Collection<LXP> filteredRecords = new HashSet<>();

        records.forEach(record -> {
            if (record.getString(sexField).toLowerCase().equals(keepSex.toLowerCase()))
                filteredRecords.add(record);
        });
        return filteredRecords;
    }

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, List<Integer> filterOn) {
        return filterSourceRecords(records, filterOn, pre_filtering_required_populated_linkage_fields);
    }

    public void setPreFilteringRequiredPopulatedLinkageFields(int preFilteringRequiredPopulatedLinkageFields) {
        if (preFilteringRequiredPopulatedLinkageFields > getLinkageFields().size()) {
            System.out.printf("Requested more linkage fields to be populated than are present - setting to number of linkage fields - %d \n", getLinkageFields().size());
            this.pre_filtering_required_populated_linkage_fields = getLinkageFields().size();
        } else {
            this.pre_filtering_required_populated_linkage_fields = preFilteringRequiredPopulatedLinkageFields;
        }
    }

    public int getSearchSetSize() {
        return storr.getSize(getSearchType());
    }

    public int getStoredSetSize() {
        return storr.getSize(getStoredType());
    }

    public String getLinkageClassCanonicalName() {
        return this.getClass().getCanonicalName();
    };
}
