package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LinkageRecipe {

    protected final String results_repository_name;
    protected final String links_persistent_name;
    protected final String source_repository_name;
    protected final RecordRepository record_repository;
    protected Path store_path;

    protected Iterable<LXP> birth_records;
    protected Iterable<LXP> marriage_records;
    protected Iterable<LXP> death_records;

    public LinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {

        this.results_repository_name = results_repository_name;
        this.links_persistent_name = links_persistent_name;
        this.source_repository_name = source_repository_name;
        this.record_repository = record_repository;

        store_path = ApplicationProperties.getStorePath();

        createRecordIterables();
    }

    private void createRecordIterables() {

        initIterable(getSourceType1());

        if(!isSymmetric()) // if symmetric linkage then source type two will be same as source type 1 - thus waste of time to init it twice!
            initIterable(getSourceType2());

    }

    private void initIterable(String sourceType) {
        switch(sourceType.toLowerCase()) {
            case "birth":
            case "births":
                birth_records = Utilities.getBirthRecords(record_repository);
                break;
            case "marriage":
            case "marriages":
                marriage_records = Utilities.getMarriageRecords(record_repository);
                break;
            case "death":
            case "deaths":
                death_records = Utilities.getDeathRecords(record_repository);
                break;
        }
    }

    private Iterable<LXP> getIterable(String sourceType) {
        switch(sourceType.toLowerCase()) {
            case "birth":
            case "births":
                return birth_records;
            case "marriage":
            case "marriages":
                return marriage_records;
            case "death":
            case "deaths":
                return death_records;
        }
        throw new Error("Invalid source type");
    }

    public Iterable<LXP> getSourceRecords1() {
        return getIterable(getSourceType1());
    }

    public Iterable<LXP> getSourceRecords2() {
        return getIterable(getSourceType2());
    }

    public abstract Iterable<LXP> getPreFilteredSourceRecords1();

    public abstract Iterable<LXP> getPreFilteredSourceRecords2();

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract String getLinkageType();

    public abstract String getSourceType1();

    public abstract String getSourceType2();

    public abstract String getRole1();

    public abstract String getRole2();

    public abstract List<Integer> getLinkageFields1();

    public abstract List<Integer> getLinkageFields2();

    public boolean isSymmetric() {
        // A linkage is symmetric if for both records sets being linked have the same: record type AND role
        // (By definition this must mean that the chosen linkage fields are the same for both records - i.e. sibling linkage)
        return getSourceType1().equals(getSourceType2()) && getRole1().equals(getRole2());
    }

    /**
     * This method gets the set of group truth links for the two sets of source records based on the record fields
     * given by the parameters - in the LXP scheme the call will likely be Birth.FAMILY or Birth.CHILD_IDENTITY as these
     * are really ints that correspond to a field in the LXP.
     *
     * The method itself creates a mapping from the chosen field to LXP for the records from source 1.
     * Then it iterates over the second set of source records and looks up each LXP in the map using the indicated field
     * If an LXP is in the map for this key then the two LXP constitute a true match and are thus added to the map of links
     * The formation of the link key simply concatonates the IDs for the two LXPs together.
     *
     * @param record1LinkageID the ground truth field for source records 1
     * @param record2LinkageID the ground truth field for source records 2
     * @return A map of all ground truth links
     */
    protected Map<String, Link> getGroundTruthLinksOn(int record1LinkageID, int record2LinkageID) {
        final Map<String, Link> links = new HashMap<>();
        Map<String, LXP> records1 = new HashMap<>();

        getSourceRecords1().forEach(record1 -> records1.put(record1.getString(record1LinkageID), record1));

        for (LXP record2 : getSourceRecords2()) {
            records1.computeIfPresent(record2.getString(record2LinkageID), (k, record1) -> {
                try {
                    Link l = new Link(record1, getRole1(), record2, getRole2(), 1.0f, "ground truth");
                    String linkKey = toKey(record1, record2);

                    if(linkKey != null) // link key will be null if recipe is symmetric and record IDs are identical - shouldn't happen if this method is called
                        links.put(linkKey, l);

                } catch (PersistentObjectException e) {
                    ErrorHandling.error("PersistentObjectException adding getGroundTruthLinksOn");
                }
                return record1;
            });
        }
        return links;
    }

    /**
     * This method returns the count of ground truth links among source records 1 and 2 when using the ground truth IDs
     * specified by the parameters.
     *
     * The method behaviour is much the same as method: getGroundTruthLinksOn - see javadoc their for more info.
     *
     * @param record1LinkageID the ground truth field for source records 1
     * @param record2LinkageID the ground truth field for source records 2
     * @return A count of all ground truth links
     */
    protected int numberOfGroundTruthTrueLinksOn(int record1LinkageID, int record2LinkageID) {

        Map<String, LXP> records1 = new HashMap<>();
        getSourceRecords1().forEach(record1 -> records1.put(record1.getString(record1LinkageID), record1));

        int c = 0;

        for (LXP record2 : getSourceRecords2())
            if(records1.containsKey(record2.getString(record2LinkageID)))
                c++;

        return c;
    }

    /**
     * This method returns the set of ground truth links for symmetric sibling linkage.
     * A map of group/family ID to count of group size is created by the first loop
     * The values in this map are then looped over in the second loop - this loop created the combination of links for
     * the group subset.
     *  - The originalIdField(a) != originalIdField(b) test ensures links are not made between records with the same ID
     *  - The toKey(a,b) method created a key where the record IDs are ordered and then concatonated
     *      - the ordering ensures that each link is only recorded in one direction (i.e. a link A->B is not also added as B->A)
     *
     * @param fatherID the father ID field to be used as ground truth (same for both records as symmetric linkage)
     * @param motherID the mother ID field to be used as ground truth (same for both records as symmetric linkage)
     * @return A map of all ground truth links
     */
    protected Map<String, Link> getGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID) {

        Map<String, ArrayList<LXP>> records1GroupedByLinkageID = new HashMap<>();
        for(LXP record1 : getSourceRecords1()) {

            String famID = record1.getString(fatherID).trim() + "-" + record1.getString(motherID).trim();
            if(!famID.equals(""))
                records1GroupedByLinkageID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        final Map<String, Link> links = new HashMap<>();

        records1GroupedByLinkageID.forEach((k, grouping) -> {

            for(LXP a : grouping)
                for(LXP b : grouping)
                    if(originalIdField(a) != originalIdField(b)) {
                        try {
                            links.put(toKey(a,b), new Link(a, getRole1(), b, getRole2(), 1.0f, "ground truth")); // role 1 and role 2 should be the same
                        } catch (PersistentObjectException e) {
                            ErrorHandling.error("PersistentObjectException adding getGroundTruthLinksOnSymmetric");
                        }
                    }
        });
        return links;
    }

    /**
     * This method returns the count of all ground truth links for symmetric sibling linkage.
     * The first loop is the same as documented for getGroundTruthLinksOnSymmetric but with counts of links rather than the links.
     * The second loop calculates the number of links for each ground and sums these together.
     *  - The links among a set are equal to the triangle number (this accounts for not linking to self or in two directions)
     *
     * @param fatherID the father ID field to be used as ground truth (same for both records as symmetric linkage)
     * @param motherID the mother ID field to be used as ground truth (same for both records as symmetric linkage)
     * @return A count of all ground truth links
     */
    protected int getNumberOfGroundTruthLinksOnSiblingSymmetric(int fatherID, int motherID) {

        Map<String, AtomicInteger> groupCounts = new HashMap<>();
        for(LXP record1 : getSourceRecords1()) {

            String famID = record1.getString(fatherID).trim() + "-" + record1.getString(motherID).trim();
            if(!famID.equals(""))
                groupCounts.computeIfAbsent(famID, k -> new AtomicInteger()).incrementAndGet();
        }

        AtomicInteger c = new AtomicInteger();
        groupCounts.forEach((key, count) -> {
            int numberOfLinksAmongGroup = (int) (count.get() * (count.get() - 1) / 2.0); // the number of links among the groups are defined by the triangle numbers - this is a formula for such!
            c.addAndGet(numberOfLinksAmongGroup);
        });

        return c.get();
    }

    protected Map<String, Link> getGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID) {

        Map<String, ArrayList<LXP>> records1GroupedByFamilyID = new HashMap<>();
        for(LXP record1 : getSourceRecords1()) {

            String famID = record1.getString(r1FatherID).trim() + "-" + record1.getString(r1MotherID).trim();
            if(!famID.equals(""))
                records1GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        Map<String, ArrayList<LXP>> records2GroupedByFamilyID = new HashMap<>();
        for(LXP record2 : getSourceRecords1()) {

            String famID = record2.getString(r2FatherID).trim() + "-" + record2.getString(r2MotherID).trim();
            if(!famID.equals(""))
                records2GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record2);
        }

        final Map<String, Link> links = new HashMap<>();

        for(String famID : records1GroupedByFamilyID.keySet()) {

            ArrayList<LXP> records2 = records2GroupedByFamilyID.get(famID);

            if(records2 != null) {
                for(LXP a : records1GroupedByFamilyID.get(famID))
                    for(LXP b : records2GroupedByFamilyID.get(famID)) {
                        try {
                            links.put(toKey(a,b), new Link(a, getRole1(), b, getRole2(), 1.0f, "ground truth"));
                        } catch (PersistentObjectException e) {
                            ErrorHandling.error("PersistentObjectException adding getGroundTruthLinksOnSymmetric");
                        }
                    }
            }
        }
        return links;
    }

    protected int getNumberOfGroundTruthLinksOnSiblingNonSymmetric(int r1FatherID, int r1MotherID, int r2FatherID, int r2MotherID) {

        Map<String, ArrayList<LXP>> records1GroupedByFamilyID = new HashMap<>();
        for(LXP record1 : getSourceRecords1()) {

            String famID = record1.getString(r1FatherID).trim() + "-" + record1.getString(r1MotherID).trim();
            if(!famID.equals(""))
                records1GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record1);
        }

        Map<String, ArrayList<LXP>> records2GroupedByFamilyID = new HashMap<>();
        for(LXP record2 : getSourceRecords1()) {

            String famID = record2.getString(r2FatherID).trim() + "-" + record2.getString(r2MotherID).trim();
            if(!famID.equals(""))
                records2GroupedByFamilyID.computeIfAbsent(famID, k -> new ArrayList<>()).add(record2);
        }

        int numberOfLinks = 0;

        for(String famID : records1GroupedByFamilyID.keySet()) {

            ArrayList<LXP> records1 = records1GroupedByFamilyID.get(famID);
            ArrayList<LXP> records2 = records2GroupedByFamilyID.get(famID);

            if(records2 != null) {
                numberOfLinks += records1.size() * records2.size();
            }
        }
        return numberOfLinks;
    }

    public abstract Map<String, Link> getGroundTruthLinks();

    public abstract int numberOfGroundTruthTrueLinks();

    public void makeLinksPersistent(Iterable<Link> links) {
        makePersistentUsingStorr(store_path, results_repository_name, links_persistent_name, links);
    }

    public void makeLinkPersistent(Link link) {
        makePersistentUsingStorr(store_path, results_repository_name, links_persistent_name, link);
    }

    public Iterable<Link> getLinksMade() { // this only works if you chose to persist the links
        try {
            IRepository repo = new Store(store_path).getRepository(results_repository_name);
            IBucket<Link> bucket = repo.getBucket(links_persistent_name, Link.class);
            return bucket.getInputStream();
        } catch (RepositoryException | BucketException e) {
            throw new RuntimeException("No made links repo found when expected -" +
                    " make sure your made the repo you're trying to access");
        }
    }

    //////////////////////// Private ///////////////////////

    private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(originalIdField(record1));
        String s2 = record2.getString(originalIdField(record2));

        if(isSymmetric() && s1.compareTo(s2) == 0)
            return null;

        if(isSymmetric() && s1.compareTo(s2) > 0) {
            return s2 + "-" + s1; // this reordering prevents us putting the same link in opposite directions in the map - it will only be put in once
        } else {
            return s1 + "-" + s2;
        }
    }

    private int originalIdField(LXP record) {
        if(record instanceof Birth)
            return Birth.ORIGINAL_ID;
        if(record instanceof Marriage)
            return Marriage.ORIGINAL_ID;
        if(record instanceof Death)
            return Death.ORIGINAL_ID;

        throw new Error("Record of unknown type: " + record.getClass().getCanonicalName());
    }

    private Map<String, IBucket> storeRepoBucketLookUp = new HashMap<>();

    private IBucket getBucket(Path store_path, String results_repo_name, String bucket_name) {

        IBucket bucket = storeRepoBucketLookUp.get(getSRBString(store_path, results_repo_name, bucket_name));
        if(bucket == null) {

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
                    bucket = results_repository.makeBucket(bucket_name, BucketKind.DIRECTORYBACKED, Link.class );
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

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Link link) {

        try {
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
                f.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Link l : links) {
                bw.write("Role1:\t" + l.getRole1() + "\tRole2:\t" + l.getRole2() + "\tid1:\t" + l.getRecord1().getReferend().getId() + "\tid2:\t" + l.getRecord2().getReferend().getId() + "\tprovenance:\t" + combineProvenance(l.getProvenance()));
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BucketException e) {
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

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, int[] filterOn, int reqPopulatedFields) {
        Collection<LXP> filteredDeathRecords = new HashSet<>();

        records.forEach(record -> {
            int allowedEmptyFieldsRemaining = filterOn.length - reqPopulatedFields;
            for(int attribute : filterOn) {
                String value = record.getString(attribute).toLowerCase().trim();
                if(value.equals("") || value.equals("missing"))
                    if(--allowedEmptyFieldsRemaining <= 0)
                        return;
            }
            filteredDeathRecords.add(record);
        });

        return filteredDeathRecords;
    }

    protected Iterable<LXP> filterSourceRecords(Iterable<LXP> records, int[] filterOn) {
        return filterSourceRecords(records, filterOn, filterOn.length);
    }

    public static void showLXP(LXP lxp) {
        System.out.println(lxp.getString(Birth.FORENAME) + " " + lxp.getString(Birth.SURNAME) + " // "
                + lxp.getString(Birth.FATHER_FORENAME) + " " + lxp.getString(Birth.FATHER_SURNAME) + " " + lxp.getString(Birth.FAMILY));
    }

}
