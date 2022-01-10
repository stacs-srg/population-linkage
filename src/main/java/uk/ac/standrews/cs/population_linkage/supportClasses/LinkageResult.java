/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LinkageResult {

    private Map<String, Collection<Link>> mapOfLinks = null;
    private LinkageQuality linkageQuality = null;
    private Iterable<Link> linked_pairs;

    public LinkageResult(LinkageQuality linkageQuality, Map<String, Collection<Link>> mapOfLinks, List<Link> links) {
        this.mapOfLinks = mapOfLinks;
        this.linkageQuality = linkageQuality;
        this.linked_pairs = links;
    }

    public LinkageResult(LinkageQuality lq, Iterable<Link> links) {
        this.linkageQuality = lq;
        this.linked_pairs = links;
    }

    public Map<String, Collection<Link>> getMapOfLinks() {
        if(mapOfLinks == null) {
            throw new RuntimeException("map not requested");
        }
        return mapOfLinks;
    }

    public LinkageQuality getLinkageQuality() {
        return linkageQuality;
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

    public Iterable<Link> getLinks() { return linked_pairs; }
}
