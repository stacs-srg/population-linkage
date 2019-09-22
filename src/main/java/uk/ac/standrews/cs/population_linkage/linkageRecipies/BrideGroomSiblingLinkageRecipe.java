package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class BrideGroomSiblingLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> marriage_records;

    public BrideGroomSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        marriage_records = Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return marriage_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return marriage_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between groom and bride on marriage records";
    }

    @Override
    public String getSourceType1() {
        return "marriages";
    }

    @Override
    public String getSourceType2() {
        return "marriages";
    }

    @Override
    public String getRole1() { return Marriage.ROLE_BRIDE; }

    @Override
    public String getRole2() { return Marriage.ROLE_GROOM; }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.SIBLING_BUNDLING_BRIDE_MARRIAGE_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_GROOM_MARRIAGE_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        final List<LXP> records = new ArrayList<>();

        for (LXP lxp : record_repository.getMarriages()) {
            records.add(lxp);
        }

        final int number_of_records = records.size();

        for (int i = 0; i < number_of_records; i++) {

            for (int j = i + 1; j < number_of_records; j++) {

                LXP record1 = records.get(i);
                LXP record2 = records.get(j);


                try {
                    if (isTrueMatch(record1, record2).equals(TRUE_MATCH)) {

                        Link l = new Link(record1, Marriage.ROLE_BRIDE, record2, Marriage.ROLE_GROOM, 1.0f, "ground truth");
                        String linkKey = toKey(record1, record2);
                        links.put(linkKey.toString(), l);

                    }
                } catch (PersistentObjectException e) {
                    ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                }
            }
        }

        return links;
    }

    public int numberOfGroundTruthTrueLinks() {

        int count = 0;

        Map<String, AtomicInteger> married_brother_count_per_family = new HashMap<>();
        for(LXP marriage : record_repository.getMarriages()) {

            String fID = marriage.getString(Marriage.BRIDE_FATHER_IDENTITY).trim();
            String mID = marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY).trim();

            if(!(fID.equals("") || mID.equals(""))) {
                String key = fID + "|" + mID;     // This is a unique key for the groom's mother and father
                married_brother_count_per_family.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
            }
        }

        for(LXP marriage : record_repository.getMarriages()) {

            String fID = marriage.getString(Marriage.GROOM_FATHER_IDENTITY).trim();
            String mID = marriage.getString(Marriage.GROOM_MOTHER_IDENTITY).trim();

            if(!(fID.equals("") || mID.equals(""))) {
                String key = fID + "|" + mID;
                AtomicInteger marriages_count;
                if((marriages_count = married_brother_count_per_family.get(key)) != null) { //
                    count += marriages_count.get() - 1; // counting links, so this number is the links from this brother to the other brothers marriages (one less than family size)
                }
            }
        }

        return count / 2 ; // symmetric so don't want to count twice - in above loop we have counted Alice -> Jemma and Jemma -> Alice
    }

    private Collection<LXP> filteredMarriageRecords = null;

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        if (filteredMarriageRecords == null) {

            filteredMarriageRecords = new HashSet<>();

            for (LXP record : marriage_records) {

                String fathersForename = record.getString(Marriage.BRIDE_FATHER_FORENAME).trim();
                String fathersSurname = record.getString(Marriage.BRIDE_FATHER_SURNAME).trim();
                String mothersForename = record.getString(Marriage.BRIDE_MOTHER_FORENAME).trim();
                String mothersSurname = record.getString(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME).trim();

                int populatedFields = 0;

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
        }
        return filteredMarriageRecords;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {

        if(filteredMarriageRecords == null) {

            filteredMarriageRecords = new HashSet<>();

            for(LXP record : marriage_records) {

                String fathersForename = record.getString(Marriage.GROOM_FATHER_FORENAME).trim();
                String fathersSurname = record.getString(Marriage.GROOM_FATHER_SURNAME).trim();
                String mothersForename = record.getString(Marriage.GROOM_MOTHER_FORENAME).trim();
                String mothersSurname = record.getString(Marriage.GROOM_MOTHER_MAIDEN_SURNAME).trim();

                int populatedFields = 0;

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
        }
        return filteredMarriageRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 3;
    }


        private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(Marriage.ORIGINAL_ID);
        String s2 = record2.getString(Marriage.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }


    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String m1_father_id = record1.getString(Marriage.BRIDE_FATHER_IDENTITY);
        final String m1_mother_id = record1.getString(Marriage.BRIDE_MOTHER_IDENTITY);

        final String m2_father_id = record2.getString(Marriage.GROOM_FATHER_IDENTITY);
        final String m2_mother_id = record2.getString(Marriage.GROOM_MOTHER_IDENTITY);

        if (!m1_mother_id.isEmpty() && m1_mother_id.equals(m2_mother_id) && !m1_father_id.isEmpty() && m1_father_id.equals(m2_father_id)) return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }
    
}
