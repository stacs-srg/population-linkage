package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class DeathDeathSiblingLinkageRecipe extends LinkageRecipe {

    public DeathDeathSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String b1_mother_id = record1.getString(Death.MOTHER_IDENTITY);
        final String b2_mother_id = record2.getString(Death.MOTHER_IDENTITY);

        final String b1_father_id = record1.getString(Death.FATHER_IDENTITY);
        final String b2_father_id = record2.getString(Death.FATHER_IDENTITY);

        if (!b1_mother_id.isEmpty() && b1_mother_id.equals(b2_mother_id) && !b1_father_id.isEmpty() && b1_father_id.equals(b2_father_id)) return LinkStatus.TRUE_MATCH;

        if ( b1_mother_id.isEmpty() && b2_mother_id.isEmpty() &&
                b1_father_id.isEmpty() && b2_father_id.isEmpty() ) return LinkStatus.UNKNOWN;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between deceased on death records";
    }

    @Override
    public String getSourceType1() {
        return "deaths";
    }

    @Override
    public String getSourceType2() {
        return "deaths";
    }

    @Override
    public String getRole1() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getRole2() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields1() {
        return Constants.SIBLING_BUNDLING_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getLinkageFields2() {
        return Constants.SIBLING_BUNDLING_DEATH_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingSymmetric(Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    @Override
    public int numberOfGroundTruthTrueLinks() { // See comment above
        return getNumberOfGroundTruthLinksOnSiblingSymmetric(Death.FATHER_IDENTITY, Death.MOTHER_IDENTITY);
    }

    private Iterable<LXP> prefilteredRecords = null;

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords1() {
        if(prefilteredRecords == null) { // we do this for symmetric linkage recipes as it ensures the iterables
            // returned by this method and the one for records 2 is the same object - this is required by the
            // implementation of similarity search - otherwise we link to people to themselves
            prefilteredRecords = filterSourceRecords(getSourceRecords1(), new int[]{
                            Death.FATHER_FORENAME, Death.FATHER_SURNAME,
                            Death.MOTHER_FORENAME, Death.MOTHER_MAIDEN_SURNAME},
                    3);
        }
        return prefilteredRecords;
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return getPreFilteredSourceRecords1();
    }
}
