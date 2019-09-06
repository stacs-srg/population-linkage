package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

import static uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus.TRUE_MATCH;

public class BirthFatherIdentityLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> birth_records;

    public BirthFatherIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        birth_records = Utilities.getBirthRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return birth_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() {
        return birth_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return BirthFatherIdentityLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public String getDatasetName() {
        return "Rubbish this is";
    } // TODO delete or clean this up

    @Override
    public String getLinkageType() {
        return "identity bundling between babies on birth records and fathers on birth records - same person in roles of baby and father";
    }

    @Override
    public String getSourceType1() {
        return "births";
    }

    @Override
    public String getSourceType2() {
        return "births";
    }

    @Override
    public String getRole1() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getRole2() {
        return Birth.ROLE_FATHER;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.BIRTH_FATHER_BABY_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BIRTH_FATHER_FATHER_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        final List<LXP> records = new ArrayList<>();

        for (LXP lxp : record_repository.getBirths()) {
            records.add(lxp);
        }

        final int number_of_records = records.size();

        for (int i = 0; i < number_of_records; i++) {

            for (int j = i + 1; j < number_of_records; j++) {

                LXP record1 = records.get(i);
                LXP record2 = records.get(j);


                try {
                    if (isTrueMatch(record1, record2).equals(TRUE_MATCH)) {

                        Link l = new Link(record1, Birth.ROLE_BABY, record2, Birth.ROLE_FATHER, 1.0f, "ground truth");
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

        for(LXP birth1 : record_repository.getBirths()) {

            for (LXP birth2 : record_repository.getBirths()) {

                if( birth1.getString(Birth.CHILD_IDENTITY).equals(birth2.getString(Birth.FATHER_IDENTITY) ) ) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        Collection<LXP> filteredBirthRecords = new HashSet<>();

        for(LXP record : birth_records) {

            String babyForename = record.getString(Birth.FORENAME).trim();
            String babySurname = record.getString(Birth.SURNAME).trim();

            if( ! ( babyForename.equals("") || babyForename.equals("missing") ||
                    babySurname.equals("") || babySurname.equals("missing") ) ) {

                filteredBirthRecords.add(record);
            }
        }
        return filteredBirthRecords;
    }


    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {

        Collection<LXP> filteredBirthRecords = new HashSet<>();

        for(LXP record : birth_records) {

            String fatherForename = record.getString(Birth.FATHER_FORENAME).trim();
            String fatherSurname = record.getString(Birth.FATHER_SURNAME).trim();

            if( ! ( fatherForename.equals("") || fatherForename.equals("missing") ||
                    fatherSurname.equals("") || fatherSurname.equals("missing") ) ) {


                filteredBirthRecords.add(record);
            }
        }
        return filteredBirthRecords;
    }

    private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(Birth.ORIGINAL_ID);
        String s2 = record2.getString(Birth.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {

        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY);
        final String b2_father_id = record2.getString(Birth.FATHER_IDENTITY);

        if (b1_baby_id.isEmpty() || b2_father_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (b1_baby_id.equals( b2_father_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
