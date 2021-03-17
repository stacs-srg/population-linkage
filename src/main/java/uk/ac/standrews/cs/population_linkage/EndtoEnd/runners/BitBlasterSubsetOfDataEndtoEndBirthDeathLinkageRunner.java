/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.EndtoEnd.runners;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageQuality;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthGroomIdentityLinkageRecipe.trueMatch;

public class BitBlasterSubsetOfDataEndtoEndBirthDeathLinkageRunner extends BitBlasterLinkageRunner {

    private static final int NUMBER_OF_BIRTHS = 10000;
    private static final int EVERYTHING = Integer.MAX_VALUE;


    @Override
    public LinkageResult link(boolean pre_filter, boolean persist_links, boolean evaluate_quality, int numberOfGroundTruthTrueLinks, int prefilterRequiredFields, boolean generateMapOfLinks, boolean reverseMap) throws BucketException {

        // NOTE - cannot use numberOfGroundTruthTrueLinks - not been initialised properly.

        System.out.println("Adding records into linker @ " + LocalDateTime.now().toString());

        if (!pre_filter) {
            throw new RuntimeException("This code only works with filtering - need to fix selection of sub region");
        }

        // This is alternative to the code in LinkageRunner which requires the whole set to be manifested.
        // This only manifests the first REQUIRED fields.

        ArrayList<LXP> filtered_birth_records = filter(prefilterRequiredFields, NUMBER_OF_BIRTHS, linkageRecipe.getStoredRecords(), linkageRecipe.getLinkageFields());
        ArrayList<LXP> filtered_death_records = filter(prefilterRequiredFields, EVERYTHING, linkageRecipe.getQueryRecords(), linkageRecipe.getQueryMappingFields()); // do not filter these - will reduce what we can find otherwise!

        linker.addRecords(filtered_birth_records, filtered_death_records);

        System.out.println("Records added records to linker @ " + LocalDateTime.now().toString());

        Iterable<Link> links = linker.getLinks();

        System.out.println("Entering persist and evaluate loop @ " + LocalDateTime.now().toString());

        int tp = 0;
        int fp = 0;
        int unknown = 0;

        for (Link link : links) {

            LXP birth = link.getRecord1().getReferend();  // birth
            LXP death = link.getRecord2().getReferend();  // death
            final LinkStatus linkStatus = trueMatch(birth, death);

            if ( isViable( birth,death ) ) {
                switch (linkStatus) {
                    case TRUE_MATCH:
                        tp++;
                        break;
                    case NOT_TRUE_MATCH:
                        fp++;
                        break;
                    default:
                        unknown++;
                }
                procesViableLink(link);
            }
        }

        System.out.println( "Num GT true links = " + numberOfGroundTruthTrueLinks );
        int fn = numberOfGroundTruthTrueLinks - tp;
        LinkageQuality lq = new LinkageQuality(tp, fp, fn);
        lq.print(System.out);

        return null; // TODO FIX THIS
    }

    private void procesViableLink(Link link) {
    }

    private boolean isViable(LXP birth, LXP death) {
        RecordPair rp = new RecordPair( birth,death,0.0 );  // TODO this is mad packaging in a RecordPair unused with a distance unused - refactor.
        return BirthDeathIdentityLinkageRecipe.isViable( rp );
    }

}
