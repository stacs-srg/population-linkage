/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

public class EmploymentDataEvaluationApproach extends SubGroupEvaluationApproach {

    public EmploymentDataEvaluationApproach(LinkageRecipe linkageRecipe) {
        super(linkageRecipe);
    }

    @Override
    protected Set<String> getGroups() {
        return Sets.newHashSet("both-no-occupation", "male-no-occupation", "female-no-occupation", "both-have-occupation");
    }

    protected String identifyGroup(Link proposedLink) {
        try {
            LXP death = proposedLink.getRecord1().getReferend();
            return classifyRecord(death);
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
    protected String classifyRecord(LXP death) {

        boolean deathDeceasedOccupationMissing = Objects.equals(death.getString(Death.OCCUPATION), "");
        boolean deathSpouseOccupationMissing = Objects.equals(death.getString(Death.SPOUSE_OCCUPATION), "");

        if(deathDeceasedOccupationMissing && deathSpouseOccupationMissing) {
            return "both-no-occupation";
        } else if(deathDeceasedOccupationMissing) {
            return "male-no-occupation";
        } else if(deathSpouseOccupationMissing) {
            return "female-no-occupation";
        } else {
            return "both-have-occupation";
        }
    }

    @Override
    public Type getEvaluationDescription() {
        return Type.EMPLOYMENT_DATA;
    }

}
