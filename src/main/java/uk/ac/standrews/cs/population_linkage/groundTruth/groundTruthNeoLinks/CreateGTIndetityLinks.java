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

public class CreateGTIndetityLinks extends GTLinksBase {

    public CreateGTIndetityLinks(NeoDbCypherBridge bridge) {
        super(bridge);
    }

    /**
     * This creates all the GT neo4J links (relationships) from the information stored in the records
     */
    private void createGTLinks() {
//        timeQuery( "BirthOwnDeath identity", BIRTH_DEATH_IDENTITY,DEATH_BIRTH_IDENTITY );
//        timeQuery( "BirthOwnMarriage identity", BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY );
//        timeQuery( "DeathOwnMarriage identity", DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY );

//        timeQuery( "Birth-ParentsMarriage identity", BIRTH_PARENTS_MARRIAGE );
//        timeQuery( "FatherGroom/MotherBride identity", FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY );
//        timeQuery( "Mother/FatherOwnBirth identity", MOTHER_OWNBIRTH_IDENTITY, FATHER_OWNBIRTH_IDENTITY );

        // ok to here

        timeQuery( "Half sibling marriages", BRIDE_GROOM_HALF_SIBLING );

       // timeQuery( "Half sibling marriages", BRIDE_GROOM_HALF_SIBLING, GROOM_BRIDE_HALF_SIBLING, GROOM_GROOM_HALF_SIBLING, BRIDE_BRIDE_HALF_SIBLING );

       // GROOM_BRIDE_SIBLING, GROOM_GROOM_SIBLING, BRIDE_BRIDE_SIBLING ); // HEAP ERROR

//         timeQuery( "Sibling links",BIRTH_BIRTH_SIBLING, DEATH_DEATH_SIBLING, BIRTH_DEATH_SIBLING ); // HEAP ERROR
//         timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING  ); // HEAP ERROR
//
//        timeQuery( "Sibling links",BIRTH_BIRTH_SIBLING, DEATH_DEATH_SIBLING, BIRTH_DEATH_SIBLING ); // HEAP ERROR
//        timeQuery( "Half-sibling links",BIRTH_BIRTH_HALF_SIBLING, DEATH_DEATH_HALF_SIBLING, BIRTH_DEATH_HALF_SIBLING  ); // HEAP ERROR
    }


    public static void main(String[] args) {
        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            CreateGTIndetityLinks gt_link_creator = new CreateGTIndetityLinks(bridge);
            gt_link_creator.timeQuery("BirthOwnDeath identity", BIRTH_DEATH_IDENTITY, DEATH_BIRTH_IDENTITY);
            gt_link_creator.timeQuery("BirthOwnMarriage identity", BIRTH_GROOM_IDENTITY, BIRTH_BRIDE_IDENTITY);
            gt_link_creator.timeQuery("DeathOwnMarriage identity", DEATH_GROOM_IDENTITY, DEATH_BRIDE_IDENTITY);
            gt_link_creator.timeQuery("Birth-ParentsMarriage identity", BIRTH_PARENTS_MARRIAGE);
            gt_link_creator.timeQuery("FatherGroom/MotherBride identity", FATHER_GROOM_IDENTITY, MOTHER_BRIDE_IDENTITY);
            gt_link_creator.timeQuery("Mother/FatherOwnBirth identity", MOTHER_OWNBIRTH_IDENTITY, FATHER_OWNBIRTH_IDENTITY);
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