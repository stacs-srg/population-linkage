/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.storr.impl.LXP;

public abstract class SubGroupEvaluationApproach implements EvaluationApproach {

    Map<String, StandardEvaluationApproach> evaluations = new HashMap<>();
    private final LinkageRecipe linkageRecipe;

    public SubGroupEvaluationApproach(LinkageRecipe linkageRecipe) {
        this.linkageRecipe = linkageRecipe;
        getGroups().forEach(group -> {
            StandardEvaluationApproach evaluation = new StandardEvaluationApproach(linkageRecipe);
            evaluation.setSearchRecordsFilter(defineSearchRecordsFilter(group));
            evaluation.setStoredRecordsFilter(defineStoredRecordsFilter(group));
            evaluations.put(group, evaluation);
        });
    }

    protected abstract Set<String> getGroups();

    protected abstract String identifyGroup(Link proposedLink);
    protected abstract boolean isStoredRecordInGroup(LXP record, String group);
    protected abstract boolean isSearchRecordInGroup(LXP record, String group);

    @Override
    public LinkageRecipe getLinkageRecipe() {
        return linkageRecipe;
    }

    @Override
    public Map<String, LinkageQuality> calculateLinkageQuality() {
        Map<String, LinkageQuality> results = new HashMap<>();

        evaluations.forEach((group, evaluationApproach) -> {
            Map<String, LinkageQuality> subGroups = evaluationApproach.calculateLinkageQuality();
            if(subGroups.size() != 1) {
                throw new IllegalStateException("Expected only one group result from StandardEvaluationApproach");
            }
            subGroups.forEach((subGroup,result) -> {
                results.put(group, result);
            });
        });
        return results;
    }

    @Override
    public void evaluateLink(Link proposedLink) {
        String group = identifyGroup(proposedLink);
        StandardEvaluationApproach evaluation = evaluations.get(group);
        if(evaluation == null) {
            evaluation = new StandardEvaluationApproach(linkageRecipe);
            evaluation.setSearchRecordsFilter(defineSearchRecordsFilter(group));
            evaluation.setStoredRecordsFilter(defineStoredRecordsFilter(group));
            evaluations.put(group, evaluation);
        }
        evaluation.evaluateLink(proposedLink);
    }

    private Function<Iterable<LXP>, Iterable<LXP>> defineStoredRecordsFilter(String group) {
        return records ->
                StreamSupport.stream(records.spliterator(), true)
                        .filter(record -> isStoredRecordInGroup(record, group))
                        .collect(Collectors.toList());
    }

    private Function<Iterable<LXP>, Iterable<LXP>> defineSearchRecordsFilter(String group) {
        return records ->
                StreamSupport.stream(records.spliterator(), true)
                        .filter(record -> isSearchRecordInGroup(record, group))
                        .collect(Collectors.toList());
    }
}
