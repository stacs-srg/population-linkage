package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Siblings {

    private List<Link> birth_links = new ArrayList<>();             // All the links between children in this family
    private Set<LXP> births = new TreeSet<>();                      // the birth records for all babies in this family.

    // creators

    public void createSiblings() throws BucketException {
        for (Link link : birth_links) {
            births.add(link.getRecord1().getReferend());
            births.add(link.getRecord2().getReferend());
        }
    }

    // Vital event record selectors and updaters

    public Set<LXP> getBirthSiblings() {
        return births;
    }

    public List<Link> getBirthLinks() {
        return birth_links;
    }

    public void addSiblingBirth(Link link) {
        birth_links.add(link);
    }

    public void addBirthLinks( List<Link> siblings ) {
        birth_links.addAll(siblings);
    }
}
