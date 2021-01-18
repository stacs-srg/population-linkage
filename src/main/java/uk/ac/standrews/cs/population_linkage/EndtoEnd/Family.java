package uk.ac.standrews.cs.population_linkage.EndtoEnd;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Family {

    private List<Link> birth_links = new ArrayList<>();
    private Set<LXP> births = new TreeSet<>();

    public void addSiblingBirth(Link link) {
        birth_links.add(link);
    }

    public List<Link> getBirthLinks() {
        return birth_links;
    }

    public Set<LXP> getBirthSiblings() {
        return births;
    }

    public void createSiblings() throws BucketException {
        for (Link link : birth_links) {
            births.add(link.getRecord1().getReferend());
            births.add(link.getRecord2().getReferend());
        }
    }
}
