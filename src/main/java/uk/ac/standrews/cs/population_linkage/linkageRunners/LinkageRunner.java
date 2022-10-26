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
package uk.ac.standrews.cs.population_linkage.linkageRunners;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.helpers.MemoryLogger;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.searchStructures.SearchStructureFactory;
import uk.ac.standrews.cs.population_linkage.supportClasses.*;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public abstract class LinkageRunner {

    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 100;
    protected LinkageRecipe linkage_recipe;

    public LinkageResult run(LinkageRecipe linkage_recipe,
                             MakePersistent make_persistent,
                             boolean evaluateQuality, boolean persistLinks) throws Exception {

        this.linkage_recipe = linkage_recipe;
        MemoryLogger.update();

        try( Linker linker = getLinker(linkage_recipe) ) {
            linkage_recipe.setCacheSizes(LinkageConfig.BIRTH_CACHE_SIZE, LinkageConfig.DEATH_CACHE_SIZE, LinkageConfig.MARRIAGE_CACHE_SIZE);
            int numberOGroundTruthLinks = 0;
            MemoryLogger.update();
            addRecords(linker);
            LinkageResult result = link(linker, make_persistent, evaluateQuality, numberOGroundTruthLinks, persistLinks);
            return result;
        }
    }

    public LinkageResult listsRun(LinkageRecipe linkage_recipe,
                                  MakePersistent make_persistent,
                                  boolean evaluateQuality, boolean persistLinks, boolean isIdentityLinkage ) throws Exception {

        this.linkage_recipe = linkage_recipe;
        MemoryLogger.update();

        try( Linker linker = getLinker(linkage_recipe) ) {
            linkage_recipe.setCacheSizes(LinkageConfig.BIRTH_CACHE_SIZE, LinkageConfig.DEATH_CACHE_SIZE, LinkageConfig.MARRIAGE_CACHE_SIZE);
            int numberOGroundTruthLinks = 0;
            MemoryLogger.update();
            LinkageResult result = linkLists(linker, make_persistent, evaluateQuality, numberOGroundTruthLinks, persistLinks, isIdentityLinkage);
            return result;
        }
    }

    public LinkageResult investigateRun(LinkageRecipe linkage_recipe,
                                        MakePersistent make_persistent,
                                        boolean evaluateQuality, boolean persistLinks, boolean isIdentityLinkage, NeoDbCypherBridge
        bridge) throws Exception {

        this.linkage_recipe = linkage_recipe;
        MemoryLogger.update();

        try( Linker linker = getLinker(linkage_recipe) ) {
            linkage_recipe.setCacheSizes(LinkageConfig.BIRTH_CACHE_SIZE, LinkageConfig.DEATH_CACHE_SIZE, LinkageConfig.MARRIAGE_CACHE_SIZE);
            int numberOGroundTruthLinks = 0;
            MemoryLogger.update();
            LinkageResult result = investigatelinkLists(linker, make_persistent, evaluateQuality, numberOGroundTruthLinks, persistLinks, isIdentityLinkage, bridge);
            return result;
        }
    }

    public LinkageResult printLinksNonLinksRun(LinkageRecipe linkage_recipe,
                                               MakePersistent make_persistent,
                                               boolean evaluateQuality, boolean persistLinks, boolean isIdentityLinkage, NeoDbCypherBridge bridge) throws Exception {

        this.linkage_recipe = linkage_recipe;
        MemoryLogger.update();

        try( Linker linker = getLinker(linkage_recipe) ) {
            linkage_recipe.setCacheSizes(LinkageConfig.BIRTH_CACHE_SIZE, LinkageConfig.DEATH_CACHE_SIZE, LinkageConfig.MARRIAGE_CACHE_SIZE);
            int numberOGroundTruthLinks = 0;
            MemoryLogger.update();
            LinkageResult result = printLinksNonLinks(linker, make_persistent, evaluateQuality, numberOGroundTruthLinks, persistLinks, isIdentityLinkage, bridge);
            return result;
        }
    }

    public abstract void addRecords(Linker linker);

    protected abstract LinkageResult printLinksNonLinks(Linker linker, MakePersistent make_persistent, boolean evaluateQuality, int numberOGroundTruthLinks, boolean persistLinks, boolean isIdentityLinkage, NeoDbCypherBridge bridge) throws Exception;

    protected abstract LinkageResult investigatelinkLists(Linker linker, MakePersistent make_persistent, boolean evaluateQuality, int numberOGroundTruthLinks, boolean persistLinks, boolean isIdentityLinkage, NeoDbCypherBridge bridge) throws Exception;

    public abstract LinkageResult link(Linker linker, MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persist_links) throws Exception;

    public abstract LinkageResult linkLists(Linker linker, MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persistLinks, boolean isIdentityLinkage) throws Exception;

    protected LinkageQuality getLinkageQuality(boolean evaluate_quality, long numberOfGroundTruthTrueLinks, long tp, long fp) {
        if(evaluate_quality) {
            if(linkage_recipe.isSymmetric()) {
                // if the linkageRecipe is a dataset to itself (i.e birth-birth) we should not be rewarded or penalised
                // for making the link in both direction - thus divide by two
                tp = tp /2;
                fp = fp /2;
            }
            long fn = numberOfGroundTruthTrueLinks - tp;
            return new LinkageQuality(tp, fp, fn);
        } else {
            return new LinkageQuality("Evaluation not requested");
        }
    }

    protected boolean doesGTSayIsTrue(Link link) {
        try {
            return linkage_recipe.isTrueMatch(
                    link.getRecord1().getReferend(),
                    link.getRecord2().getReferend())
                    .equals(TRUE_MATCH);
        } catch (BucketException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records (TD)", e);
        } catch (RepositoryException e) {
            throw new RuntimeException("Bucket exception from accessing referend - bucket no longer contains expected records (TD)", e);
        }
    }

    ///////////////////////////// I/O /////////////////////////////

    private static String prettyPrint(Duration duration) {

        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }

    ///////////////////////////// Private methods /////////////////////////////

    protected LocalDateTime nextTimeStamp(final LocalDateTime previous_time_stamp, final String step_description) {

        LocalDateTime next = LocalDateTime.now();
        System.out.println(prettyPrint(Duration.between(previous_time_stamp, next)) + " to " + step_description);
        return next;
    }

    private void showLink(Link calculated_link) {

        try {
            LXP person1 = calculated_link.getRecord1().getReferend();
            LXP person2 = calculated_link.getRecord2().getReferend();

            System.out.println("B1: " + person1.getString(Birth.FORENAME) + " " + person1.getString(Birth.SURNAME) + " // "
                    + "B1F: " + person1.getString(Birth.FATHER_FORENAME) + " " + person1.getString(Birth.FATHER_SURNAME) + " " + person1.getString(Birth.FAMILY) + " -> " +
                    "B2: " + person2.getString(Birth.FORENAME) + " " + person2.getString(Birth.SURNAME) + " // " +
                    "B2F: " + person2.getString(Birth.FATHER_FORENAME) + " " + person2.getString(Birth.FATHER_SURNAME) + " " + person2.getString(Birth.FAMILY));

        } catch (Exception e) {}
    }

    protected void printLink(Link link, String classification) {

        try {
            LXP person1 = link.getRecord1().getReferend();
            LXP person2 = link.getRecord2().getReferend();

            System.out.printf("-%s------------------------------------------------------------------------------------------------------------\n", classification);

            for(int i = 0; i < linkage_recipe.getLinkageFields().size(); i++) {
                String r1FieldName = Utilities.getLabels(person1).get(linkage_recipe.getLinkageFields().get(i));
                String r2FieldName = Utilities.getLabels(person2).get(linkage_recipe.getQueryMappingFields().get(i));

                String r1FieldContent = person1.getString(linkage_recipe.getLinkageFields().get(i));
                String r2FieldContent = person2.getString(linkage_recipe.getQueryMappingFields().get(i));

                String isEquals = "≠";
                if(r1FieldContent.equals(r2FieldContent)) isEquals = "=";

                System.out.printf("%30s | %20s |%s| %-20s | %-30s %.2f \n", r1FieldName, r1FieldContent, isEquals, r2FieldContent, r2FieldName, link.getDistance());
            }

            System.out.println("---------------------------------------------------------------------------------------------------------------");

        } catch (Exception ignored) { }
    }

    public abstract Linker getLinker( LinkageRecipe linkageRecipe);

    public abstract LinkageRecipe getLinkageRecipe(final String links_persistent_name, final String source_repository_name, final String results_repository_name, final RecordRepository record_repository);

    public abstract SearchStructureFactory<LXP> getSearchFactory(final LXPMeasure composite_measure);

    protected int getNumberOfProgressUpdates() {
        return DEFAULT_NUMBER_OF_PROGRESS_UPDATES;
    }
}
