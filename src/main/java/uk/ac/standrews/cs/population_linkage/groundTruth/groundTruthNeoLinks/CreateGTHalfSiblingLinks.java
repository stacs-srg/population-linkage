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

public class CreateGTHalfSiblingLinks extends GTLinksBase {

    public CreateGTHalfSiblingLinks(NeoDbCypherBridge bridge) {
        super(bridge);
    }


//    timeQuery( "Half sibling marriages",BRIDE_GROOM_HALF_SIBLING, GROOM_BRIDE_HALF_SIBLING, GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING);


    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTHalfSiblingLinks gt_link_creator = new CreateGTHalfSiblingLinks(bridge);
            gt_link_creator.timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING);
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