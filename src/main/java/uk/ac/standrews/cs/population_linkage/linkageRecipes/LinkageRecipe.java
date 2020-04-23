package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.Normalisation;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.BucketKind;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Store;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.interfaces.IRepository;
import uk.ac.standrews.cs.storr.interfaces.IStore;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    protected final String results_repository_name;
    protected final String links_persistent_name;
    protected final String source_repository_name;
    protected final RecordRepository record_repository;
    protected Path store_path;

    protected Iterable<LXP> birth_records;
    protected Iterable<LXP> marriage_records;
    protected Iterable<LXP> death_records;

    private Iterable<LXP> pre_filtered_records = null;
    private int pre_filtering_required_populated_linkage_fields = 0;
    private Map<String, IBucket> storeRepoBucketLookUp = new HashMap<>();

    public LinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {

        this.results_repository_name = results_repository_name;
        this.links_persistent_name = links_persistent_name;
        this.source_repository_name = source_repository_name;

        store_path = ApplicationProperties.getStorePath();
        this.record_repository = new RecordRepository(store_path, source_repository_name);

        createRecordIterables();
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

        throw new RuntimeException("unexpected record type");
    }

    protected static Pair pair(final int first, final int second) {
        return new Pair(first, second);
    }

    @SuppressWarnings("unchecked")
    protected static <T> List<T> list(final T... values) {
        return Arrays.asList(values);
    }

    protected static int getBirthYearOfPersonBeingMarried(final LXP record, final boolean spouse_is_bride) {

        final String age_or_birth_date1 = record.getString(spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH);

        try {
            final int age_at_marriage_recorded = Integer.parseInt(age_or_birth_date1);
            final int marriage_year = Integer.parseInt(record.getString(Marriage.MARRIAGE_YEAR));

            return marriage_year - age_at_marriage_recorded;

        } catch (NumberFormatException e) {

            // Probably date of birth recorded rather than age.
            return Integer.parseInt(Normalisation.extractYear(age_or_birth_date1));
        }
    }

    protected static boolean spouseBirthIdentityLinkIsViable(final RecordPair proposedLink, final boolean spouse_is_bride) {

        try {
            int marriage_day = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_DAY));
            int marriage_month = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_MONTH));
            int marriage_year = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_YEAR));

            int birth_day = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_DAY));
            int birth_month = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_MONTH));
            int birth_year = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            LocalDate birth_date_from_birth_record = LocalDate.of(birth_year, birth_month, birth_day);
            LocalDate marriage_date_from_marriage_record = LocalDate.of(marriage_year, marriage_month, marriage_day);

            int age_at_marriage_calculated = birth_date_from_birth_record.until(marriage_date_from_marriage_record).getYears();

            final String age_or_birth_date = proposedLink.record1.getString(spouse_is_bride ? Marriage.BRIDE_AGE_OR_DATE_OF_BIRTH : Marriage.GROOM_AGE_OR_DATE_OF_BIRTH);
            int age_discrepancy;

            try {

                int age_at_marriage_recorded = Integer.parseInt(age_or_birth_date);
                age_discrepancy = Math.abs(age_at_marriage_calculated - age_at_marriage_recorded);

            } catch (NumberFormatException e) {

                // Probably date of birth recorded rather than age.
                LocalDate birth_date_from_marriage_record = Normalisation.parseDate(age_or_birth_date);
                age_discrepancy = Math.abs(birth_date_from_birth_record.until(birth_date_from_marriage_record).getYears());
            }

            return age_at_marriage_calculated >= LinkageConfig.MIN_AGE_AT_MARRIAGE && age_discrepancy <= LinkageConfig.MAX_ALLOWABLE_MARRIAGE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // in this case a BIRTH_YEAR or MARRIAGE_YEAR or GROOM_AGE_OR_DATE_OF_BIRTH is invalid
            return true;
        }
    }

    protected static boolean deathMarriageIdentityLinkIsViable(final RecordPair proposedLink) {

        try {
            int year_of_death = Integer.parseInt(proposedLink.record1.getString(Death.DEATH_YEAR));
            int year_of_marriage = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));

            return year_of_death >= year_of_marriage; // is death after marriage

        } catch (NumberFormatException e) { // in this case a DEATH_YEAR or MARRIAGE_YEAR is invalid
            return true;
        }
    }

    protected static boolean birthParentIdentityLinkIsViable(final RecordPair proposedLink) {

        try {
            int parent_year_of_birth = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int child_year_of_birth = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            int parent_age_at_child_birth = child_year_of_birth - parent_year_of_birth;

            return parent_age_at_child_birth >= LinkageConfig.MIN_PARENT_AGE_AT_BIRTH && parent_age_at_child_birth <= LinkageConfig.MAX_PARENT_AGE_AT_BIRTH;

        } catch (NumberFormatException e) {
            return true; // a YOB is missing or in an unexpected format
        }
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

    /*
    --------- DEBUG -----------
     */
    public static void showLXP(LXP lxp) {
        System.out.println(lxp.getString(Birth.FORENAME) + " " + lxp.getString(Birth.SURNAME) + " // "
                + lxp.getString(Birth.FATHER_FORENAME) + " " + lxp.getString(Birth.FATHER_SURNAME) + " " + lxp.getString(Birth.FAMILY));
    }

    public Iterable<LXP> getStoredRecords() {
        return getIterable(getStoredType());
    }

    public Iterable<LXP> getSearchRecords() {
        return getIterable(getSearchType());
    }

    public synchronized Iterable<LXP> getPreFilteredStoredRecords() {
        if (isSymmetric()) {
            if (pre_filtered_records == null) {
                // we do this for symmetric linkage recipes as it ensures the iterables
                // returned by this method and the one for records 2 is the same object - this is required by the
                // implementation of similarity search - otherwise we link to people to themselves
                pre_filtered_records = filterSourceRecords(getStoredRecords(), getLinkageFields());
            }
            return pre_filtered_records;
        } else {
            return filterSourceRecords(getStoredRecords(), getLinkageFields());
        }
    }

    public Iterable<LXP> getPreFilteredSearchRecords() {
        if (isSymmetric()) {
            return getPreFilteredStoredRecords();
        } else {
            return filterSourceRecords(getSearchRecords(), getSearchMappingFields());
        }
    }

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract String getLinkageType();

    /*
    --------- GROUND TRUTH CODE ---------
     */

    public abstract Class<? extends LXP> getStoredType();

    public abstract Class<? extends LXP> getSearchType();

    public abstract String getStoredRole();

    public abstract String getSearchRole();

    public abstract List<Integer> getLinkageFields();

    public abstract boolean isViableLink(RecordPair proposedLink);

    /**
     * This identifies how to map the fields in the search records to the fields in the storage records
     *
     * @return list of integers identifies mapping fields
     */
    public abstract List<Integer> getSearchMappingFields();

    public boolean isSymmetric() {
        // A linkage is symmetric if both record sets being linked have the same: record type AND role
        // (By definition this must mean that the chosen linkage fields are the same for both records)
        return getStoredType().equals(getSearchType()) && getStoredRole().equals(getSearchRole());
    }

    public LXP convertToOtherRecordType(LXP recordToConvert) {
        // here we are going to convert from the search type to the stored type - e.g. death to marriage (according to the role)

        // first make sure that the recordToConvert is of the appropriate type
        if (!(recordToConvert.getClass().equals(getSearchType()))) {
            throw new RuntimeException("Wrong record type to convert:" + recordToConvert.getClass().getName());
        }

        LXP resultingRecord;

        try {
            resultingRecord = getStoredType().newInstance(); // create an instance of the stored type
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (getLinkageFields().size() != getSearchMappingFields().size()) {
            throw new RuntimeException("Mismatched size for linkage fields and mapping fields");
        }

        // this pulls out the linkage fields from the search type and assigns them to the corresponding fields in the stored type
        // we do this so that when we pass records to the metric search they will always appear to be of the same type as that stored in the search structure
        for (int i = 0; i < getLinkageFields().size(); i++) {
            resultingRecord.put(getLinkageFields().get(i), recordToConvert.get(getSearchMappingFields().get(i)));
        }

        return resultingRecord;
    }

    public abstract Map<String, Link> getGroundTruthLinks();

    public abstract int getNumberOfGroundTruthTrueLinks();

    public abstract int getNumberOfGroundTruthTrueLinksPostFilter();

    /**
     * This method gets the set of group truth links for the two sets of source records based on the record fields
     * given by the parameters - in the LXP scheme the call will likely be Birth.FAMILY or Birth.CHILD_IDENTITY as these
     * are really ints that correspond to a field in the LXP.
     * <p>
     * The method itself creates a mapping from the chosen field to LXP for the records from source 1.
     * Then it iterates over the second set of source records and looks up each LXP in the map using the indicated field
     * If an LXP is in the map for this key then the two LXP constitute a true match and are thus added to the map of links
     * The formation of the link key simply concatonates the IDs for the two LXPs together.
     *
     * @param record1LinkageID the ground truth field for source records 1
     * @param record2LinkageID the ground truth field for source records 2
     * @return A map of all ground truth links
     */
    private Map<String, Link> getGroundTruthLinksOn(int record1LinkageID, int record2LinkageID, Iterable<LXP> sourceRecords1, Iterable<LXP> sourceRecords2) {

        final Map<String, Link> links = new HashMap<>();
        Map<String, Collection<LXP>> records1 = new HashMap<>();

        sourceRecords1.forEach(record1 -> {
            records1.putIfAbsent(record1.getString(record1LinkageID), new ArrayList<>());
            records1.get(record1.getString(record1LinkageID)).add(record1);
        });

        for (LXP record2 : sourceRecords2) {
            records1.computeIfPresent(record2.getString(record2LinkageID), (k, recordSet1) -> {
                try {
                    for (LXP record1 : recordSet1) {
                        Link l = new Link(record1, getStoredRole(), record2, getSearchRole(), 1.0f, "ground truth", -1);
                        String linkKey = toKey(record1, record2);

                        if (linkKey != null) // link key will be null if recipe is symmetric and record IDs are identical - shouldn't happen if this method is called
                            links.put(linkKey, l);
                    }

                } catch (PersistentObjectException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        return links;
    }

    protected Map<String, Link> getGroundTruthLinksOn(int record1LinkageID, int record2LinkageID) {
        return getGroundTruthLinksOn(record1LinkageID, record2LinkageID, getStoredRecords(), getSearchRecords());
    }

    /**
     * Returns the count of ground truth links among source records 1 and 2 when using the ground truth IDs
     * specified by the parameters.
     * <p>
     * The method behaviour is much the same as method: getGroundTruthLinksOn.
     *
     * @param record1LinkageID the ground truth field for source records 1
     * @param record2LinkageID the ground truth field for source records 2
     * @return A count of all ground truth links
     */
    private int getNumberOfGroundTruthTrueLinksOn(int record1LinkageID, int record2LinkageID, Iterable<LXP> sourceRecords1, Iterable<LXP> sourceRecords2) {

        Map<String, Collection<LXP>> records1 = new HashMap<>();
        sourceRecords1.forEach(record1 -> {
            records1.putIfAbsent(record1.getString(record1LinkageID), new ArrayList<>());
            records1.get(record1.getString(record1LinkageID)).add(record1);
        });

        int c = 0;

        for (LXP record2 : sourceRecords2)
            if (records1.containsKey(record2.getString(record2LinkageID)))
                c += records1.get(record2.getString(record2LinkageID)).size();

        return c;
    }

    protected int getNumberOfGroundTruthTrueLinksOn(int record1LinkageID, int record2LinkageID) {
        return getNumberOfGroundTruthTrueLinksOn(record1LinkageID, record2LinkageID, getStoredRecords(), getSearchRecords());
    }

    protected int getNumberOfGroundTruthTrueLinksPostFilterOn(int record1LinkageID, int record2LinkageID) {
        return getNumberOfGroundTruthTrueLinksOn(record1LinkageID, record2LinkageID, getPreFilteredStoredRecords(), getPreFilteredSearchRecords());
    }

    /**
     * Returns the set of ground truth links for symmetric sibling linkage.
     * A map of group/family ID to count of group size is created by the first loop
     * The values in this map are then looped over in the second loop - this loop created the combination of links for
     * the group subset.
     * - The originalId(a) != originalId(b) test ensures links are not made between records with the same ID
     * - The toKey(a,b) method created a key where the record IDs are ordered and then concatonated
     * - the ordering ensures that each link is only recorded in one direction (i.e. a link A->B is not also added as B->A)
     *
     * @param fatherID the father ID field to be used as ground truth (same for both records as symmetric linkage)
     * @param motherID the mother ID field to be used as ground truth (same for both records as symmetric linkage)
     * @return A map of all ground truth links
     */
    private Map<String, Link> getGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID, Iterable<LXP> sourceRecords1) {

        Map<String, ArrayList<LXP>> records1GroupedByLinkageID = new HashMap<>();

        for (LXP record1 : sourceRecords1) {

            String famID = record1.getString(fatherID).trim() + "-" + record1.getString(motherID).trim();
            if (!famID.equals(""))
                records1GroupedByLinkageID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        final Map<String, Link> links = new HashMap<>();

        records1GroupedByLinkageID.forEach((k, grouping) -> {

            for (LXP a : grouping)
                for (LXP b : grouping)
                    if (!Utilities.originalId(a).equals(Utilities.originalId(b))) {
                        try {
                            links.put(toKey(a, b), new Link(a, getStoredRole(), b, getSearchRole(), 1.0f, "ground truth", -1)); // role 1 and role 2 should be the same
                        } catch (PersistentObjectException e) {
                            ErrorHandling.error("PersistentObjectException adding getGroundTruthLinksOnSymmetric");
                        }
                    }
        });
        return links;
    }

    protected Map<String, Link> getGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID) {
        return getGroundTruthLinksOnSiblingSymmetric(fatherID, motherID, getStoredRecords());
    }

    /**
     * This method returns the count of all ground truth links for symmetric sibling linkage.
     * The first loop is the same as documented for getGroundTruthLinksOnSymmetric but with counts of links rather than the links.
     * The second loop calculates the number of links for each ground and sums these together.
     * - The links among a set are equal to the triangle number (this accounts for not linking to self or in two directions)
     *
     * @param fatherID the father ID field to be used as ground truth (same for both records as symmetric linkage)
     * @param motherID the mother ID field to be used as ground truth (same for both records as symmetric linkage)
     * @return A count of all ground truth links
     */
    private int getNumberOfGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID, Iterable<LXP> sourceRecords1) {

        Map<String, AtomicInteger> groupCounts = new HashMap<>();
        for (LXP record1 : sourceRecords1) {

            String famID = record1.getString(fatherID).trim() + "-" + record1.getString(motherID).trim();
            if (!famID.equals(""))
                groupCounts.computeIfAbsent(famID, k -> new AtomicInteger()).incrementAndGet();
        }

        AtomicInteger c = new AtomicInteger();
        groupCounts.forEach((key, count) -> {
            int numberOfLinksAmongGroup = (int) (count.get() * (count.get() - 1) / 2.0); // the number of links among the groups are defined by the triangle numbers - this is a formula for such!
            c.addAndGet(numberOfLinksAmongGroup);
        });

        return c.get();
    }

    /*
    --------- ABSTRACTION HELPER CODE -----------
     */

    protected int getNumberOfGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID) {
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(fatherID, motherID, getStoredRecords());
    }

    protected int getNumberOfGroundTruthLinksPostFilterOnSiblingSymmetric(int fatherID, int motherID) {
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(fatherID, motherID, getPreFilteredStoredRecords());
    }

    protected Map<String, Link> getGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID, Iterable<LXP> sourceRecords1, Iterable<LXP> sourceRecords2) {

        Map<String, ArrayList<LXP>> records1GroupedByFamilyID = new HashMap<>();
        for (LXP record1 : sourceRecords1) {

            String famID = record1.getString(r1FatherID).trim() + "-" + record1.getString(r1MotherID).trim();
            if (!famID.equals(""))
                records1GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        Map<String, ArrayList<LXP>> records2GroupedByFamilyID = new HashMap<>();
        for (LXP record2 : sourceRecords2) {

            String famID = record2.getString(r2FatherID).trim() + "-" + record2.getString(r2MotherID).trim();
            if (!famID.equals(""))
                records2GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record2);
        }

        final Map<String, Link> links = new HashMap<>();

        for (String famID : records1GroupedByFamilyID.keySet()) {

            ArrayList<LXP> records2 = records2GroupedByFamilyID.get(famID);

            if (records2 != null) {
                for (LXP a : records1GroupedByFamilyID.get(famID)) {
                    for (LXP b : records2GroupedByFamilyID.get(famID)) {
                        try {
                            links.put(toKey(a, b), new Link(a, getStoredRole(), b, getSearchRole(), 1.0f, "ground truth", -1));
                        } catch (PersistentObjectException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return links;
    }

    protected Map<String, Link> getGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID) {
        return getGroundTruthLinksOnSiblingNonSymmetric(r1FatherID, r1MotherID, r2FatherID, r2MotherID, getStoredRecords(), getSearchRecords());
    }

    protected int getNumberOfGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID, Iterable<LXP> sourceRecords1, Iterable<LXP> sourceRecords2) {

        Map<String, List<LXP>> records1GroupedByFamilyID = new HashMap<>();
        for (LXP record1 : sourceRecords1) {

            String famID = record1.getString(r1FatherID).trim() + "-" + record1.getString(r1MotherID).trim();
            if (!famID.equals(""))
                records1GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        Map<String, ArrayList<LXP>> records2GroupedByFamilyID = new HashMap<>();
        for (LXP record2 : sourceRecords2) {

            String famID = record2.getString(r2FatherID).trim() + "-" + record2.getString(r2MotherID).trim();
            if (!famID.equals(""))
                records2GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record2);
        }

        int numberOfLinks = 0;

        for (String famID : records1GroupedByFamilyID.keySet()) {

            List<LXP> records1 = records1GroupedByFamilyID.get(famID);
            List<LXP> records2 = records2GroupedByFamilyID.get(famID);

            if (records2 != null) {
                numberOfLinks += records1.size() * records2.size();
            }
        }
        return numberOfLinks;
    }

    protected int getNumberOfGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID) {
        return getNumberOfGroundTruthLinksOnSiblingNonSymmetric(r1FatherID, r1MotherID, r2FatherID, r2MotherID, getStoredRecords(), getSearchRecords());
    }

    protected int getNumberOfGroundTruthLinksPostFilterOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID) {
        return getNumberOfGroundTruthLinksOnSiblingNonSymmetric(r1FatherID, r1MotherID, r2FatherID, r2MotherID, getPreFilteredStoredRecords(), getPreFilteredSearchRecords());
    }

    public String toKey(LXP record1, LXP record2) {
        String s1 = Utilities.originalId(record1);
        String s2 = Utilities.originalId(record2);

        if (isSymmetric() && s1.compareTo(s2) == 0)
            return null;

        if (isSymmetric() && s1.compareTo(s2) > 0) {
            return s2 + "-" + s1; // this reordering prevents us putting the same link in opposite directions in the map - it will only be put in once
        } else {
            return s1 + "-" + s2;
        }
    }

    /*
    --------- PRE-FILTERING OF RECORDS ----------
     */

    public Iterable<Link> getLinksMade() { // this only works if you chose to persist the links
        try {
            IRepository repo = new Store(store_path).getRepository(results_repository_name);
            IBucket<Link> bucket = repo.getBucket(links_persistent_name, Link.class);
            return bucket.getInputStream();
        } catch (RepositoryException | BucketException e) {
            throw new RuntimeException("No made links repo found when expected - make sure you made the repo you're trying to access");
        }
    }

    private void createRecordIterables() {

        initIterable(getStoredType());

        if (!isSymmetric()) // if symmetric linkage then source type two will be same as source type 1 - thus waste of time to init it twice!
            initIterable(getSearchType());
    }

    private void initIterable(Class<? extends LXP> sourceType) {

        if (sourceType.equals(Birth.class)) {
            birth_records = Utilities.getBirthRecords(record_repository);
        } else if (sourceType.equals(Marriage.class)) {
            marriage_records = Utilities.getMarriageRecords(record_repository);
        } else if (sourceType.equals(Death.class)) {
            death_records = Utilities.getDeathRecords(record_repository);
        } else {
            throw new RuntimeException("Invalid source type");
        }
    }

    private Iterable<LXP> getIterable(Class<? extends LXP> sourceType) {

        if (sourceType.equals(Birth.class)) {
            return birth_records;
        }

        if (sourceType.equals(Marriage.class)) {
            return marriage_records;
        }

        if (sourceType.equals(Death.class)) {
            return death_records;
        }

        throw new RuntimeException("Invalid source type");
    }

    private int getSize(Class<? extends LXP> sourceType) throws BucketException {

        if (sourceType.equals(Birth.class)) {
            return record_repository.getNumberOfBirths();
        }

        if (sourceType.equals(Marriage.class)) {
            return record_repository.getNumberOfMarriages();
        }

        if (sourceType.equals(Death.class)) {
            return record_repository.getNumberOfDeaths();
        }

        throw new Error("Invalid source type");
    }

    /*
    ------- PERSISTENCE CODE ------------
     */

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, List<Integer> filterOn, int reqPopulatedFields) {
        Collection<LXP> filteredRecords = new HashSet<>();

        for (LXP record : records) {

            int numberOfEmptyFieldsPermitted = filterOn.size() - reqPopulatedFields + 1;

            for (int attribute : filterOn) {
                String value = record.getString(attribute).toLowerCase().trim();
                if (value.equals("") || value.contains("missing")) {
                    numberOfEmptyFieldsPermitted--;
                }

                if (numberOfEmptyFieldsPermitted == 0) {
                    break;
                }

            }

            if (numberOfEmptyFieldsPermitted > 0) { // this is a data-full record that we want to keep
                filteredRecords.add(record);
            }
        }

        return filteredRecords;
    }

    protected Iterable<LXP> filterBySex(Iterable<LXP> records, int sexField, String keepSex) {
        Collection<LXP> filteredRecords = new HashSet<>();

        records.forEach(record -> {
            if (record.getString(sexField).toLowerCase().equals(keepSex.toLowerCase()))
                filteredRecords.add(record);
        });
        return filteredRecords;
    }

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, List<Integer> filterOn) {
        return filterSourceRecords(records, filterOn, pre_filtering_required_populated_linkage_fields);
    }

    public int getPreFilteringRequiredPopulatedLinkageFields() {
        return pre_filtering_required_populated_linkage_fields;
    }

    public void setPreFilteringRequiredPopulatedLinkageFields(int preFilteringRequiredPopulatedLinkageFields) {
        if (preFilteringRequiredPopulatedLinkageFields > getLinkageFields().size()) {
            System.out.printf("Requested more linkage fields to be populated than are present - setting to number of linkage fields - %d \n", getLinkageFields().size());
            this.pre_filtering_required_populated_linkage_fields = getLinkageFields().size();
        } else {
            this.pre_filtering_required_populated_linkage_fields = preFilteringRequiredPopulatedLinkageFields;
        }
    }

    public void makeLinksPersistent(Iterable<Link> links) {
        makePersistentUsingStorr(store_path, results_repository_name, links_persistent_name, links);
    }

    public void makeLinkPersistent(Link link) {
        makePersistentUsingStorr(store_path, results_repository_name, links_persistent_name, link);
    }

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Link link) {

        try {
            //noinspection unchecked
            getBucket(store_path, results_repo_name, bucket_name).makePersistent(link);

        } catch (BucketException e) {
            throw new RuntimeException(e);
        }
    }

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Iterable<Link> links) {

        for (Link link : links)
            makePersistentUsingStorr(store_path, results_repo_name, bucket_name, link);
    }

    protected void makePersistentUsingFile(String name, Iterable<Link> links) {

        try {
            File f = new File(name);
            if (!f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Link l : links) {
                bw.write("Role1:\t" + l.getRole1() + "\tRole2:\t" + l.getRole2() + "\tid1:\t" + l.getRecord1().getReferend().getId() + "\tid2:\t" + l.getRecord2().getReferend().getId() + "\tprovenance:\t" + combineProvenance(l.getProvenance()));
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException | BucketException e) {
            throw new RuntimeException(e);
        }
    }

    private String combineProvenance(final List<String> provenance) {

        final StringBuilder builder = new StringBuilder();

        for (String s : provenance) {
            if (builder.length() > 0) builder.append("/");
            builder.append(s);
        }

        return builder.toString();
    }

    private IBucket getBucket(Path store_path, String results_repo_name, String bucket_name) {

        IBucket bucket = storeRepoBucketLookUp.get(getSRBString(store_path, results_repo_name, bucket_name));
        if (bucket == null) {

            try {
                IStore store = new Store(store_path);

                IRepository results_repository;
                try {
                    results_repository = store.getRepository(results_repo_name);
                } catch (RepositoryException e) {
                    results_repository = store.makeRepository(results_repo_name);
                }

                try {
                    bucket = results_repository.getBucket(bucket_name);
                } catch (RepositoryException e) {
                    bucket = results_repository.makeBucket(bucket_name, BucketKind.DIRECTORYBACKED, Link.class);
                }

                storeRepoBucketLookUp.put(getSRBString(store_path, results_repo_name, bucket_name), bucket);

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return bucket;
    }

    private String getSRBString(Path store_path, String results_repo_name, String bucket_name) {
        return store_path.toString() + "|" + results_repo_name + "|" + bucket_name;
    }

    public String getResults_repository_name() {
        return results_repository_name;
    }

    public String getLinks_persistent_name() {
        return links_persistent_name;
    }

    public String getSource_repository_name() {
        return source_repository_name;
    }

    public RecordRepository getRecord_repository() {
        return record_repository;
    }

    public int getSearchSetSize() {
        try {
            return getSize(getSearchType());
        } catch (BucketException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public int getStoredSetSize() {
        try {
            return getSize(getStoredType());
        } catch (BucketException e) {
            throw new RuntimeException(e.getMessage());
        }
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
