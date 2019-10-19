package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class GroomBirthIdentityLinkageRecipe extends LinkageRecipe {

    public GroomBirthIdentityLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
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
        return getGroundTruthLinksOn(Marriage.GROOM_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Marriage.GROOM_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Marriage.GROOM_IDENTITY, Birth.CHILD_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredSourceRecords2() {
        return filterBySex(
                super.getPreFilteredSourceRecords2(),
                Birth.SEX, "m");
    }
}
