/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.resolver.cluster;

import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BrideBrideSiblingLinkageRecipe;

public class SiblingBrideClusterAllTrianglesResolver extends SiblingBrideClusterOpenTriangleResolver {

    public SiblingBrideClusterAllTrianglesResolver(NeoDbCypherBridge bridge, String source_repo_name, BrideBrideSiblingLinkageRecipe recipe) {
        super( bridge, source_repo_name, recipe );
    }

    public static void main(String[] args) {

        BRIDE_SIBLING_TRIANGLE_QUERY = "MATCH (x:Marriage)-[xy:SIBLING]-(y:Marriage)-[yz:SIBLING]-(z:Marriage)-[zx:SIBLING]-(x:Marriage) return x,y,z,xy,yz";

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BrideBrideSiblingLinkageRecipe linkageRecipe = new BrideBrideSiblingLinkageRecipe(sourceRepo, "10000", BirthSiblingBundleBuilder.class.getName(), bridge);
            SiblingBrideClusterAllTrianglesResolver resolver = new SiblingBrideClusterAllTrianglesResolver( bridge,sourceRepo,linkageRecipe );

            printHeaders();

            for( int min_cluster = 9; min_cluster > 2; min_cluster-- ) {
                for (double hdrt = 2; hdrt < 8; hdrt += 1) {
                    for (double ldrt = 10; ldrt < 40; ldrt += 5) {
                        resolver.resolve(min_cluster, ldrt / 100d, hdrt / 10d);
                    }
                }
            }
 //              resolver.resolve();

        } catch (Exception e) {
            System.out.println( "Exception closing bridge" );
        } finally {
            System.out.println( "Run finished" );
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}
