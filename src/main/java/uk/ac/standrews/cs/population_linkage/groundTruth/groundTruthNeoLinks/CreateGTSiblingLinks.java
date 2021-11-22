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

public class CreateGTSiblingLinks extends GTLinksBase {

    public CreateGTSiblingLinks(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTSiblingLinks gt_link_creator = new CreateGTSiblingLinks(bridge);
            gt_link_creator.timeQuery( "BirthBith Sibling links",BIRTH_BIRTH_SIBLING );
            gt_link_creator.timeQuery( "DeathDeath Sibling links",DEATH_DEATH_SIBLING );
            gt_link_creator.timeQuery( "BirthDeathSibling links",BIRTH_DEATH_SIBLING);
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