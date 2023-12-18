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
package uk.ac.standrews.cs.population_linkage.resolver.triangles2;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.endToEnd.builders.BirthSiblingBundleBuilder;
import uk.ac.standrews.cs.population_linkage.graph.Query;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthSiblingLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.Jaccard;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.*;
import java.util.stream.Stream;

import static uk.ac.standrews.cs.population_linkage.resolver.triangles2.OpenTriangle2.*;

/**
 * @author al@st-andrews.ac.uk
 * @date 16/8/2022
 * Examines the following situation:
 *
 *                Birth ---Death id --- Death
 *                  |                    |
 *               SIBLING              SIBLING
 *                  |                    |
 *                Birth ---Death id--- Death
 *
 */
public class BirthDeathOpenTriangleResolver {

    /**
     *                x:Birth - Death id - y:Death
     *                      \             /
     *                     NOT         SIBLING
     *                        \        /
     *                         z:Death
     */

    // TODO Move into Query??

    protected static final String TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:ID]-(y:Death)-[yz:SIBLING]-(z:Death) WHERE NOT (z)-[:SIBLING]-(x) return x,y,z,xy,yz";

    // protected static final String TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:ID]-(y:Birth)-[yz:SIBLING]-(z:Birth) WHERE NOT (z)-[:SIBLING]-(x) return x,y,z,xy,yz";

    private static final String GET_BIRTH_SIBLINGS = "MATCH (a)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String GET_DEATH_SIBLINGS = "MATCH (a)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String GET_MARRIAGE_SIBLINGS = "MATCH (a)-[r:SIBLING]-(b:Marriage) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private static final String BB_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String BD_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String DD_GET_SIBLINGS = "MATCH (a:Death)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    private static final String UPDATE_RELATIONSHIP_QUERY = "MATCH (a)-[r]-(b) WHERE id(r) = $id SET r += {link_refuted:TRUE,link_refuted_by:$provenance} return r";

    private static final double SIBLING_SET_SIMILARITY_THRESHOLD = 0.2;
    public static double LOW_DISTANCE_MATCH_THRESHOLD = 0.05;

    private static Jaccard jaccard = new Jaccard();

    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final IBucket deaths;

    private final StringMeasure base_measure;
    private final LXPMeasure composite_measure;

    private int correct_count = 0;
    private int error_count = 0;
    private int harmless_count = 0;
    private int ambiguous_count = 0;

    public BirthDeathOpenTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, BirthSiblingLinkageRecipe recipe) {
        this.bridge = bridge;
        RecordRepository record_repository = new RecordRepository(source_repo_name);
        this.births = record_repository.getBucket("birth_records");
        this.deaths = record_repository.getBucket("death_records");
        this.base_measure = Constants.JENSEN_SHANNON;
        this.composite_measure = getCompositeMeasure(recipe);
    }

    protected LXPMeasure getCompositeMeasure(final LinkageRecipe linkageRecipe) {
        return new LXPMeasure(linkageRecipe.getLinkageFields(), linkageRecipe.getQueryMappingFields(), base_measure);
    }

    private void resolve() {
        Stream<OpenTriangle2> oddballs = findOpenTriangles();
        oddballs.forEach(this::process);
        reportResults();
    }

    private void reportResults() {
        System.out.println( "correct_count = " + correct_count );
        System.out.println( "error_count = " + error_count );
        System.out.println( " harmless_count = " + harmless_count );
        System.out.println( " ambiguous_count = " + ambiguous_count );
    }

    /**
     * @return a Stream of OpenTriangle2
     */
    public Stream<OpenTriangle2> findOpenTriangles() {
        Result result = bridge.getNewSession().run(TRIANGLE_QUERY); // returns x,y,z where x and y and z are connected and zx is not.
        return result.stream().map(r -> {
                    return new OpenTriangle2(
                            ((Node) r.asMap().get("x")),
                            ((Node) r.asMap().get("y")),
                            ((Node) r.asMap().get("z")),
                            ((Relationship) r.asMap().get("xy")),
                            ((Relationship) r.asMap().get("yz")) );
                }
        );
    }

    private void process(OpenTriangle2 open_triangle) {
        try {
            final Node x = open_triangle.x;
            final Node y = open_triangle.y;
            final Node z = open_triangle.z;

            Relationship xy = open_triangle.xy;
            Relationship yz = open_triangle.yz;

            final LXP lxp_x = (LXP) births.getObjectById(getStorrId(x));
            final LXP lxp_y = (LXP) deaths.getObjectById(getStorrId(y));
            final LXP lxp_z = (LXP) deaths.getObjectById(getStorrId(z));

            final long x_id = getNeoId(open_triangle.x);

            final String std_id_x = lxp_x.getString(Birth.STANDARDISED_ID);
            final String std_id_y = lxp_y.getString(Death.STANDARDISED_ID);
            final String std_id_z = lxp_z.getString(Death.STANDARDISED_ID);

            final double xy_dist = getDistance( xy );
            final double yz_dist = getDistance( yz );

            String x_f_id = lxp_x.getString(Birth.FATHER_IDENTITY);
            String y_f_id = lxp_y.getString(Death.FATHER_IDENTITY);
            String z_f_id = lxp_z.getString(Death.FATHER_IDENTITY);

            /**
             *                x:Birth - Death id - y:Death
             *                      \             /
             *                     NOT         SIBLING
             *                        \        /
             *                         z:Death
             */

            /* 1. x is the birth record of death record for y */ boolean hypothesis_1 = probablyBirthIdDeath( lxp_x, lxp_y, std_id_x,std_id_y,xy_dist );
            /* 2. y and z are siblings */ boolean hypothesis_2 = probablyDeathSiblingDeath( lxp_y, lxp_z, std_id_y,std_id_z,yz_dist );
            /* 3. x and z NOT are siblings */ boolean hypothesis_3 = probablyBirthSiblingDeath( lxp_x, lxp_z, std_id_x,std_id_z );

            // We are interested in exactly one of these being wrong otherwise we cannot reason about them.

            if (!x_f_id.equals("") && ! y_f_id.equals("") && ! z_f_id.equals("")) {
                 if (allTrue(hypothesis_1, hypothesis_2, hypothesis_3)) {
                     reportAndCount(x_f_id, z_f_id);
                     createNewBDSiblingLink(std_id_x,std_id_z,this.getClass().getSimpleName()); // Create a new link between x and z
                 } else if( exactlyOneFalse(hypothesis_1, hypothesis_2, hypothesis_3)) {
                    if( ! hypothesis_1 ) {
                        annotateSiblingLink( xy,this.getClass().getSimpleName() ); // annotate the link between x and y as possibly incorrect.
                        reportAndCount(x_f_id,y_f_id);
                    } else if( ! hypothesis_2 ) {
                        annotateSiblingLink( yz,this.getClass().getSimpleName() ); // annotate the link between between y and z as possibly incorrect.
                        reportAndCount(y_f_id,z_f_id);
                    } else if( ! hypothesis_3 ) {
                        // there was no link there and still no evidence for it - do nothing
                        boolean correct = ! x_f_id.equals(z_f_id);
//                        System.out.println( ">>>>> No evidence for B-D link between x and z " + correct + " harmless" );
                        if( correct ) {
                            correct_count = correct_count  + 1;
                        } else {
                            harmless_count = harmless_count + 1;
                        }
                    }
                } else {
                     // situation is ambiguous - do nothing
//                    System.out.println( ">>>>> 2 hypotheses are false: BidD " + hypothesis_1 + " DDSib " + hypothesis_2 + " BDSib " + hypothesis_3 );
                    if( x_f_id.equals(y_f_id) && y_f_id.equals(z_f_id) ) {
                        ambiguous_count = ambiguous_count + 1;
                    }
                }
            }

        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private void createNewBDSiblingLink(String std_id_birth, String std_id_death, String provenance) {
        Query.createDBSiblingReference(
                bridge,
                std_id_death,
                std_id_birth,
                provenance );
    }

    private void annotateSiblingLink(Relationship r, String provenance) {
        Query.updateReference( bridge, r, UPDATE_RELATIONSHIP_QUERY, provenance );
    }

    private void reportAndCount(String a_id, String b_id) {
        boolean correct = a_id.equals(b_id);
        String correct_str = correct ? "Correctly" : "INCORRECTLY";
        if( correct ) {
            correct_count = correct_count  + 1;
        } else {
            error_count = error_count + 1;
        }
    }

    private boolean allTrue(boolean hypothesis_1, boolean hypothesis_2, boolean hypothesis_3) {
        return hypothesis_1 && hypothesis_2 && hypothesis_3;
    }

    private boolean exactlyOneFalse(boolean A, boolean B, boolean C) {
        if( A ) {
            return B != C;
        } else {
            return B && C;
        }
    }

    private boolean probablyDeathSiblingDeath(LXP death1_lxp, LXP death2_lxp, String death1_std_id, String death2_std_id, double dist) throws BucketException {
        if( dist < LOW_DISTANCE_MATCH_THRESHOLD ) {
            return true;
        }
        if( /* haveDeathParentsInCommon( death1_lxp,death2_lxp ) || */
                haveSiblingsInCommon( death1_std_id,death2_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean probablyBirthSiblingDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id) throws BucketException {

        if( /* haveDeathParentsInCommon( death1_lxp,death2_lxp ) || */
                haveSiblingsInCommon( birth_std_id,death_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean probablyBirthIdDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id, double dist) throws BucketException {
        if( dist < LOW_DISTANCE_MATCH_THRESHOLD ) { // was this a highly matching link - might be provenance or confidence etc.
            return true;
        }
        return probablyBirthIdDeath( birth_lxp, death_lxp, birth_std_id, death_std_id);
    }

    private boolean probablyBirthIdDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id) throws BucketException { // the open side of the triangle - no point in checking dist - it is above threshold
        if( /* haveBirthDeathParentsInCommon( birth_lxp,death_lxp ) ||  */
                haveSiblingsInCommon( birth_std_id,death_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    /**
     * @param lxp1_std_id - a birth or death record (doesn't matter which)
     * @param lxp2_std_id - a birth or death record (doesn't matter which)
     * @return true if there is commonality amongst siblings
     * @throws BucketException
     */
    private boolean haveSiblingsInCommon(String lxp1_std_id, String lxp2_std_id) throws BucketException {
        return countCommonSiblings( lxp1_std_id,lxp2_std_id ) >= 2; // arbitrary
    }

    /**
     * @param lxp1_std_id - a birth or death record (doesn't matter which)
     * @param lxp2_std_id - a birth or death record (doesn't matter which)
     * @return number of common siblings
     */
    private int countCommonSiblings(String lxp1_std_id, String lxp2_std_id) throws BucketException {
        Set<String> sibling_names1 = new TreeSet<>();
        Set<String> sibling_names2 = new TreeSet<>();

        getBirthNames( getSiblings(bridge, GET_BIRTH_SIBLINGS, lxp1_std_id),sibling_names1 );
        getDeathNames( getSiblings(bridge, GET_DEATH_SIBLINGS, lxp1_std_id),sibling_names1 );
        //getMarriageNames( getSiblings(bridge, GET_MARRIAGE_SIBLINGS, lxp1_std_id),sibling_names1  );

        getBirthNames( getSiblings(bridge, GET_BIRTH_SIBLINGS, lxp2_std_id),sibling_names2 );
        getDeathNames( getSiblings(bridge, GET_DEATH_SIBLINGS, lxp2_std_id),sibling_names2 );
        //getMarriageNames( getSiblings(bridge, GET_MARRIAGE_SIBLINGS, lxp2_std_id),sibling_names2  );

        return countCommonSiblings( sibling_names1,sibling_names2 );
    }

    /**
     * @param sibling_names1 - a set of sibling names
     * @param sibling_names2 - a set of sibling names
     * @return number of names in common
     */
    private int countCommonSiblings(Set<String> sibling_names1, Set<String> sibling_names2) {
        int size1 = sibling_names1.size();
        int size2 = sibling_names2.size();
        if( size1 == 0 || size2 == 0  ) return 0;
        if( size1 < size2 ) { // swap
            Set<String> temp = sibling_names1;
            sibling_names1 = sibling_names2;
            sibling_names2 = temp;
        }
        int count = 0;
        for( String name : sibling_names1 ) { // the bigger set
            if( sibling_names2.contains( name ) ) {
                count = count +1;
            }
        }
        return count;
    }

    private void getDeathNames(List<Long> storr_sibling_ids, Set<String> sibling_names) throws BucketException {
        for( long storr_id : storr_sibling_ids ) {
            LXP record = (LXP) deaths.getObjectById(storr_id);
            String name = record.getString( Death.FORENAME ) + " " + record.getString( Death.SURNAME );
            if( ! name.equals("") ) { sibling_names.add( name ); }
        }
    }

    private void getBirthNames(List<Long> storr_sibling_ids, Set<String> sibling_names) throws BucketException {
        for( long storr_id : storr_sibling_ids ) {
            LXP record = (LXP) births.getObjectById(storr_id);
            String name = record.getString( Birth.FORENAME ) + " " + record.getString( Birth.SURNAME );
            if( ! name.equals("") ) { sibling_names.add( name ); }
        }
    }

    /**
     *
     * @param bridge
     * @param query_string  - must return bs
     * @param standard_id_from
     * @return a list of STORR ids
     */
    private static List<Long> getSiblings(NeoDbCypherBridge bridge, String query_string, String standard_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return result.list(r -> r.get("b").get("STORR_ID").asLong());
    }

    /**
     * @param bridge
     * @param query_string - must return bs
     * @param standard_id_from
     * @return a list Neo4J nodes
     */
    private static List<Node> getSiblingsAsNodeList(NeoDbCypherBridge bridge, String query_string, String standard_id_from) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(query_string, parameters);
        return result.list(r -> r.get("b").asNode());
    }

    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. umea

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();
            BirthSiblingLinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, "EVERYTHING", BirthSiblingBundleBuilder.class.getName()) ) {
            BirthDeathOpenTriangleResolver resolver = new BirthDeathOpenTriangleResolver(bridge, sourceRepo, linkageRecipe); // this class
            resolver.resolve();
        }
    }

}
