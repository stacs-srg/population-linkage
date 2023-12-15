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
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import com.google.common.collect.Iterables;
import uk.ac.standrews.cs.neoStorr.impl.DynamicLXP;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * In all linkage recipes the naming convention is:
 * the stored type is the first part of the name
 * the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipeMatchLists the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 */
public abstract class LinkageRecipe implements LinkViabilityChecker, AutoCloseable {

    /**
     * If TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN is false, then the recipe is tuned to the Umea dataset,
     * for which it is assumed that where an identifier is not
     * present, this means that the corresponding person/record is not included in the dataset. This
     * would be because the parent was not born or married within the geographical and temporal region.
     * <p>
     * Therefore we interpret absence of an identifier as having a particular meaning, and thus where
     * one record in a pair has an identifier and one doesn't, we classify as a non-match.
     * <p>
     * For use in a more general context with dirtier data, TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN
     * should be set to true. We then have less information about what a missing
     * identifier means, so classify as unknown.
     */
    protected static final boolean TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN = false;

    protected static final String EVERYTHING_STRING = "EVERYTHING";
    public static final int EVERYTHING = Integer.MAX_VALUE;

    protected final String source_repository_name;
    protected final String links_persistent_name;
    private final RecordRepository record_repository;

    private Iterable<LXP> birth_records;
    private Iterable<LXP> marriage_records;
    private Iterable<LXP> death_records;

    private Integer birth_records_size;
    private Integer death_records_size;
    private Integer marriage_records_size;

    private int number_of_linkage_fields_required;
//    private StringMeasure base_measure;
    protected NeoDbCypherBridge bridge;

    public LinkageRecipe(String source_repository_name, String links_persistent_name) {

        this.source_repository_name = source_repository_name;
        this.links_persistent_name = links_persistent_name;

        this.record_repository = new RecordRepository(source_repository_name);
        bridge = Store.getInstance().getBridge(); // lovely :)
    }

    public NeoDbCypherBridge getBridge() { 
        return bridge;
    }

    public void close() {
        record_repository.close();
        bridge.close();
    }

    public String getLinksPersistentName() {
        return links_persistent_name;
    }

    public int getQuerySetSize() {
        return getSizeByType(getQueryType());
    }

//    public abstract double getThreshold();

    public void setCacheSizes(int birthCacheSize, int deathCacheSize, int marriageCacheSize) {
        record_repository.setBirthsCacheSize(birthCacheSize);
        record_repository.setDeathsCacheSize(deathCacheSize);
        record_repository.setMarriagesCacheSize(marriageCacheSize);
    }

//    public StringMeasure getBaseMeasure() {
//        return base_measure;
//    }
//
//    public void setBaseMeasure(StringMeasure m) {
//        this.base_measure = m;
//    }

    //public abstract LXPMeasure getCompositeMeasure();

    public record Pair(int first, int second){}

    public int getNumberOfLinkageFieldsRequired() {
        return number_of_linkage_fields_required;
    }

    public void setNumberOfLinkageFieldsRequired(int number_of_linkage_fields_required) {
        this.number_of_linkage_fields_required = number_of_linkage_fields_required;
    }

    public static LinkStatus trueMatch(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            boolean match = true;
            for (Pair fields : true_match_fields) {
                if (!equalsNonEmpty(record1.getString(fields.first), record2.getString(fields.second))) {
                    match = false;
                }
            }
            if (match) return LinkStatus.TRUE_MATCH;
        }

        boolean all_empty = allEmpty(record1, record2, true_match_alternatives);
        boolean any_empty = anyIdentityFieldsEmpty(record1, record2, true_match_alternatives);

        if ((TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && any_empty) || (!TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && all_empty)) {

            return LinkStatus.UNKNOWN;
        }

        return LinkStatus.NOT_TRUE_MATCH;
    }

    public static boolean allEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            if (!allIdentityFieldsEmpty(record1, record2, true_match_fields)) return false;
        }
        return true;
    }

    public static boolean allIdentityFieldsEmpty(final LXP record1, final LXP record2, final List<Pair> true_match_fields) {

        for (Pair fields : true_match_fields) {
            if (identityFieldNotEmpty(record1, fields.first)) return false;
            if (identityFieldNotEmpty(record2, fields.second)) return false;
        }
        return true;
    }

    public static boolean anyIdentityFieldsEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            for (Pair fields : true_match_fields) {
                if (identityFieldEmpty(record1, fields.first)) return true;
                if (identityFieldEmpty(record2, fields.second)) return true;
            }
        }
        return false;
    }

    private static boolean identityFieldEmpty(final LXP record, final int field_number) {

        // Ignore the record id field.
        return field_number != getRecordIdFieldNumber(record) && record.getString(field_number).isEmpty();
    }

    private static boolean identityFieldNotEmpty(final LXP record, final int field_number) {

        // Ignore the record id field.
        return field_number != getRecordIdFieldNumber(record) && !record.getString(field_number).isEmpty();
    }

    public static int getRecordIdFieldNumber(final LXP record) {

        if (record instanceof Birth) return Birth.STANDARDISED_ID;
        if (record instanceof Marriage) return Marriage.STANDARDISED_ID;
        if (record instanceof Death) return Death.STANDARDISED_ID;
        if (record instanceof DynamicLXP) {
            Integer slot = record.getMetaData().getSlot("STANDARDISED_ID");
            if (slot == null) {
                throw new RuntimeException("unexpected record type - can't find STANDARDISED_ID in DynamicLXP");
            } else {
                return slot;
            }
        }

        throw new RuntimeException("unexpected record type");
    }

    public static Pair pair(final int first, final int second) {
        return new Pair(first, second);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> list(final T... values) {
        return Arrays.asList(values);
    }

    static boolean equalsNonEmpty(final String s1, final String s2) {
        return !s1.isEmpty() && s1.equals(s2);
    }

    Iterable<LXP> getByType(Class<? extends LXP> type) {
        if (type.equals(Birth.class)) {
            Iterable<LXP> records = getBirthRecords();
            return records;
        }
        if (type.equals(Marriage.class)) {
            Iterable<LXP> records =  getMarriageRecords();
            return records;
        }
        if (type.equals(Death.class)) {
            Iterable<LXP> records = getDeathRecords();
            return records;
        }
        throw new RuntimeException("Invalid source type");
    }

    private void printSize( Iterable<LXP> iterable, String label) {
        System.out.println( "Retrieved " + Iterables.size(iterable) + " " + label + " records" );
    }

    public Iterable<LXP> getStoredRecords() {
        Iterable<LXP> records = getByType(getStoredType());
        printSize( records,"stored");
        return records;
    }

    public Iterable<LXP> getQueryRecords() {
        Iterable<LXP> records = getByType(getQueryType());
        printSize( records,"query");
        return records;
    }

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract String getLinkageType();

    /*
    --------- GROUND TRUTH CODE ---------
     */

    public abstract Class<? extends LXP> getStoredType();

    public abstract Class<? extends LXP> getQueryType();

    public abstract String getStoredRole();

    public abstract String getQueryRole();

    public abstract List<Integer> getLinkageFields();

    public abstract boolean isViableLink(LXP record1, LXP record2);

    /**
     * This identifies how to map the fields in the query records to the fields in the storage records
     *
     * @return list of integers identifies mapping fields
     */
    public abstract List<Integer> getQueryMappingFields();

    public boolean isSymmetric() {
        // A linkage is symmetric if both record sets being linked have the same: record type AND role
        // (By definition this must mean that the chosen linkage fields are the same for both records)
        return getStoredType().equals(getQueryType()) && getStoredRole().equals(getQueryRole());
    }

    public LXP convertToOtherRecordType(LXP recordToConvert) {
        // here we are going to convert from the search type to the stored type - e.g. death to marriage (according to the role)

        // first make sure that the recordToConvert is of the appropriate type
        if (!(recordToConvert.getClass().equals(getQueryType()))) {
            throw new RuntimeException("Wrong record type to convert:" + recordToConvert.getClass().getName());
        }

        LXP resultingRecord;

        try {
            resultingRecord = getStoredType().getDeclaredConstructor().newInstance(); // create an instance of the stored type
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (getLinkageFields().size() != getQueryMappingFields().size()) {
            throw new RuntimeException("Mismatched size for linkage fields and mapping fields");
        }

        // this pulls out the linkage fields from the search type and assigns them to the corresponding fields in the stored type
        // we do this so that when we pass records to the metric search they will always appear to be of the same type as that stored in the search structure
        for (int i = 0; i < getLinkageFields().size(); i++) {
            resultingRecord.put(getLinkageFields().get(i), recordToConvert.get(getQueryMappingFields().get(i)));
        }

        return resultingRecord;
    }

    public abstract Map<String, Link> getGroundTruthLinks();

    public abstract long getNumberOfGroundTruthTrueLinks();

    /**
     * Returns the count of ground truth links among source records 1 and 2
     * @return A count of all ground truth links
     */
    public int getNumberOfGroundTruthLinksAsymmetric() {

        int count = 0;

        for (LXP record1 : getStoredRecords()) {
            for (LXP record2 : getQueryRecords()) {
                if (isTrueMatch(record1, record2) == LinkStatus.TRUE_MATCH) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * This method returns the count of all ground truth links.
     *
     * @return a count of all ground truth links
     */
    int getNumberOfGroundTruthLinksSymmetric() {

        int count = 0;

        LXP[] records = StreamSupport.stream(getStoredRecords().spliterator(), false).toArray(LXP[]::new);

        for (int i = 0; i < records.length - 1; i++) {
            for (int j = i + 1; j < records.length; j++) {
                if (isTrueMatch(records[i], records[j]) == LinkStatus.TRUE_MATCH) {
                    count++;
                }
            }
        }

        return count;
    }

    public Map<String, Link> getGroundTruthLinksSymmetric() {

        Map<String, Link> map = new HashMap<>();
        LXP[] records = StreamSupport.stream(getStoredRecords().spliterator(), false).toArray(LXP[]::new);

        for (int i = 0; i < records.length - 1; i++) {
            for (int j = i + 1; j < records.length; j++) {
                if (isTrueMatch(records[i], records[j]) == LinkStatus.TRUE_MATCH) {
                    try {
                        Link l = new Link(records[i], getStoredRole(), records[j], getQueryRole(), 1.0f, "GT", 0.0, "GT");
                        map.put(l.toString(), l);
                    } catch (PersistentObjectException e) {
                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                    }
                }
            }
        }

        return map;
    }

    public Map<String, Link> getGroundTruthLinksAsymmetric() {

        Map<String, Link> map = new HashMap<>();
        for (LXP lxp1 : getStoredRecords()) {
            for (LXP lxp2 : getQueryRecords()) {
                LinkStatus ls = isTrueMatch(lxp1, lxp2);
                if (ls.equals(LinkStatus.TRUE_MATCH)) {
                    try {
                        Link l = new Link(lxp1, getStoredRole(), lxp2, getQueryRole(), 1.0f, "GT", 0.0, "GT");
                        map.put(l.toString(), l);
                    } catch (PersistentObjectException e) {
                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                    }
                }
            }
        }
        return map;
    }

    public void setNumberLinkageFieldsRequired(int number) {
        number_of_linkage_fields_required = number;
    }

    public String toKey(LXP query_record, LXP stored_record) {

        String s1 = Utilities.originalId(query_record);
        String s2 = Utilities.originalId(stored_record);

        if (isSymmetric() && s1.compareTo(s2) == 0)
            return null;

        if (isSymmetric() && s1.compareTo(s2) > 0) {
            return s2 + "-" + s1; // this reordering prevents us putting the same link in opposite directions in the map - it will only be put in once
        } else {
            return s1 + "-" + s2;
        }
    }

    public Iterable<LXP> getBirthRecords() {
        if (birth_records == null) {
            birth_records = Utilities.getBirthRecords(record_repository);
        }
        return birth_records;
    }

    protected int getBirthRecordsSize() {
        if (birth_records_size == null) {
            birth_records_size = getSize(birth_records);
        }
        return birth_records_size;
    }

    protected Iterable<LXP> getDeathRecords() {
        if (death_records == null) {
            death_records = Utilities.getDeathRecords(record_repository);
        }
        return death_records;
    }

    protected int getDeathRecordsSize() {
        if (death_records_size == null) {
            death_records_size = getSize(death_records);
        }
        return death_records_size;
    }

    protected Iterable<LXP> getMarriageRecords() {
        if (marriage_records == null) {
            marriage_records = Utilities.getMarriageRecords(record_repository);
        }
        return marriage_records;
    }

    protected int getMarriageRecordsSize() {
        if (marriage_records_size == null) {
            marriage_records_size = getSize(marriage_records);
        }
        return marriage_records_size;
    }

    private int getSize(Iterable<LXP> records) {
        int size = 0;
        for (LXP ignored : records) {
            size++;
        }
        return size;
    }

    protected int getSizeByType(Class<? extends LXP> type) {
        if (type.equals(Birth.class)) {
            return getBirthRecordsSize();
        }
        if (type.equals(Marriage.class)) {
            return getMarriageRecordsSize();
        }
        if (type.equals(Death.class)) {
            return getDeathRecordsSize();
        }
        throw new RuntimeException("Invalid source type");
    }

    /*
    ------- PERSISTENCE CODE ------------
     */

    protected Iterable<LXP> filterBySex(Iterable<LXP> records, int sexField, String keepSex) {
        Collection<LXP> filteredRecords = new HashSet<>();

        records.forEach(record -> {
            if (record.getString(sexField).equalsIgnoreCase(keepSex))
                filteredRecords.add(record);
        });
        return filteredRecords;
    }
}
