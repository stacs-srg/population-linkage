/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import java.util.List;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.StandardEvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;

public class SexLinkageRecipe extends LinkageRecipe {

    public enum Sex {
        male,
        female
    }

    private final LinkageRecipe originalLinkageRecipe;
    private final Sex sex;

    public SexLinkageRecipe(LinkageRecipe originalLinkageRecipe, Sex sex) {
        this.originalLinkageRecipe = originalLinkageRecipe;
        this.storr = originalLinkageRecipe.getStorr();
        this.sex = sex;
        addEvaluationsApproach(new StandardEvaluationApproach(this));
    }

    @Override
    public String getLinkageType() {
        return sex.name() + "-" + originalLinkageRecipe.getLinkageType();
    }

    @Override
    public boolean isSiblingLinkage() {
        return originalLinkageRecipe.isSiblingLinkage();
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return originalLinkageRecipe.getSearchType();
    }

    @Override
    public Class<? extends LXP> getSearchType() {
        return originalLinkageRecipe.getStoredType();
    }

    @Override
    public String getStoredRole() {
        return originalLinkageRecipe.getSearchRole();
    }

    @Override
    public String getSearchRole() {
        return originalLinkageRecipe.getStoredRole();
    }

    @Override
    public List<Integer> getLinkageFields() {
        return originalLinkageRecipe.getSearchMappingFields();
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return originalLinkageRecipe.isViableLink(new RecordPair(proposedLink.record2, proposedLink.record1, proposedLink.distance));
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return originalLinkageRecipe.getLinkageFields();
    }

    @Override
    public List<List<Pair>> getTrueMatchMappings() {
        return originalLinkageRecipe.getTrueMatchMappings();
    }

    @Override
    public String getLinkageClassCanonicalName() {
        return originalLinkageRecipe.getLinkageClassCanonicalName();
    }

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {
        return filterBySex(super.getPreFilteredStoredRecords(), Birth.SEX, mapSex(sex));
    }

    @Override
    public Iterable<LXP> getPreFilteredSearchRecords() {
        return filterBySex(super.getPreFilteredSearchRecords(), Birth.SEX, mapSex(sex));
    }

    private String mapSex(Sex sex) {
        switch(sex) {
            case male:
                return "m";
            case female:
                return "f";
        }
        throw new IllegalStateException("Non-existent sex chosen");
    }
}
