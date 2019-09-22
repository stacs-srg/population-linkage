package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

public class DeathBrideOwnMarriageLinkageRecipe extends LinkageRecipe {

    private final Iterable<LXP> death_records;
    private final Iterable<LXP> marriage_records;

    public DeathBrideOwnMarriageLinkageRecipe(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
        death_records = Utilities.getDeathRecords(record_repository);
        marriage_records = Utilities.getMarriageRecords(record_repository);
    }

    @Override
    public Iterable<LXP> getSourceRecords1() {
        return death_records;
    }

    @Override
    public Iterable<LXP> getSourceRecords2() { return marriage_records; }


    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return DeathBrideOwnMarriageLinkageRecipe.trueMatch(record1, record2);
    }

    @Override
    public String getLinkageType() {
        return "identity bundling between deaths and brides own marriage";
    }

    @Override
    public String getSourceType1() {
        return "deaths";
    }

    @Override
    public String getSourceType2() {
        return "marriages";
    }

    @Override
    public String getRole1() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getRole2() { return Marriage.ROLE_BRIDE; }

    @Override
    public List<Integer> getLinkageFields1() { return Constants.DEATH_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public List<Integer> getLinkageFields2() { return Constants.BRIDE_IDENTITY_LINKAGE_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        for (LXP marriage_record : record_repository.getMarriages()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage_record );

            for (LXP birth_record : death_records) {

                String birth_key_from_marriage = toKeyFromDeath( birth_record );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
                    try {
                        Link l = new Link(marriage_record, Death.ROLE_DECEASED, birth_record, Marriage.ROLE_BRIDE, 1.0f, "ground truth");
                        links.put(l.toString(), l);
                    } catch (PersistentObjectException e) {
                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                    }
                }
            }
        }

        return links;
    }

    private static String toKeyFromDeath(LXP birth_record) {
        return  birth_record.getString(Birth.FATHER_IDENTITY ) +
                "-" + birth_record.getString(Birth.MOTHER_IDENTITY ) +
                "-" + birth_record.getString(Birth.CHILD_IDENTITY );
    }

    private static String toKeyFromMarriage(LXP marriage_record) {   //TODO is this OK?
        return  marriage_record.getString(Marriage.BRIDE_FATHER_IDENTITY ) +
                "-" + marriage_record.getString(Marriage.BRIDE_MOTHER_IDENTITY ) +
                "-" + marriage_record.getString(Marriage.BRIDE_IDENTITY );
    }

    public int numberOfGroundTruthTrueLinks() {  // TODO can we do a hashmap to avoid double nexted loop?

        int count = 0;

        for(LXP marriage : record_repository.getMarriages()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage );

            for (LXP death : record_repository.getBirths()) {

                String birth_key_from_marriage = toKeyFromDeath( death );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {

        HashSet<LXP> filteredBirthRecords = new HashSet<>();

        for (LXP record : death_records) {

            String fatherForename = record.getString(Death.FATHER_FORENAME).trim();
            String fatherSurname = record.getString(Death.FATHER_SURNAME).trim();
            String motherForename = record.getString(Death.MOTHER_FORENAME).trim();
            String motherSurname = record.getString(Death.MOTHER_MAIDEN_SURNAME).trim();
            String forename  = record.getString(Death.FORENAME).trim();
            String surname  = record.getString(Death.SURNAME).trim();

            int populatedFields = 0;

            if (!(fatherForename.equals("") || fatherForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(fatherSurname.equals("") || fatherSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(motherForename.equals("") || motherForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(motherSurname.equals("") || motherSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(forename.equals("") || forename.equals("missing"))) {
                populatedFields++;
            }
            if (!(surname.equals("") || surname.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredBirthRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredBirthRecords;
    }


    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {

        Collection<LXP> filteredMarriageRecords = new HashSet<>();

        for(LXP record :  marriage_records) {

            String brideFForename = record.getString(Marriage.BRIDE_FATHER_FORENAME).trim();
            String brideFSurname = record.getString(Marriage.BRIDE_FATHER_SURNAME).trim();
            String brideMForename = record.getString(Marriage.BRIDE_MOTHER_FORENAME).trim();
            String brideMSurname = record.getString(Marriage.BRIDE_MOTHER_MAIDEN_SURNAME).trim();
            String forename  = record.getString(Marriage.BRIDE_FORENAME).trim();
            String surname  = record.getString(Marriage.BRIDE_SURNAME).trim();

            int populatedFields = 0;

            if (!(brideFForename.equals("") || brideFForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideFSurname.equals("") || brideFSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideMForename.equals("") || brideMForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideMSurname.equals("") || brideMSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(forename.equals("") || forename.equals("missing"))) {
                populatedFields++;
            }
            if (!(surname.equals("") || surname.equals("missing"))) {
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

    public static LinkStatus trueMatch(LXP death, LXP marriage) {

        if(     death.getString( Death.FATHER_IDENTITY ).isEmpty()  ||
                death.getString( Death.MOTHER_IDENTITY ).isEmpty()  ||
                death.getString( Death.DECEASED_IDENTITY ).isEmpty()  ||
                marriage.getString(Marriage.BRIDE_FATHER_IDENTITY ).isEmpty() ||
                marriage.getString(Marriage.BRIDE_MOTHER_IDENTITY ).isEmpty() ||
                marriage.getString(Marriage.BRIDE_IDENTITY ).isEmpty() ) {

                    return LinkStatus.UNKNOWN;

        }

        String key_from_death = toKeyFromDeath( death );
        String key_from_marriage = toKeyFromMarriage( marriage );

        if (key_from_death.equals( key_from_marriage ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
