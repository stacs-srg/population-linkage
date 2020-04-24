/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.groundTruth;

import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.groundTruth.umea.*;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.DynamicLXP;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.LXPMetadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.impl.exceptions.KeyNotFoundException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.JSONReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class UmeaGroundTruthTest {

    @Test
    public void birthBrideIdentity() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthBrideIdentity/births.jsn", Birth::new);
        List<LXP> marriage_records = loadRecords("birthBrideIdentity/marriages.jsn", Marriage::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(167253,9687645),
                Arrays.asList(167841,64831));

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(56464,745114));

        assertLinks(birth_records, marriage_records, expected_true_links, expected_unknown_links, UmeaBirthBrideIdentity::trueMatch, false);
    }

    @Test
    public void birthDeathIdentity() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthDeathIdentity/births.jsn", Birth::new);
        List<LXP> death_records = loadRecords("birthDeathIdentity/deaths.jsn", Death::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(167253, 30844578),
                Arrays.asList(167841, 5646511),
                Arrays.asList(6546513, 11111111));

        final List<List<Integer>> expected_unknown_links = Arrays.asList();

        assertLinks(birth_records, death_records, expected_true_links, expected_unknown_links, UmeaBirthDeathIdentity::trueMatch, false);
    }

    @Test
    public void birthFatherIdentity() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthFatherIdentity/births.jsn", Birth::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(654521, 167253),
                Arrays.asList(654521, 167841)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(64654, 31247),
                Arrays.asList(31247, 654521),
                Arrays.asList(31247, 64654),
                Arrays.asList(64654, 654521)
        );

        assertLinks(birth_records, birth_records, expected_true_links, expected_unknown_links, UmeaBirthFatherIdentity::trueMatch, false);
    }

    @Test
    public void birthGroomIdentity() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthGroomIdentity/births.jsn", Birth::new);
        List<LXP> marriage_records = loadRecords("birthGroomIdentity/marriages.jsn", Marriage::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(167253,9687645),
                Arrays.asList(167841,64831));

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(56464,745114));

        assertLinks(birth_records, marriage_records, expected_true_links, expected_unknown_links, UmeaBirthGroomIdentity::trueMatch, false);
    }

    @Test
    public void birthMotherIdentity() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthMotherIdentity/births.jsn", Birth::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(654521, 167253),
                Arrays.asList(654521, 167841)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(64654, 31247),
                Arrays.asList(31247, 654521),
                Arrays.asList(31247, 64654),
                Arrays.asList(64654, 654521)
        );

        assertLinks(birth_records, birth_records, expected_true_links, expected_unknown_links, UmeaBirthMotherIdentity::trueMatch, false);
    }

    @Test
    public void birthSibling() throws PersistentObjectException, IOException {

        List<LXP> birth_records = loadRecords("birthSibling/births.jsn", Birth::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(167253, 167841, 654521),
                Arrays.asList(149857, 6541874),
                Arrays.asList(3214758, 985214)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(64654, 31247)
        );

        assertLinks(birth_records, birth_records, expected_true_links, expected_unknown_links, UmeaBirthSibling::trueMatch, true);
    }

    @Test
    public void brideBrideSibling() throws PersistentObjectException, IOException {

        List<LXP> marriage_records = loadRecords("brideBrideSibling/marriages.jsn", Marriage::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(64831, 6546513, 9687645),
                Arrays.asList(6411478, 6546513, 9687645),
                Arrays.asList(147147, 963963)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(745114, 65461)
        );

        assertLinks(marriage_records, marriage_records, expected_true_links, expected_unknown_links, UmeaBrideBrideSibling::trueMatch, true);
    }

    @Test
    public void brideGroomSibling() throws PersistentObjectException, IOException {

        List<LXP> marriage_records = loadRecords("brideGroomSibling/marriages.jsn", Marriage::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(64831, 6546513),
                Arrays.asList(64831, 9687645),
                Arrays.asList(6411478, 6546513),
                Arrays.asList(6411478, 9687645),
                Arrays.asList(147147, 963963)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(745114, 65461),
                Arrays.asList(65461, 745114)
        );

        assertLinks(marriage_records, marriage_records, expected_true_links, expected_unknown_links, UmeaBrideGroomSibling::trueMatch, false);
    }

    @Test
    public void deathSibling() throws PersistentObjectException, IOException {

        List<LXP> death_records = loadRecords("deathSibling/deaths.jsn", Death::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(30844578, 11111111, 5646511),
                Arrays.asList(65415647, 6461235),
                Arrays.asList(77777, 88888)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(666666, 94782)
        );

        assertLinks(death_records, death_records, expected_true_links, expected_unknown_links, UmeaDeathSibling::trueMatch, true);
    }

    @Test
    public void groomGroomSibling() throws PersistentObjectException, IOException {

        List<LXP> marriage_records = loadRecords("groomGroomSibling/marriages.jsn", Marriage::new);

        final List<List<Integer>> expected_true_links = Arrays.asList(
                Arrays.asList(64831, 6546513, 9687645),
                Arrays.asList(6411478, 6546513, 9687645),
                Arrays.asList(147147, 963963)
        );

        final List<List<Integer>> expected_unknown_links = Arrays.asList(
                Arrays.asList(745114, 65461)
        );

        assertLinks(marriage_records, marriage_records, expected_true_links, expected_unknown_links, UmeaGroomGroomSibling::trueMatch, true);
    }

    private void assertLinks(final List<LXP> records1, final List<LXP> records2, final List<List<Integer>> expected_true_links, final List<List<Integer>> expected_unknown_links, final MatchTest match_test, final boolean symmetric) {

        final Map<Integer, Set<Integer>> expected_true_links_by_first_record = getExpectedLinksForEachRecord(expected_true_links, symmetric);
        final Map<Integer, Set<Integer>> expected_unknown_links_by_first_record = getExpectedLinksForEachRecord(expected_unknown_links, symmetric);

        for (final LXP record1 : records1) {
            for (final LXP record2 : records2) {

                if (record1 != record2) {
                    assertLinkStatus(record1, record2, expected_true_links_by_first_record, expected_unknown_links_by_first_record, match_test);
                }
            }
        }

        checkMentionedLinksAllPresent(records1, records2, expected_true_links, expected_unknown_links);
    }

    private void checkMentionedLinksAllPresent(final List<LXP> records1, final List<LXP> records2, final List<List<Integer>> expected_true_links, final List<List<Integer>> expected_unknown_links) {

        final Set<Integer> all_mentioned_links = combine(expected_true_links, expected_unknown_links);
        for (LXP record : records1) all_mentioned_links.remove(getRecordId(record));
        for (LXP record : records2) all_mentioned_links.remove(getRecordId(record));
        if (all_mentioned_links.size() > 0) fail("record not present in data: " + all_mentioned_links.toArray()[0]);
    }

    private Set<Integer> combine(final List<List<Integer>> links1, final List<List<Integer>> links2) {

        final Set<Integer> result = new HashSet<>();
        for (List<Integer> links : links1) result.addAll(links);
        for (List<Integer> links : links2) result.addAll(links);
        return result;
    }

    private void assertLinkStatus(final LXP record1, final LXP record2, final Map<Integer, Set<Integer>> expected_true_links_by_first_record, final Map<Integer, Set<Integer>> expected_unknown_links_by_first_record, final MatchTest match_test) {

        final int record_id = getRecordId(record1);
        final Set<Integer> expected_true_links_for_first_record = expected_true_links_by_first_record.get(record_id);
        final Set<Integer> expected_unknown_links_for_first_record = expected_unknown_links_by_first_record.get(record_id);

        final LinkStatus expected_status = getExpectedStatus(record2, expected_true_links_for_first_record, expected_unknown_links_for_first_record);
        final String error_message = "\n" + record1 + "\n" + record2;

        assertEquals(error_message, expected_status, match_test.trueMatch(record1, record2));
    }

    private LinkStatus getExpectedStatus(final LXP record2, final Set<Integer> expected_true_links_for_first_record, final Set<Integer> expected_unknown_links_for_first_record) {

        final int record_id = getRecordId(record2);
        return expected_true_links_for_first_record != null && expected_true_links_for_first_record.contains(record_id) ? LinkStatus.TRUE_MATCH :
                expected_unknown_links_for_first_record != null && expected_unknown_links_for_first_record.contains(record_id) ? LinkStatus.UNKNOWN : LinkStatus.NOT_TRUE_MATCH;
    }

    private int getRecordId(final LXP record) {
        return Integer.parseInt(record.getString(LinkageRecipe.getRecordIdFieldNumber(record)));
    }

    private Map<Integer, Set<Integer>> getExpectedLinksForEachRecord(final List<List<Integer>> expected_links, final boolean symmetric) {

        final Map<Integer, Set<Integer>> expected_links_by_record = new HashMap<>();

        for (final List<Integer> expected_link : expected_links) {

            if (symmetric) loadSymmetricLinksByRecord(expected_link, expected_links_by_record);
            else loadAsymmetricLinksByRecord(expected_link, expected_links_by_record);
        }

        return expected_links_by_record;
    }

    private void loadSymmetricLinksByRecord(final List<Integer> expected_link, final Map<Integer, Set<Integer>> expected_links_by_first_record) {

        for (int i = 0; i < expected_link.size(); i++) {
            for (int j = i + 1; j < expected_link.size(); j++) {

                final int record_id1 = expected_link.get(i);
                final int record_id2 = expected_link.get(j);

                add(expected_links_by_first_record, record_id1, record_id2);
                add(expected_links_by_first_record, record_id2, record_id1);
            }
        }
    }

    private void loadAsymmetricLinksByRecord(final List<Integer> expected_link, final Map<Integer, Set<Integer>> expected_links_by_first_record) {

        final int record_id1 = expected_link.get(0);

        for (int j = 1; j < expected_link.size(); j++) {

            final int record_id2 = expected_link.get(j);
            add(expected_links_by_first_record, record_id1, record_id2);
        }
    }

    private void add(final Map<Integer, Set<Integer>> expected_links_by_first_record, final int record_id1, final int record_id2) {

        if (!expected_links_by_first_record.containsKey(record_id1)) {
            expected_links_by_first_record.put(record_id1, new HashSet<>());
        }
        expected_links_by_first_record.get(record_id1).add(record_id2);
    }

    private List<LXP> loadRecords(String resource_name, final StaticLXPFactory factory) throws PersistentObjectException, IOException {

        final List<LXP> list = new ArrayList<>();

        try (final InputStreamReader stream_reader = new InputStreamReader(UmeaGroundTruthTest.class.getResourceAsStream(resource_name))) {

            final JSONReader reader = new JSONReader(stream_reader);
            DynamicLXP dynamic_lxp = new DynamicLXP(reader, null);

            while (dynamic_lxp.getFieldCount() > 0) {

                list.add(makeStaticLXP(dynamic_lxp, factory));
                dynamic_lxp = new DynamicLXP(reader, null);
            }
        }

        return list;
    }

    private StaticLXP makeStaticLXP(final DynamicLXP dynamic_lxp, final StaticLXPFactory factory) {

        final StaticLXP static_lxp = factory.makeLXP();
        final LXPMetadata metaData = static_lxp.getMetaData();

        for (int field_number = 0; field_number < metaData.getFieldCount(); field_number++) {

            String value = "";
            try {
                value = (String)dynamic_lxp.get(metaData.getFieldName(field_number));
            }
            catch (KeyNotFoundException ignored) { }

            static_lxp.put(field_number, value);
        }

        return static_lxp;
    }

    interface StaticLXPFactory {
        StaticLXP makeLXP();
    }

    interface MatchTest {
        LinkStatus trueMatch(LXP record1, LXP record2);
    }
}
