package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.data.Utilities;
import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.nio.file.Path;
import java.util.*;

public class UmeaBrideBirthIdentityLinkage extends Linkage {

    Path store_path = ApplicationProperties.getStorePath();

    private final String results_repository_name;
    private final String links_persistent_name;     // may be a filename or a bucket name.
    private final String gt_persistent_name;
    private final String source_repository_name;
    private final RecordRepository record_repository;

    public UmeaBrideBirthIdentityLinkage(String results_repository_name, String links_persistent_name, String gt_persistent_name, String source_repository_name, RecordRepository record_repository) {
        this.results_repository_name = results_repository_name;
        this.links_persistent_name = links_persistent_name;
        this.gt_persistent_name = gt_persistent_name;
        this.source_repository_name = source_repository_name;
        this.record_repository = record_repository;
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String child_id = record1.getString(Birth.CHILD_IDENTITY);
        final String bride_id = record2.getString(Marriage.BRIDE_IDENTITY);

        if (child_id.isEmpty() || bride_id.isEmpty()) return LinkStatus.UNKNOWN;

        return child_id.equals(bride_id) ? LinkStatus.TRUE_MATCH : LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getDatasetName() {
        return  "Umea";
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between a bride and her birth record";
    }

    @Override
    public String getSourceType1() {
        return "births";
    }

    @Override
    public String getSourceType2() {
        return "marriages";
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Arrays.asList(
                Birth.FORENAME,
                Birth.SURNAME,
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME);
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Arrays.asList(
                Marriage.BRIDE_FORENAME,
                Marriage.BRIDE_SURNAME,
                Marriage.BRIDE_FATHER_FORENAME,
                Marriage.BRIDE_FATHER_SURNAME,
                Marriage.BRIDE_MOTHER_FORENAME,
                Marriage.BRIDE_MOTHER_MAIDEN_SURNAME);
    }

    @Override
    public Role makeRole1(LXP lxp) throws PersistentObjectException {
        return new Role(lxp.getThisRef(), Birth.ROLE_BABY);
    }

    @Override
    public Role makeRole2(LXP lxp) throws PersistentObjectException {
        return new Role(lxp.getThisRef(), Marriage.ROLE_BRIDE);
    }

    @Override
    public Set<Link> getGroundTruthLinks() {

        final Set<Link> links = new HashSet<>();

        // TODO GRAHAM created a temp map in memory
        // TODO GRAHAM do we want to create persistent maps in store, or index the bucket fields.

        Map<String, List<LXP>> child_bride_map = createMap(record_repository.getMarriages());

        for (LXP birth_record : record_repository.getBirths()) {

            String child_id = birth_record.getString(Birth.CHILD_IDENTITY);
            if (is_legal(child_id)) {

                final List<LXP> marriage_records = child_bride_map.get(child_id);

                if (marriage_records != null) {

                    for (LXP marriage_record : marriage_records) {

                        try {

                            links.add(new Link(makeRole1(birth_record), makeRole2(marriage_record), 1.0f, "ground truth"));
                        } catch (PersistentObjectException e) {
                            ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                        }
                    }
                }
            }
        }

        return links;
    }

    @Override
    public void makeLinksPersistent(Iterable<Link> links) {
        makePersistentUsingStor( store_path, results_repository_name, links_persistent_name, links );  // use makePersistentUsingStor or makePersistentUsingFile
    }

    @Override
    public void makeGroundTruthPersistent(Iterable<Link> links) {
        makePersistentUsingStor( store_path, results_repository_name, gt_persistent_name, links ); // use makePersistentUsingStor or makePersistentUsingFile
    }

    /////////////////// Private methods ///////////////////

    private boolean is_legal(String child_id) {
        try {
            return child_id != null && child_id.length() > 0 && Integer.valueOf(child_id) > 0;
        } catch( RuntimeException e ) { // if the string is not an int encoding - TODO GRAHAM - is this OK?
            return false;
        }
    }

    private Map <String, List<LXP>> createMap(Iterable<Marriage> marriages) {
        Map<String, List<LXP>> map = new HashMap<>(); // Maps from child id to marriages for that child as a bride

        for( Marriage marriage : marriages ) {
            String bride_identity = marriage.getString( Marriage.BRIDE_IDENTITY );
            if( is_legal(bride_identity) ) {

                List<LXP> list_marriages = map.get(bride_identity);
                if( list_marriages == null ) {
                    list_marriages = new ArrayList<>();
                }
                list_marriages.add(marriage);
                map.put(bride_identity,list_marriages);
            }
        }

        return map;
    }
}
