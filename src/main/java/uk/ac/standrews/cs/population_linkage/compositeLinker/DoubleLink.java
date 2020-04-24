/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.compositeLinker;

import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

public class DoubleLink {

    final Link a;
    final Link b;
    final String linkType;

    public DoubleLink(Link a, Link b, String linkType) {
        this.a = a;
        this.b = b;
        this.linkType = linkType;
    }

    public Link directLink() throws BucketException, PersistentObjectException {
            return new Link(a.getRecord1().getReferend(), a.getRole1(), b.getRecord2().getReferend(), b.getRole2(),
                    (float) (a.getConfidence() * b.getConfidence()), linkType, a.getDistance() * b.getDistance(),
                    a.getProvenance().toString() + " | " + b.getProvenance().toString());
    }

}
