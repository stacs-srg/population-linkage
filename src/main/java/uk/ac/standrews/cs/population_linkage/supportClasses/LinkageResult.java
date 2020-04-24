/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.supportClasses;

import java.util.Collection;
import java.util.Map;

public class LinkageResult {

    private Map<String, Collection<Link>> mapOfLinks = null;
    private LinkageQuality linkageQuality = null;

    public LinkageResult(LinkageQuality linkageQuality, Map<String, Collection<Link>> mapOfLinks) {
        this.mapOfLinks = mapOfLinks;
        this.linkageQuality = linkageQuality;
    }

    public LinkageResult(LinkageQuality linkageQuality) {
        this.linkageQuality = linkageQuality;
    }

    public Map<String, Collection<Link>> getMapOfLinks() {
        if(mapOfLinks == null) {
            throw new MapNotRequestedException();
        }
        return mapOfLinks;
    }

    public LinkageQuality getLinkageQuality() {
        return linkageQuality;
    }
}
