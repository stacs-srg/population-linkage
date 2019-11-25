package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new BirthDeathIdentityLinkageRecipe(sourceRepo, resultsRepo,
                linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "birth-death-identity";

    public BirthDeathIdentityLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        final String b1_baby_id = record1.getString(Birth.CHILD_IDENTITY).trim();
        final String d2_deceased_id = record2.getString(Death.DECEASED_IDENTITY).trim();

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
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class getSearchType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getSearchRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
                Birth.FATHER_FORENAME,
                Birth.FATHER_SURNAME,
                Birth.MOTHER_FORENAME,
                Birth.MOTHER_MAIDEN_SURNAME,
                Birth.FORENAME,
                Birth.SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable( proposedLink );
    }

    public static boolean isViable(RecordPair proposedLink) {
        try {
            int yob = Integer.parseInt(proposedLink.record1.getString(Birth.BIRTH_YEAR));
            int yod = Integer.parseInt(proposedLink.record2.getString(Death.DEATH_YEAR));

            return yod >= yob; // is year of death later than year of birth

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or DEATH_YEAR is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() { return Arrays.asList(
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FORENAME,
            Death.SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinksPostFilter() {
        return getNumberOfGroundTruthTrueLinksPostFilterOn(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY);
    }

}
