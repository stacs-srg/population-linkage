/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.endToEnd;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MetaMarriage {

    private List<Link> groom_links = new ArrayList<>();             // Links between birth records and the groom in this marriage.
    private List<Link> bride_links = new ArrayList<>();             // ALinks between birth records and the groom in this marriage.
    private List<Link> grooms_children_links = new ArrayList<>();   // Links between birth records of grooms children and the groom in this marriage
    private List<Link> brides_children_links = new ArrayList<>();   // Links between birth records of brides children and the groom in this marriage

    private Set<LXP> groom_births = new TreeSet<>();            // the birth records for all the grooms in this marriage
    private Set<LXP> bride_births = new TreeSet<>();            // the birth records for all the grooms in this marriage
    private Set<LXP> marriage_records = new TreeSet<>();        // the marriage records for all the bride/grooms in this marriage.
    private Set<LXP> grooms_child_births = new TreeSet<>();     // the birth records for all the children of the groom in this marriage
    private Set<LXP> brides_child_births = new TreeSet<>();     // the birth records for all the children of the bride in this marriage

    // Link Adders

    public void addBirthMarriageGroomLink(Link link) {
       groom_links.add(link);
    }

    public void addBirthMarriageBrideLink(Link link) {
        bride_links.add(link);
    }

    public void addBirthFathersMarriageLink(Link link) {
        grooms_children_links.add(link);
    }

    public void addBirthMothersMarriageLink(Link link) {
        brides_children_links.add(link);
    }

    // creators - turns links into Births, Marriages etc.

    public void createMarriagesFromGroomLinks() throws BucketException, RepositoryException {
        for (Link link : groom_links) {
            groom_births.add(link.getRecord1().getReferend());
            marriage_records.add(link.getRecord2().getReferend());
        }
    }

    public void createMarriagesFromBrideLinks() throws BucketException, RepositoryException {
        for (Link link : bride_links) {
            bride_births.add(link.getRecord1().getReferend());
            marriage_records.add(link.getRecord2().getReferend());
        }
    }

    public void createMarriagesFromBirthAndFathersMarriageLinks() throws BucketException, RepositoryException {
        for (Link link : grooms_children_links) {
            brides_child_births.add(link.getRecord1().getReferend());
            marriage_records.add(link.getRecord2().getReferend());
        }
    }

    public void createMarriagesFromBirthAndMothersMarriageLinks() throws BucketException, RepositoryException {
        for (Link link : brides_children_links) {
            grooms_child_births.add(link.getRecord1().getReferend());
            marriage_records.add(link.getRecord2().getReferend());
        }
    }

    // Vital event record selectors

    public Set<LXP> getBirthSiblings() {
        Set<LXP> result = new TreeSet<>();
        result.addAll(grooms_child_births);
        result.addAll(brides_child_births);
        return result;
    }

    public Set<LXP> getBirthsOfGrooms() {
        return groom_births;
    }

    public Set<LXP> getBrideBirths() {
        return bride_births;
    }

    public Set<LXP> getMarriageRecords() {
        return marriage_records;
    }
}
