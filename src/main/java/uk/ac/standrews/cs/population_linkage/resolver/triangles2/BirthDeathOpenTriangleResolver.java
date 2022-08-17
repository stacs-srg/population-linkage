/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
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

    protected static final String TRIANGLE_QUERY = "MATCH (x:Birth)-[xy:DEATH]-(y:Death)-[yz:SIBLING]-(z:Death) WHERE NOT (z)-[:SIBLING]-(x) return x,y,z,xy,yz";

    private static final double SIBLING_SET_SIMILARITY_THRESHOLD = 0.03;
    public static double LOW_DISTANCE_MATCH_THRESHOLD = 0.05;

    private static Jaccard jaccard = new Jaccard();

    private final NeoDbCypherBridge bridge;
    private final IBucket births;
    private final IBucket deaths;

    private final StringMeasure base_measure;
    private final LXPMeasure composite_measure;

    private static final String BB_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Birth) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String BD_GET_SIBLINGS = "MATCH (a:Birth)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";
    private static final String DD_GET_SIBLINGS = "MATCH (a:Death)-[r:SIBLING]-(b:Death) WHERE a.STANDARDISED_ID = $standard_id_from RETURN b";

    public BirthDeathOpenTriangleResolver(NeoDbCypherBridge bridge, String source_repo_name, BirthSiblingLinkageRecipe recipe) {
        this.bridge = bridge;
        RecordRepository record_repository = new RecordRepository(source_repo_name);
        this.births = record_repository.getBucket("birth_records");
        this.deaths = record_repository.getBucket("death_records");
        this.base_measure = Constants.JENSEN_SHANNON;
        this.composite_measure = getCompositeMeasure(recipe);
    }

    protected LXPMeasure getCompositeMeasure(final LinkageRecipe linkageRecipe) {
        return new SumOfFieldDistances(base_measure, linkageRecipe.getLinkageFields());
    }

    private void resolve() {
        Stream<OpenTriangle2> oddballs = findOpenTriangles();
        oddballs.forEach(this::process);
        //printResults();
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

            final LXP lxp_x = (LXP) births.getObjectById(getStorrId(x));
            final LXP lxp_y = (LXP) deaths.getObjectById(getStorrId(y));
            final LXP lxp_z = (LXP) deaths.getObjectById(getStorrId(z));

            final long x_id = getNeoId(open_triangle.x);

            final String std_id_x = lxp_x.getString(Birth.STANDARDISED_ID);
            final String std_id_y = lxp_y.getString(Death.STANDARDISED_ID);
            final String std_id_z = lxp_z.getString(Death.STANDARDISED_ID);

            final double xy_dist = getDistance( open_triangle.xy );
            final double yz_dist = getDistance( open_triangle.yz );

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

            // At least one of the following hypotheses are not true:
            /* 1. x is the birth record of death record for y */ boolean hypothesis_1 = probablyBirthIdDeath( lxp_x, lxp_y, std_id_x,std_id_y,xy_dist );
            /* 2. y and z are siblings */ boolean hypothesis_2 = probablyDeathSiblingDeath( lxp_y, lxp_z, std_id_y,std_id_z,yz_dist );
            /* 3. x and z are siblings */ boolean hypothesis_3 = probablyBirthSiblingDeath( lxp_x, lxp_z, std_id_x,std_id_z );

            if (!x_f_id.equals("") && ! y_f_id.equals("") && ! z_f_id.equals("")) {
                if (exactlyOneFalse(hypothesis_1, hypothesis_2, hypothesis_3)) {
                    System.out.print("Found exactly one hypothsis false: ");
                    if (! hypothesis_1) System.out.println("probablyBirthDeathMatch is false");
                    if (! hypothesis_2) System.out.println("probablyDeathsOfSiblings is false");
                    if (! hypothesis_3) System.out.println("probablyBirthDeathMatch is false");

                    System.out.println("GT: fids: " + x_f_id + " / " + y_f_id + " / " + z_f_id);
                }
            }
            // otherwise do nothing!

        } catch (BucketException e) {
            e.printStackTrace();
        }
    }

    private boolean probablyDeathSiblingDeath(LXP death1_lxp, LXP death2_lxp, String death1_std_id, String death2_std_id, double dist) throws BucketException {
        if( dist < LOW_DISTANCE_MATCH_THRESHOLD ) {
            return true;
        }
        if( /* haveDeathParentsInCommon( death1_lxp,death2_lxp ) || */
                haveDeathSiblingsInCommon( death1_std_id,death2_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean probablyBirthSiblingDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id) throws BucketException {
        if( /* haveDeathParentsInCommon( death1_lxp,death2_lxp ) || */
                haveBirthDeathSiblingsInCommon( birth_std_id,death_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean probablyBirthIdDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id, double dist) throws BucketException {
        if( dist < LOW_DISTANCE_MATCH_THRESHOLD ) { // was this a highly matching link - might be provenance or confidence etc.
            return true;
        }
        if( /* haveBirthDeathParentsInCommon( birth_lxp,death_lxp ) || */
        haveBirthDeathSiblingsInCommon( birth_std_id,death_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean probablyBirthIdDeath(LXP birth_lxp, LXP death_lxp, String birth_std_id, String death_std_id) throws BucketException { // the open side of the triangle - no point in checking dist - it is above threshold
        if( /* haveBirthDeathParentsInCommon( birth_lxp,death_lxp ) ||  */
                haveBirthDeathSiblingsInCommon( birth_std_id,death_std_id ) ) {
            return true;
        }
        return false; // cannot say for sure
    }

    private boolean haveDeathSiblingsInCommon(String adeath1_std_id, String adeath2_std_id) throws BucketException {
        return similarityOfDirectDDSiblings( adeath1_std_id,adeath2_std_id ) < SIBLING_SET_SIMILARITY_THRESHOLD;
    }

    private boolean haveBirthDeathSiblingsInCommon(String adeath1_std_id, String adeath2_std_id) throws BucketException {
        return similarityOfDirectBDSiblings( adeath1_std_id,adeath2_std_id ) < SIBLING_SET_SIMILARITY_THRESHOLD;
    }

//    private boolean haveDeathParentsInCommon(LXP adeath1_std_id, LXP adeath2_std_id) {
//
//    }
//
//    private boolean haveBirthDeathParentsInCommon(LXP adeath1_std_id, LXP adeath2_std_id) throws BucketException {
//        return false;
//    }

    private boolean exactlyOneFalse(boolean A, boolean B, boolean C) {
        if( A ) {
            return B != C;
        } else {
            return B && C;
        }
    }

    /**
     *
     * @param bridge
     * @param query_string
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
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return jaccard distance between the sets of ids of siblings
     * @throws BucketException
     */
    private double similarityOfDirectBBSiblingsB(String std_id1, String std_id2) throws BucketException {
        Set<Long> siblings_of_1 = getBBSiblingIds(std_id1);
        Set<Long> siblings_of_2 = getBBSiblingIds(std_id2);

        return jaccard.distance(siblings_of_1,siblings_of_2);
    }

    /**
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return jaccard distance between the sets of ids of siblings
     * @throws BucketException
     */
    private double similarityOfDirectBDSiblings(String std_id1, String std_id2) throws BucketException {
        Set<Long> siblings_of_1 = getBDSiblingIds(std_id1);
        Set<Long> siblings_of_2 = getBDSiblingIds(std_id2);

        if( siblings_of_1.size() == 0 && siblings_of_2.size() == 0) { // TODO copy code elsewhere
            return 0;
        }
        double d = jaccard.distance(siblings_of_1,siblings_of_2);
        return d;
    }

    /**
     * std_id_x - the standard id of node x
     * std_id_z - the standard id of node z
     * @return jaccard distance between the sets of ids of siblings
     * @throws BucketException
     */
    private double similarityOfDirectDDSiblings(String std_id1, String std_id2) throws BucketException {
        Set<Long> siblings_of_1 = getDDSiblingIds(std_id1);
        Set<Long> siblings_of_2 = getDDSiblingIds(std_id2);

        return jaccard.distance(siblings_of_1,siblings_of_2);
    }

    private Set<Long> getBBSiblingIds(String std_id) throws BucketException {
        return new HashSet<>(getSiblings(bridge, BB_GET_SIBLINGS, std_id));
    }

    private Set<Long> getBDSiblingIds(String std_id) throws BucketException {
        return new HashSet<>(getSiblings(bridge, BD_GET_SIBLINGS, std_id));
    }

    private Set<Long> getDDSiblingIds(String std_id) throws BucketException {
        return new HashSet<>(getSiblings(bridge, DD_GET_SIBLINGS, std_id));
    }


    public static void main(String[] args) {

        String sourceRepo = args[0]; // e.g. umea

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge();) {

            BirthSiblingLinkageRecipe linkageRecipe = new BirthSiblingLinkageRecipe(sourceRepo, BirthSiblingBundleBuilder.class.getName(), bridge);
            BirthDeathOpenTriangleResolver resolver = new BirthDeathOpenTriangleResolver(bridge, sourceRepo, linkageRecipe); // this class
            resolver.resolve();

        } catch (Exception e) {
            System.out.println("Exception closing bridge");
        } finally {
            System.out.println("Run finished");
            System.exit(0); // Make sure it all shuts down properly.
        }
    }

}