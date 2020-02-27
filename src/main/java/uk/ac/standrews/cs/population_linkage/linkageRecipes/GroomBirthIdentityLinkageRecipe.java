package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.utilities.metrics.JensenShannon;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GroomBirthIdentityLinkageRecipe extends LinkageRecipe {

    public static void main(String[] args) throws BucketException {

        String sourceRepo = args[0]; // e.g. synthetic-scotland_13k_1_clean
        String resultsRepo = args[1]; // e.g. synth_results

        LinkageRecipe linkageRecipe = new GroomBirthIdentityLinkageRecipe(sourceRepo, resultsRepo,
                linkageType + "-links");

        new BitBlasterLinkageRunner()
                .run(linkageRecipe, new JensenShannon(2048), 0.67, true, 5, false, false, true, false
                );
    }

    public static final String linkageType = "groom-birth-identity";

    public GroomBirthIdentityLinkageRecipe(String source_repository_name, String results_repository_name, String links_persistent_name) {
        super(source_repository_name, results_repository_name, links_persistent_name);
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
        return linkageType;
    }

    @Override
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class getSearchType() {
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_GROOM;
    }

    @Override
    public String getSearchRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
            Marriage.GROOM_FATHER_FORENAME,
            Marriage.GROOM_FATHER_SURNAME,
            Marriage.GROOM_MOTHER_FORENAME,
            Marriage.GROOM_MOTHER_MAIDEN_SURNAME,
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
    }

    public static boolean isViable(RecordPair proposedLink) {
        try {
            int dom = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_DAY));
            int mom = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_MONTH));
            int yom = Integer.parseInt(proposedLink.record1.getString(Marriage.MARRIAGE_YEAR));

            int dob = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_DAY));
            int mob = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_MONTH));
            int yob = Integer.parseInt(proposedLink.record2.getString(Birth.BIRTH_YEAR));

            boolean personAgedOver15AtMarriage = yob + LinkageConfig.MIN_AGE_AT_MARRIAGE <= yom;

            LocalDate birthDate = LocalDate.of(yob, mob, dob);
            LocalDate marriageDate = LocalDate.of(yom, mom, dom);

            int groomsExpectedAge = birthDate.until(marriageDate).getYears();
            int groomAge = Integer.parseInt(proposedLink.record1.getString(Marriage.GROOM_AGE_OR_DATE_OF_BIRTH));

            boolean groomOfExpectedAge = Math.abs(groomAge - groomsExpectedAge) < 10;

            return personAgedOver15AtMarriage && groomOfExpectedAge; // is person at least 15 on marriage date

        } catch(NumberFormatException e) { // in this case a BIRTH_YEAR or MARRIAGE_YEAR or GROOM_AGE_OR_DATE_OF_BIRTH is invalid
            return true;
        }
    }

    @Override
    public List<Integer> getSearchMappingFields() {
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
    public Iterable<LXP> getPreFilteredSearchRecords() {
        return filterBySex(
                super.getPreFilteredSearchRecords(),
                Birth.SEX, "m")
        ;
    }
}
