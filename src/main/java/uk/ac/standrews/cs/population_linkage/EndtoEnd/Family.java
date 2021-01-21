package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Family {

    private List<Link> birth_links = new ArrayList<>();                     // All the links between children in this family
    private List<Link> family_members_marriage_links = new ArrayList<>();   // All the links between the children in this family and those childrens marriages.
    private List<Link> parents_marriage_links = new ArrayList<>();          // All the links between the children in this family and their parents marriages.

    private Set<LXP> births = new TreeSet<>();                      // the birth records for all babies in this family.
    private Set<LXP> family_members_marriages = new TreeSet<>();    // the marriage records for all babies in this family.
    private Set<LXP> parents_marriages = new TreeSet<>();           // the marriage records for the parents of this family.

    // Link Adders

    public void addSiblingBirth(Link link) {
        birth_links.add(link);
    }

    public void addMarriageBabyAsGroom(Link link) {
        family_members_marriage_links.add(link);
    }

    public void addParentsMarriage(Link link) {
        parents_marriage_links.add(link);
    }

    // creators - turns links into Births, Marriages etc.

    public void createSiblings() throws BucketException {
        for (Link link : birth_links) {
            births.add(link.getRecord1().getReferend());
            births.add(link.getRecord2().getReferend());
        }
    }
    public void createMarriageGrooms() throws BucketException {
        for (Link link : family_members_marriage_links) {
            births.add(link.getRecord1().getReferend());
            family_members_marriages.add(link.getRecord2().getReferend());
        }
    }

    public void createParentsMarriages() throws BucketException {
        for (Link link : parents_marriage_links) {
            births.add(link.getRecord1().getReferend());                // TODO <<<<<<<<<<<<<<<<<<<<< this will add them twice if we have added for births - rationalise/merge later ********
            parents_marriages.add(link.getRecord2().getReferend());
        }
    }

    // Vital event record selectors

    public Set<LXP> getBirthSiblings() {
        return births;
    }

    public Set<LXP> getGroomMarriages() {
        return family_members_marriages;
    }

    public Set<LXP> getParentsMarriages() { return parents_marriages; }
}
