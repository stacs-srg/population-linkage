/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import java.util.Collection;
import java.util.Map;

public class LinkageResult {

    private Map<String, Collection<Link>> mapOfLinks = null;
    private Map<String, LinkageQuality> linkageEvaluations;

    public LinkageResult(Map<String, LinkageQuality> linkageEvaluations, Map<String, Collection<Link>> mapOfLinks) {
        this.mapOfLinks = mapOfLinks;
        this.linkageEvaluations = linkageEvaluations;
    }

    public LinkageResult(Map<String, LinkageQuality> linkageEvaluations) {
        this.linkageEvaluations = linkageEvaluations;
    }

    public Map<String, Collection<Link>> getMapOfLinks() {
        if(mapOfLinks == null) {
            throw new MapNotRequestedException();
        }
        return mapOfLinks;
    }

    public Map<String, LinkageQuality> getLinkageEvaluations() {
        return linkageEvaluations;
    }
}
