package uk.ac.standrews.cs.population_linkage.linkageRecipies;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;

public class BrideGroomSiblingLinkageRecipe extends LinkageRecipe {

    public BrideGroomSiblingLinkageRecipe(String results_repository_name, String links_persistent_name, String source_repository_name, RecordRepository record_repository) {
        super(results_repository_name, links_persistent_name, source_repository_name, record_repository);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String m1_father_id = record1.getString(Marriage.BRIDE_FATHER_IDENTITY);
        final String m1_mother_id = record1.getString(Marriage.BRIDE_MOTHER_IDENTITY);

        final String m2_father_id = record2.getString(Marriage.GROOM_FATHER_IDENTITY);
        final String m2_mother_id = record2.getString(Marriage.GROOM_MOTHER_IDENTITY);

        if (!m1_mother_id.isEmpty() && m1_mother_id.equals(m2_mother_id) && !m1_father_id.isEmpty() && m1_father_id.equals(m2_father_id)) return LinkStatus.TRUE_MATCH;

        return LinkStatus.NOT_TRUE_MATCH;
    }

    @Override
    public String getLinkageType() {
        return "sibling bundling between groom and bride on marriage records";
    }

    @Override
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() { return Marriage.ROLE_BRIDE; }

    @Override
    public String getSearchRole() { return Marriage.ROLE_GROOM; }

    @Override
    public List<Integer> getLinkageFields() {
        return Constants.SIBLING_BUNDLING_BRIDE_MARRIAGE_LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Constants.SIBLING_BUNDLING_GROOM_MARRIAGE_LINKAGE_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOnSiblingNonSymmetric(
                Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY,
                Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthLinksOnSiblingNonSymmetric(
                Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY,
                Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthLinksPostFilterOnSiblingNonSymmetric(
                Marriage.BRIDE_FATHER_IDENTITY, Marriage.BRIDE_MOTHER_IDENTITY,
                Marriage.GROOM_FATHER_IDENTITY, Marriage.GROOM_MOTHER_IDENTITY);
    }

}
