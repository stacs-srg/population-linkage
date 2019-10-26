/*
 * ***********************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 * Copyright 2019 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************
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
