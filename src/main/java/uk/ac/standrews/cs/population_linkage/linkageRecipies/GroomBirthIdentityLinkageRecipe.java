package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

public class GroomBirthIdentityLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> birth_records;
    private final Iterable<LXP> marriage_records;

    public GroomBirthIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        birth_records = Utilities.getBirthRecords(record_repository);
        marriage_records = Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() { return marriage_records; }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return birth_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return GroomBirthIdentityLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between grooms on marriage records and babies on birth records - same person in roles of groom and baby";
    }

    @Override
    public String getSourceType1() {
        return "marriages";
    }

    @Override
    public String getSourceType2() {
        return "births";
    }

    @Override
    public String getRole1() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public String getRole2() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields1() { return Constants.GROOM_IDENTITY_LINKAGE_FIELDS ;}

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BABY_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();
        Map<String, LXP> marriageRecords = new HashMap<>();

        for (LXP marriage_record : record_repository.getMarriages()) {
            marriageRecords.put(marriage_record.getString(Marriage.GROOM_IDENTITY), marriage_record);
        }

        for (LXP birthRecord : birth_records) {
            marriageRecords.computeIfPresent(birthRecord.getString(Birth.CHILD_IDENTITY), (k, marriageRecord) -> {
                try {
                    Link l = new Link(birthRecord, Marriage.ROLE_GROOM, marriageRecord, Birth.ROLE_BABY, 1.0f, "ground truth");
                    String linkKey = toKey(birthRecord, marriageRecord);
                    links.put(linkKey, l);
                } catch (PersistentObjectException e) {
                    ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                }
                return marriageRecord;
            });
        }

        return links;
    }

    public int numberOfGroundTruthTrueLinks() {

        int count = 0;

        for(LXP marriage : record_repository.getMarriages()) {

            for (LXP birth2 : record_repository.getBirths()) {

                if( marriage.getString(Marriage.GROOM_IDENTITY).equals(birth2.getString(Birth.CHILD_IDENTITY) ) ) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        Collection<LXP> filteredMarriageRecords = new HashSet<>();

        for(LXP record : marriage_records) {

            String groomForename = record.getString(Marriage.GROOM_FORENAME).trim();
            String groomSurname = record.getString(Marriage.GROOM_SURNAME).trim();
            String fathersForename = record.getString(Marriage.GROOM_FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Marriage.GROOM_FATHER_SURNAME).trim();
            String mothersForename = record.getString(Marriage.GROOM_MOTHER_FORENAME).trim();
            String mothersSurname = record.getString(Marriage.GROOM_MOTHER_MAIDEN_SURNAME).trim();

            int populatedFields = 0;

            if (!(groomForename.equals("") || groomForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(groomSurname.equals("") || groomSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(fathersForename.equals("") || fathersForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(fathersSurname.equals("") || fathersSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(mothersForename.equals("") || mothersForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(mothersSurname.equals("") || mothersSurname.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredMarriageRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredMarriageRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 3;
    }


    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {

        HashSet<LXP> filteredBirthRecords = new HashSet<>();

        for (LXP record : birth_records) {

            String childsForename = record.getString(Birth.FORENAME).trim();
            String childsSurname = record.getString(Birth.SURNAME).trim();
            String fathersForename = record.getString(Birth.FATHER_FORENAME).trim();
            String fathersSurname = record.getString(Birth.FATHER_SURNAME).trim();
            String mothersForename = record.getString(Birth.MOTHER_FORENAME).trim();
            String mothersSurname = record.getString(Birth.MOTHER_MAIDEN_SURNAME).trim();

            int populatedFields = 0;

            if (!(childsForename.equals("") || childsForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(childsSurname.equals("") || childsSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(fathersForename.equals("") || fathersForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(fathersSurname.equals("") || fathersSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(mothersForename.equals("") || mothersForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(mothersSurname.equals("") || mothersSurname.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredBirthRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredBirthRecords;
    }

    private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(Marriage.ORIGINAL_ID);
        String s2 = record2.getString(Birth.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String groom_id = record1.getString(Marriage.GROOM_IDENTITY);
        final String baby_id = record2.getString(Birth.CHILD_IDENTITY);

        if (groom_id.isEmpty() || baby_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (groom_id.equals( baby_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
