package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> birth_records;
    private final Iterable<LXP> death_records;

    public BirthDeathIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

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
        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY);
        final String d2_deceased_id = record2.getString(Death.DECEASED_IDENTITY);

        if (b1_baby_id.isEmpty() || d2_deceased_id.isEmpty() ) {
            return LinkStatus.UNKNOWN;
        } else if (b1_baby_id.equals( d2_deceased_id ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return "identity links between babies on birth records and deceased on death records - same person in roles of baby and deceased";
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
        return Constants.BABY_IDENTITY_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        final Map<String, LXP> deathRecords = new HashMap<>();

        for (LXP lxp : record_repository.getDeaths()) {
            deathRecords.put(lxp.getString(Death.DECEASED_IDENTITY), lxp);
        }

        for(LXP birthRecord : record_repository.getBirths()) {
            deathRecords.computeIfPresent(birthRecord.getString(Birth.CHILD_IDENTITY), (k, deathRecord) -> {
                try {
                    Link l = new Link(birthRecord, Birth.ROLE_BABY, deathRecord, Death.ROLE_DECEASED, 1.0f, "ground truth");
                    String linkKey = toKey(birthRecord, deathRecord);
                    links.put(linkKey, l);
                } catch (PersistentObjectException e) {
                    ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                }
                return deathRecord;
            });
        }
        return links;
    }

    @Override
    public int numberOfGroundTruthTrueLinks() {
        return getGroundTruthLinks().size();
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        return filterSourceRecords(birth_records, new int[]{Birth.FORENAME, Birth.SURNAME});
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterSourceRecords(death_records, new int[]{Death.FORENAME, Death.SURNAME});
    }

    private String toKey(LXP record1, LXP record2) {
        String s1 = record1.getString(Birth.CHILD_IDENTITY);
        String s2 = record2.getString(Death.DECEASED_IDENTITY);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }
}
