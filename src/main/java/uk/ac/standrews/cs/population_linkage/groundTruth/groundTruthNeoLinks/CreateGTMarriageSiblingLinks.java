/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

/*
 * Establishes ground truth links in Neo4J for Umea data set
 *
 * @author al
 */

public class CreateGTMarriageSiblingLinks extends GTLinksBase {

    public CreateGTMarriageSiblingLinks(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTMarriageSiblingLinks gt_link_creator = new CreateGTMarriageSiblingLinks(bridge);
            gt_link_creator.timeQuery( "Marriage sibling links", BRIDE_GROOM_SIBLING, GROOM_BRIDE_SIBLING, GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING );
            System.out.println("Finished creating GT links");
        } catch (Exception e) {
            System.out.println("Fatal exception during GT linkage creation");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            System.exit(0);
        }
    }
}