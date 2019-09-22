package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BirthDeathSiblingLinkageRecipe extends LinkageRecipe {


    private final Iterable<LXP> birth_records;
    private final Iterable<LXP> death_records;

    public BirthDeathSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        birth_records = Utilities.getBirthRecords(record_repository);
        death_records = Utilities.getDeathRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return birth_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return death_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {

        String childFatherID = record1.getString(Birth.FATHER_IDENTITY);
        String childMotherID = record1.getString(Birth.MOTHER_IDENTITY);

        String decFatherID = record2.getString(Death.FATHER_IDENTITY);
        String decMotherID = record2.getString(Death.MOTHER_IDENTITY);

        if(childFatherID.trim().equals("") || childMotherID.trim().equals("") || decFatherID.trim().equals("") || decMotherID.trim().equals(""))
            return LinkStatus.UNKNOWN;

        if(childFatherID.equals(decFatherID) && childMotherID.equals(decMotherID))
            return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "birth-death-sibling";
    }

    @Override
    public String getSourceType1() {
        return "births";
    }

    @Override
    public String getSourceType2() {
        return "deaths";
    }

    @Override
    public String getRole1() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getRole2() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.SIBLING_BUNDLING_BIRTH_TO_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_DEATH_TO_BIRTH_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {

        int c = 0;

        // we put the deaths into memory so that we dont have to 'retrieve' them from the storr for every birth
        Map<String, AtomicInteger> deathRecords = new HashMap<>();
        for(LXP death : record_repository.getDeaths()) {

            String fID = death.getString(Death.FATHER_IDENTITY).trim();
            String mID = death.getString(Death.MOTHER_IDENTITY).trim();

            if(!(fID.equals("") || mID.equals(""))) {
                String key = fID + "|" + mID;
                deathRecords.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
            }
        }

        for(LXP birth : record_repository.getBirths()) {

            String fID = birth.getString(Birth.FATHER_IDENTITY).trim();
            String mID = birth.getString(Birth.MOTHER_IDENTITY).trim();

            if(!(fID.equals("") || mID.equals(""))) {
                String key = fID + "|" + mID;
                AtomicInteger records;
                if((records = deathRecords.get(key)) != null) {
                    c += records.get();
                }
            }
        }

        return c;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        HashSet<LXP> filteredBirthRecords = new HashSet<>();

        for(LXP record : birth_records) {

            String fathersForename = record.getString(Birth.FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Birth.FATHER_SURNAME).trim();
            String mothersForename = record.getString(Birth.MOTHER_FORENAME).trim();
            String mothersSurname = record.getString(Birth.MOTHER_MAIDEN_SURNAME).trim();

            int populatedFields = 0;

            if(!(fathersForename.equals("") || fathersForename.equals("missing") ) ) {
                populatedFields++;
            }
            if(!(fathersSurname.equals("") || fathersSurname.equals("missing") ) ) {
                populatedFields++;
            }
            if(!( mothersForename.equals("") || mothersForename.equals("missing") ) ) {
                populatedFields++;
            }
            if(!( mothersSurname.equals("") || mothersSurname.equals("missing") ) ) {
                populatedFields++;
            }

            if( populatedFields >= requiredNumberOfPreFilterFields() ) {
                filteredBirthRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredBirthRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 3;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        HashSet<LXP> filteredDeathRecords = new HashSet<>();

        for(LXP record : death_records) {

            String fathersForename = record.getString(Death.FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Death.FATHER_SURNAME).trim();
            String mothersForename = record.getString(Death.MOTHER_FORENAME).trim();
                String mothersSurname = record.getString(Birth.MOTHER_MAIDEN_SURNAME).trim();

            if(!(fathersForename.equals("") || fathersForename.equals("missing") ||
                    fathersSurname.equals("") || fathersSurname.equals("missing") ||
                    mothersForename.equals("") || mothersForename.equals("missing") ||
                        mothersSurname.equals("") || mothersSurname.equals("missing"))) {
                // no key info is missing - so we'll consider this record

                filteredDeathRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredDeathRecords;
    }
}
