/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeLinker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public abstract class IndirectLinkageRecipe {

    public abstract Map<String, Collection<DoubleLink>> getPotentialLinks() throws BucketException;

    public LinkageQuality evaluateIndirectLinkage(EvaluationApproach directLinkerForGT) throws BucketException, PersistentObjectException {
        return selectAndAssessIndirectLinks(getPotentialLinks(), directLinkerForGT);
    }

    protected static LinkageQuality selectAndAssessIndirectLinks(Map<String, Collection<DoubleLink>> indirectLinks, EvaluationApproach directLinkageForGT) throws BucketException, PersistentObjectException {

        long numberOfGroundTruthTrueLinks = directLinkageForGT.getNumberOfGroundTruthTrueLinks();

        long tp = 0; // these are counters with which we use if evaluating
        long fp = 0;

        for (Collection<DoubleLink> links : indirectLinks.values()) {
            Link link = chooseIndirectLink(links);
            if (trueMatch(link, directLinkageForGT)) {
                tp++;
            } else {
                fp++;
            }
        }

        long fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return lq;
    }

    protected static Link chooseIndirectLink(Collection<DoubleLink> links) throws BucketException, PersistentObjectException {
        Link bestLink = null;

        for (DoubleLink link : links) {
            if (bestLink == null) {
                bestLink = link.directLink();
            } else {
                if (bestLink.getDistance() > link.directLink().getDistance())
                    bestLink = link.directLink();
            }
        }
        return bestLink;
    }

    protected static boolean trueMatch(Link link, EvaluationApproach directLinkageForGT) throws BucketException {
        return directLinkageForGT.isTrueMatch(link.getRecord1().getReferend(), link.getRecord2().getReferend()).equals(TRUE_MATCH);
    }

    protected static Map<String, Collection<DoubleLink>> combineLinks(Map<String, Collection<Link>> firstLinks, Map<String, Collection<Link>> secondLinks, String linkType) throws BucketException {

        Map<String, Collection<DoubleLink>> doubleLinksByFirstRecordID = new HashMap<>();

        for (String record1ID : firstLinks.keySet()) {

            Collection<Link> firstLinksByID = firstLinks.get(record1ID);
            for (Link link1 : firstLinksByID) {

                String record2ID = originalId(link1.getRecord2().getReferend());
                if (secondLinks.get(record2ID) != null) {
                    for (Link link2 : secondLinks.get(record2ID)) {
                        doubleLinksByFirstRecordID.computeIfAbsent(record1ID, o ->
                                new ArrayList<>()).add(new DoubleLink(link1, link2, linkType));
                    }
                }
            }
        }

        return doubleLinksByFirstRecordID;
    }

    public static String originalId(LXP record) {
        if(record instanceof Birth)
            return record.getString(Birth.ORIGINAL_ID);
        if(record instanceof Marriage)
            return record.getString(Marriage.ORIGINAL_ID);
        if(record instanceof Death)
            return record.getString(Death.ORIGINAL_ID);

        try { // this is used in the case that storr has lost track of the instance type
            return record.getString(record.getMetaData().getSlot("ORIGINAL_ID"));
        } catch (Exception e) {
            throw new Error("Record of unknown type: " + record.getClass().getCanonicalName());
        }

    }

}
