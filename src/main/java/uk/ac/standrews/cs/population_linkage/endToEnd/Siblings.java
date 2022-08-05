/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
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

public class Siblings {

    private List<Link> birth_links = new ArrayList<>();             // All the links between children in this family
    private Set<LXP> births = new TreeSet<>();                      // the birth records for all babies in this family.

    // creators

    public void createSiblings() throws BucketException, RepositoryException {
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
