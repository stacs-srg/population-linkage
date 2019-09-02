package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Role;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class BrideBirthIdentityLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> birth_records;
    private final Iterable<LXP> marriage_records;

    public BrideBirthIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        birth_records = Utilities.getBirthRecords(record_repository);
        marriage_records = Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return birth_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return marriage_records;
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
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        // NOTE by TOM - I've changed stuff in this method (set to maps) this may change the context of your todos
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
                            Link l = new Link(birth_record, Birth.ROLE_BABY, marriage_record, Marriage.ROLE_BRIDE, 1.0f, "ground truth");

                            links.put(l.toString(), l);
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
    public int numberOfGroundTruthTrueLinks() {

        int c = 0;

        List<LXP> marriageRecords = new ArrayList<>();
        for(LXP marriage : record_repository.getMarriages()) {
            marriageRecords.add(marriage);
        }

        for(LXP birth : record_repository.getBirths()) {
            for(LXP marriage : marriageRecords) {
                if(isTrueMatch(birth, marriage).equals(TRUE_MATCH))
                    c++;
            }
        }

        return c;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        HashSet<LXP> filteredBirthRecords = new HashSet<>();

        for(LXP record : birth_records) {

            String childsForename = record.getString(Birth.FORENAME).trim();
            String childsSurname = record.getString(Birth.SURNAME).trim();
            String fathersForename = record.getString(Birth.FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Birth.FATHER_SURNAME).trim();
            String mothersForename = record.getString(Birth.MOTHER_FORENAME).trim();
            String mothersSurname = record.getString(Birth.MOTHER_MAIDEN_SURNAME).trim();

            if(!(childsForename.equals("") || childsForename.equals("missing") ||
                    childsSurname.equals("") || childsSurname.equals("missing") ||
                    fathersForename.equals("") || fathersForename.equals("missing") ||
                    fathersSurname.equals("") || fathersSurname.equals("missing") ||
                    mothersForename.equals("") || mothersForename.equals("missing")||
                    mothersSurname.equals("") || mothersSurname.equals("missing"))) {
                // no key info is missing - so we'll consider this record

                filteredBirthRecords.add(record);
            } // else reject record for linkage - not enough info
        }

        return filteredBirthRecords;

    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        HashSet<LXP> filteredMarriageRecords = new HashSet<>();

        for(LXP record : marriage_records) {

            String bridesForename = record.getString(Marriage.BRIDE_FORENAME).trim();
            String bridesSurname = record.getString(Marriage.BRIDE_SURNAME).trim();
            String fathersForename = record.getString(Marriage.BRIDE_FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Marriage.BRIDE_FATHER_SURNAME).trim();
            String mothersForename = record.getString(Marriage.BRIDE_MOTHER_FORENAME).trim();
            String mothersSurname = record.getString(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME).trim();

            if(!(bridesForename.equals("") || bridesForename.equals("missing") ||
                    bridesSurname.equals("") || bridesSurname.equals("missing") ||
                    fathersForename.equals("") || fathersForename.equals("missing") ||
                    fathersSurname.equals("") || fathersSurname.equals("missing") ||
                    mothersForename.equals("") || mothersForename.equals("missing")||
                    mothersSurname.equals("") || mothersSurname.equals("missing"))) {
                // no key info is missing - so we'll consider this record

                filteredMarriageRecords.add(record);
            } // else reject record for linkage - not enough info
        }

        return filteredMarriageRecords;
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
