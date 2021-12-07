/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEndIterative;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;

import java.util.ArrayList;
import java.util.List;

public class LinkageResultSpecial extends LinkageResult {

    private List<Link> linked_pairs;
    private List<Link> unlinked_pairs;

    public LinkageResultSpecial(LinkageQuality linkageQuality, List<Link> linked_pairs, List<Link> unlinked_pairs) {
        super(linkageQuality);
        this.linked_pairs = linked_pairs;
        this.unlinked_pairs = unlinked_pairs;
    }

    public List<LXP> getLinkedSearchRecords() throws BucketException, RepositoryException {
        List<LXP> result = new ArrayList<>();
        for (Link linked_pair : linked_pairs) {
            result.add(linked_pair.getRecord2().getReferend());
        }
        return result;
    }

    public List<LXP> getLinkedStoredRecords() throws BucketException, RepositoryException {
        List<LXP> result = new ArrayList<>();
        for (Link linked_pair : linked_pairs) {
            result.add(linked_pair.getRecord1().getReferend());
        }
        return result;
    }
}
