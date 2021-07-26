/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.DynamicLXP;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */
public abstract class LinkageRecipe {

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
    protected static final int EVERYTHING = Integer.MAX_VALUE;

    protected String links_persistent_name;
    protected final String source_repository_name;
    private final RecordRepository record_repository;
    protected Path store_path;

    private Iterable<LXP> birth_records;
    private Iterable<LXP> marriage_records;
    private Iterable<LXP> death_records;

    private  Integer birth_records_size = null;
    private  Integer death_records_size = null;
    private  Integer marriage_records_size = null;

    public LinkageRecipe(String source_repository_name, String links_persistent_name) {

        this.links_persistent_name = links_persistent_name;
        this.source_repository_name = source_repository_name;


        this.record_repository = new RecordRepository(source_repository_name);
    }

    protected ArrayList<LXP> filter(int number_of_required_fields, int number_of_records_required, Iterable<LXP> records_to_filter, List<Integer> linkageFields) {
        ArrayList<LXP> filtered_source_records = new ArrayList<>();
        int count_rejected = 0;
        int count_accepted = 0;

        for (LXP record : records_to_filter) {
            if (passesFilter(record, linkageFields, number_of_required_fields)) {
                filtered_source_records.add(record);
                count_accepted++;
            }
            else {
//                // Trace
//                if( count_rejected < 50 ) {
//                    System.out.print( "Rejected: " );
//                    for( int i : linkageFields ) {
//                        System.out.print(record.getMetaData().getFieldName(i) + ":" + record.getString(i) + "/ ");
//                    }
//                    System.out.println();
//                }
                count_rejected++;
            }
            if (filtered_source_records.size() >= number_of_records_required) {
                break;
            }
        }
        System.out.println( "Filtering: accepted: " + count_accepted + " rejected: " + count_rejected + " from " + ( count_rejected + count_accepted ) );
        return filtered_source_records;
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
        boolean any_empty = anyEmpty(record1, record2, true_match_alternatives);

        if ((TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && any_empty) || (!TREAT_ANY_ABSENT_GROUND_TRUTH_AS_UNKNOWN && all_empty)) {

            return LinkStatus.UNKNOWN;
        }

        return LinkStatus.NOT_TRUE_MATCH;
    }

    public static boolean allEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

        for (List<Pair> true_match_fields : true_match_alternatives) {

            if (!allFieldsEmpty(record1, record2, true_match_fields)) return false;
        }
        return true;
    }

    public static boolean allFieldsEmpty(final LXP record1, final LXP record2, final List<Pair> true_match_fields) {

        for (Pair fields : true_match_fields) {
            if (identityFieldNotEmpty(record1, fields.first)) return false;
            if (identityFieldNotEmpty(record2, fields.second)) return false;
        }
        return true;
    }

    public static boolean anyEmpty(final LXP record1, final LXP record2, final List<List<Pair>> true_match_alternatives) {

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
        if( record instanceof DynamicLXP) {
            DynamicLXP lxp = (DynamicLXP) record;
            Integer slot = record.getMetaData().getSlot("STANDARDISED_ID");
            if( slot == null ) {
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

    static boolean allEmpty(final String... strings) {
        for (String s : strings) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    static boolean anyEmpty(final String... strings) {
        for (String s : strings) {
            if (s.isEmpty()) return true;
        }
        return false;
    }

    Iterable<LXP> getByType(Class<? extends LXP> type) {
        if (type.equals(Birth.class)) {
            return getBirthRecords();
        }
        if (type.equals(Marriage.class)) {
            return getMarriageRecords();
        }
        if (type.equals(Death.class)) {
            return getDeathRecords();
        }
        throw new RuntimeException("Invalid source type");
    }

    public Iterable<LXP> getStoredRecords() {
        return getByType(getStoredType());
    }

    public Iterable<LXP> getQueryRecords() {
        return getByType(getQueryType());
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

    public abstract boolean isViableLink(RecordPair proposedLink);

    /**
     * This identifies how to map the fields in the query records to the fields in the storage records
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

    public abstract int getNumberOfGroundTruthTrueLinks();

    /**
     * Returns the count of ground truth links among source records 1 and 2
     * @return A count of all ground truth links
     */
    public int getNumberOfGroundTruthLinksAsymmetric() {

        int count = 0;

        for (LXP record1 : getStoredRecords()) {
            for (LXP record2 : getQueryRecords()) {
                if( record1 != record2 ) {
                    if( isTrueMatch(record1,record2) == LinkStatus.TRUE_MATCH ) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * This method returns the count of all ground truth links.
     * @return A count of all ground truth links
     */
    int getNumberOfGroundTruthLinksSymmetric() {

        int count = 0;

        for (LXP record1 : getStoredRecords()) {
            for (LXP record2 : getStoredRecords()) {
                if( record1 != record2 ) {
                    if( isTrueMatch(record1,record2) == LinkStatus.TRUE_MATCH ) {
                        count++;
                    }
                }
            }
        }

        return count / 2; // above counts each link twice.
    }

    public Map<String, Link> getGroundTruthLinksSymmetric() {
        Map<String, Link> map = new HashMap<>();
        for (LXP lxp1 : getStoredRecords()) {
            for (LXP lxp2 : getStoredRecords()) {
                if( lxp1.getId() != lxp2.getId() ) {
                    LinkStatus ls = isTrueMatch(lxp1, lxp2);
                    if (ls.equals(LinkStatus.TRUE_MATCH)) {
                        try {
                            if( lxp1.getId() < lxp2.getId()) { // Only put in ascending order to avoid duplicates!
                                Link l = new Link(lxp1, getStoredRole(), lxp2, getQueryRole(), 1.0f, "GT", 0.0, "GT");
                                map.put(l.toString(), l);
                            }
                        } catch (PersistentObjectException e) {
                            ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                        }
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

    /**
     * Note - May be overridden by subclass
     * @return
     */
    protected Iterable<LXP> getBirthRecords() {
        if( birth_records == null ) {
            birth_records = Utilities.getBirthRecords(record_repository);
        }
        return birth_records;
    }

    protected int getBirthRecordsSize() {
        if( birth_records_size == null ) {
            birth_records_size = getSize( birth_records );
        }
        return birth_records_size;
    }

    /**
     * Note - May be overridden by subclass
     * @return the death records to be used in this recipe
     */
    protected Iterable<LXP> getDeathRecords() {
        if( death_records == null ) {
            death_records = Utilities.getDeathRecords(record_repository);
        }
        return death_records;
    }

    protected int getDeathRecordsSize() {
        if( death_records_size == null ) {
            death_records_size = getSize( death_records );
        }
        return death_records_size;
    }

    /**
     * Note - May be overwritten by subclass
     * @return the marriage records to be used in this recipe
     */
    protected Iterable<LXP> getMarriageRecords() {
        if( marriage_records == null ) {
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



    private int getSize(Iterable<LXP> records ) {
        int size = 0;
        for(LXP value : records) {
            size++;
        }
        return size;
    }

    protected int getSizeByType( Class<? extends LXP> type ) {
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

    protected Iterable<LXP> filterRecords(Iterable<LXP> records, List<Integer> filterOn, int reqPopulatedFields) {
        Collection<LXP> filteredRecords = new HashSet<>();

        for (LXP record : records) {

            if( passesFilter(record, filterOn, reqPopulatedFields) ) {
                filteredRecords.add(record);
            }
        }

        return filteredRecords;
    }

    public boolean passesFilter(LXP record, List<Integer> filterOn, int reqPopulatedFields) {
        int numberOfEmptyFieldsPermitted = filterOn.size() - reqPopulatedFields;
        int numberOfEmptyFields = 0;

        for (int attribute : filterOn) {
            String value = record.getString(attribute).toLowerCase().trim();
            if (value.equals("") || value.contains("missing") || value.equals("--") || value.equals("----") ) {  // TODO could make this field specific
                numberOfEmptyFields++;
            }
        }

        return numberOfEmptyFields <= numberOfEmptyFieldsPermitted;
    }

    protected Iterable<LXP> filterBySex(Iterable<LXP> records, int sexField, String keepSex) {
        Collection<LXP> filteredRecords = new HashSet<>();

        records.forEach(record -> {
            if (record.getString(sexField).toLowerCase().equals(keepSex.toLowerCase()))
                filteredRecords.add(record);
        });
        return filteredRecords;
    }

    public void makeLinksPersistent(Iterable<Link> links) {
        for( Link link : links ) {
            makeLinkPersistent(link);
        }
    }

    public void makeLinkPersistent(Link link) {
        throw new RuntimeException( "makeLinkPersistent unimplemented");
    }

    public String getLinks_persistent_name() {
        return links_persistent_name;
    }

    public int getQuerySetSize() {
        return getSizeByType(getQueryType());
    }

    public int getStoredSetSize() {
        return getSizeByType(getQueryType());
    }

    public abstract double getThreshold();

    public void setCacheSizes(int birthCacheSize, int deathCacheSize, int marriageCacheSize) {
        record_repository.setBirthsCacheSize(birthCacheSize);
        record_repository.setDeathsCacheSize(deathCacheSize);
        record_repository.setMarriagesCacheSize(marriageCacheSize);
    }

    public StringMetric getMetric() {
        return new JensenShannon(2048);
    }

    public static class Pair {
        public final int first;
        public final int second;

        public Pair(final int first, final int second) {
            this.first = first;
            this.second = second;
        }
    }
}
