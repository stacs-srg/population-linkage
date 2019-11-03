package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;

import java.util.*;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

public class DeathGroomOwnMarriageIdentityLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new DeathGroomOwnMarriageIdentityLinkageRecipe(sourceRepo, resultsRepo,
                linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "death-groom-identity";

    public DeathGroomOwnMarriageIdentityLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP death, LXP marriage) {
        String deceasedID = death.getString(Death.DECEASED_IDENTITY).trim();
        String groomID = marriage.getString(Marriage.GROOM_IDENTITY).trim();

        if(deceasedID.isEmpty() || groomID.isEmpty()) {
            return LinkStatus.UNKNOWN;
        }

        if (deceasedID.equals(groomID) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }

    @Override
    public String getLinkageType() {
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Death.class;
    }

    @Override
    public Class getSearchType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public String getSearchRole() { return Marriage.ROLE_GROOM; }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Death.FORENAME,
                Death.SURNAME,
                Death.SPOUSE_NAMES,
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        try {
            int yod = Integer.parseInt(proposedLink.record1.getString(Death.DEATH_YEAR));
            int yom = Integer.parseInt(proposedLink.record2.getString(Marriage.MARRIAGE_YEAR));

            return yod >= yom; // is death after marriage

        } catch(NumberFormatException e) { // in this case a DEATH_YEAR or MARRIAGE_YEAR is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
        return Arrays.asList(
                Marriage.GROOM_FORENAME,
                Marriage.GROOM_SURNAME,
                Marriage.BRIDE_FULL_NAME,
                Marriage.GROOM_FATHER_FORENAME,
                Marriage.GROOM_FATHER_SURNAME,
                Marriage.GROOM_MOTHER_FORENAME,
                Marriage.GROOM_MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Death.DECEASED_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public Iterable<LXP> getPreFilteredStoredRecords() {
        return filterBySex(
                super.getPreFilteredStoredRecords(),
                Birth.SEX, "m");
    }

}
