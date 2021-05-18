/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import java.util.Objects;
import javax.annotation.Nonnull;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

public class ParentalStatusEvaluationApproach extends SubGroupEvaluationApproach {

    public ParentalStatusEvaluationApproach(LinkageRecipe linkageRecipe) {
        super(linkageRecipe);
    }

    protected String identifyGroup(Link proposedLink) {
        try {
            LXP child = proposedLink.getRecord1().getReferend();
            return classifyRecord(child);
        } catch (BucketException e) {
            throw new IllegalStateException("Expected Birth record in ParentalStatusEvaluationApproach");
        }
    }

    protected boolean isStoredRecordInGroup(LXP record, String group) {
        return classifyRecord(record).equals(group);
    }

    protected boolean isSearchRecordInGroup(LXP record, String group) {
        return true;
    } // categorisation is done on the child (stored) record and thus we don't filter out any parent (search) records during evaluation

    @Nonnull
    protected String classifyRecord(LXP record) {
        boolean illegitimate = !Objects.equals(record.getString(Birth.ILLEGITIMATE_INDICATOR), "");
        boolean parentsMarried = !Objects.equals(record.getString(Birth.PARENT_MARRIAGE_RECORD_IDENTITY), "");

        if(illegitimate) {
            return "Residing-Separately";
        } else {
            if(parentsMarried) {
                return "Married";
            } else {
                return "Cohabiting";
            }
        }
    }

    @Override
    public Type getEvaluationDescription() {
        return Type.PARENTAL_STATUS;
    }

}
