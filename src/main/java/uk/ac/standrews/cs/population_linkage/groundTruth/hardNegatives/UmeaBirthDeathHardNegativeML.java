/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.hardNegatives;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.neoStorr.interfaces.IRepository;
import uk.ac.standrews.cs.neoStorr.interfaces.IStore;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.datasets.Umea;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.BirthDeathIdentityLinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.PrintUtils;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.graph.NeoUtil.getByNeoId;

public class UmeaBirthDeathHardNegativeML {

    private static final String true_match_filename = "Umea_BD_true_match";
    private static final String random_negatives_filename = "Umea_BD_random_negatives";
    private static final String hard_negatives_filename = "Umea_BD_hard_negatives";
    private static final String metdata_filename = "Umea_BD_metadata";

    private static final String BIRTH_DEATH_IDENTITY_QUERY = "MATCH (a:Birth)-[r:GROUND_TRUTH_BIRTH_DEATH_IDENTITY]-(b:Death) WHERE a.CHILD_IDENTITY = b.DECEASED_IDENTITY RETURN a,b,r";
    private static final String NON_LINKS_DEATHS_QUERY = "MATCH (x:Death) WHERE NOT (:Birth)-[]-(x) RETURN x LIMIT $number";
    private static final String NON_LINKS_BIRTHS_QUERY = "MATCH (x:Birth) WHERE NOT (:Death)-[]-(x) RETURN x LIMIT $number";

    protected static final String DELIMIT = ",";

    private static final int NNN = 3; // number of nearest neighbours (inc target)

    private final IBucket<Birth> births;
    private final IBucket<Death> deaths;
    private final NeoDbCypherBridge bridge;
    private final NNs nns;

    private final List<Integer> comparison_fields;
    private final List<Integer> comparison_fields2;
    private final PrintUtils pu;

    public UmeaBirthDeathHardNegativeML(NeoDbCypherBridge bridge, String true_match_filename, String random_negatives_filename, String hard_negatives_filename, String metdata_filename) throws RepositoryException, BucketException, IOException {

        IStore store = Store.getInstance();
        IRepository repo = store.getRepository(Umea.REPOSITORY_NAME);

        this.bridge = bridge;
        this.births = repo.getBucket("birth_records", Birth.class);
        this.deaths = repo.getBucket("death_records", Death.class);
        nns = new NNs(births, deaths);

        this.comparison_fields = BirthDeathIdentityLinkageRecipe.LINKAGE_FIELDS;
        this.comparison_fields2 = BirthDeathIdentityLinkageRecipe.SEARCH_FIELDS;

        pu = new PrintUtils(true_match_filename, random_negatives_filename,
                hard_negatives_filename, metdata_filename,
                comparison_fields, comparison_fields2);
    }

    public void processGTLinks() throws BucketException {

        writeHeadersAndMetaData();

        for (Relationship r : getGTLinks(BIRTH_DEATH_IDENTITY_QUERY)) {
            Birth b = getByNeoId(r.startNodeId(), births, bridge);
            Death d = getByNeoId(r.endNodeId(), deaths, bridge);

            // Output the distances of b,d as true positive.
            // Now search for each of the NN identity links for b and d to get hard negatives
            List<Birth> non_link_nn_births = nns.getBirthNNs(b, NNN);
            List<Death> non_link_nn_deaths = nns.getDeathNNs(d, NNN);
            List<Death> random_non_link_deaths = getRandomNonLinkDeaths(b, NNN);
            List<Birth> random_non_link_births = getRandomNonLinkBirths(d, NNN);

// ***** DEBUG *****
//            System.out.println("birth");
//            show(b);
//            System.out.println("death");
//            show(d);
//            System.out.println("++ births nns ++");
//            non_link_nn_births.forEach(n -> show(n));
//            System.out.println("++ deaths nns ++");
//            non_link_nn_deaths.forEach(n -> show(n));
//            System.out.println("++ Deaths randoms ++");
//            random_non_link_deaths.forEach(n -> show(n));
//            System.out.println("++ Births randoms ++");
//            random_non_link_births.forEach(n -> show(n));
//            System.out.println( "--END--");
// ***** DEBUG *****

            pu.printPairDiffType(b, d, pu.true_match_results_writer, LinkStatus.TRUE_MATCH); // the real link
            non_link_nn_deaths.forEach(n -> pu.printPairDiffType(b, n, pu.hard_negatives_results_writer, LinkStatus.NOT_TRUE_MATCH));
            non_link_nn_births.forEach(n -> pu.printPairDiffType(n, d, pu.hard_negatives_results_writer, LinkStatus.NOT_TRUE_MATCH));
            random_non_link_deaths.forEach(n -> pu.printPairDiffType(b, n, pu.random_negatives_results_writer, LinkStatus.NOT_TRUE_MATCH));
            random_non_link_births.forEach(n -> pu.printPairDiffType(n, d, pu.random_negatives_results_writer, LinkStatus.NOT_TRUE_MATCH));
        }
    }

    private void writeHeadersAndMetaData() throws BucketException {
        printMetaData();
        writeHeaders(pu.true_match_results_writer);
        writeHeaders(pu.random_negatives_results_writer);
        writeHeaders(pu.hard_negatives_results_writer);
    }

    private void writeHeaders(PrintWriter pw) throws BucketException {
        LXP b_source_record = births.getInputStream().iterator().next();
        LXP d_source_record = deaths.getInputStream().iterator().next();

        for (final StringMeasure measure : Constants.BASE_MEASURES) {

            final String name = measure.getMeasureName();
            for (int field_selector = 0; field_selector < comparison_fields.size(); field_selector++) {

                String label = name + "." + b_source_record.getMetaData().getFieldName(comparison_fields.get(field_selector)) + "-" +
                        d_source_record.getMetaData().getFieldName(comparison_fields2.get(field_selector));  // measure name concatenated with the name of the field selectors
                pw.print(label);
                pw.print(DELIMIT);

            }
        }

        pw.print("link_non-link");
        pw.print(DELIMIT);

        pw.println();
        pw.flush();
    }

    public void printMetaData() {

        PrintWriter pw = pu.metadata_writer;

        pw.println("Output file created: " + LocalDateTime.now());
        pw.println("Checking distributions of record pair distances using various string similarity measures and thresholds hard negatives");
        pw.println("Dataset: Umea");
        pw.println("LinkageRecipe type: birth-death");
        pw.println("Created by: " + this.getClass().getName());
        pw.flush();
        pw.close();
    }

    /**
     * @param b      a birth record
     * @param number
     * @return some random Deaths that are not links to the birth passed in
     */
    private List<Death> getRandomNonLinkDeaths(Birth b, int number) throws BucketException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("number", number);
        Result query_res = bridge.getNewSession().run(NON_LINKS_DEATHS_QUERY, parameters);
        List<Node> nodes = query_res.list(r -> r.get("x").asNode());
        List<Death> result = new ArrayList<>();
        for (Node node : nodes) {
            long storr_id = node.get("STORR_ID").asLong();
            result.add(deaths.getObjectById(storr_id));
        }
        return result;
    }

    /**
     * @param d      a death record
     * @param number
     * @return some random Births that are not links to the death passed in
     */
    private List<Birth> getRandomNonLinkBirths(Death d, int number) throws BucketException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("number", number);
        Result query_res = bridge.getNewSession().run(NON_LINKS_BIRTHS_QUERY, parameters);
        List<Node> nodes = query_res.list(r -> r.get("x").asNode());
        List<Birth> result = new ArrayList<>();
        for (Node node : nodes) {
            long storr_id = node.get("STORR_ID").asLong();
            result.add(births.getObjectById(storr_id));
        }
        return result;
    }

    private void show(Birth b) {
        System.out.println(b.get(Birth.FORENAME) + "/" + b.get(Birth.SURNAME) + "/" +
                b.get(Birth.MOTHER_FORENAME) + "/" + b.get(Birth.MOTHER_MAIDEN_SURNAME) + "/" +
                b.get(Birth.FATHER_FORENAME) + "/" + b.get(Birth.FATHER_SURNAME) + "/" +
                b.get(Birth.STANDARDISED_ID));
    }

    private void show(Death d) {
        System.out.println(d.get(Death.FORENAME) + "/" + d.get(Death.SURNAME) + "/" +
                d.get(Death.MOTHER_FORENAME) + "/" + d.get(Death.MOTHER_MAIDEN_SURNAME) + "/" +
                d.get(Death.FATHER_FORENAME) + "/" + d.get(Death.FATHER_SURNAME) + "/" +
                d.get(Death.STANDARDISED_ID));
    }

    private List<Relationship> getGTLinks(String query_string) {
        Result result = bridge.getNewSession().run(query_string);
        return result.list(r -> r.get("r").asRelationship());
    }

    public static void main(String[] args) throws RepositoryException, BucketException {

        try (NeoDbCypherBridge bridge = new NeoDbCypherBridge()) {
            UmeaBirthDeathHardNegativeML hn = new UmeaBirthDeathHardNegativeML(bridge, true_match_filename, random_negatives_filename, hard_negatives_filename, metdata_filename);
            hn.processGTLinks();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
