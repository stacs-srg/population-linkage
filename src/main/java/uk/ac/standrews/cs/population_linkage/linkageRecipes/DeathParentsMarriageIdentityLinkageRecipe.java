/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.SumOfFieldDistances;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Links two people appearing as the spouses on a marriage record with the same people appearing as the parents on a death record.
 */
public class DeathParentsMarriageIdentityLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THRESHOLD = 0; // TODO - LOOK AT THIS! ****

    public static final String LINKAGE_TYPE = "parents-marriage-death-identity";

    public static final List<Integer> LINKAGE_FIELDS = list(
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME
    );

    public static final int ID_FIELD_INDEX1 = Death.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    public DeathParentsMarriageIdentityLinkageRecipe(String source_repository_name, String links_persistent_name, NeoDbCypherBridge bridge) {
        super(source_repository_name, links_persistent_name, bridge);
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Death.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Death.ROLE_PARENTS;
    } // mother and father

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_SPOUSES;  // bride and groom
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        for (LXP marriage_record : getMarriageRecords()) {

            String marriage_key_from_marriage = toKeyFromMarriage(marriage_record);

            for (LXP death_record : getDeathRecords()) {

                String birth_key_from_marriage = toKeyFromDeath(death_record);

                if (birth_key_from_marriage.equals(marriage_key_from_marriage)) {
                    try {
                        Link l = new Link(marriage_record, Marriage.ROLE_BRIDE + Marriage.ROLE_GROOM, death_record, Death.ROLE_FATHER + Death.ROLE_MOTHER, 1.0f, "ground truth", -1);
                        links.put(l.toString(), l);
                    } catch (PersistentObjectException e) {
                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
                    }
                }
            }
        }

        return links;
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    /**
     * Checks whether a plausible period has elapsed between the marriage and the child's death.
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP death_record, final LXP marriage_record) {

        try {
            final LocalDate date_of_child_birth = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record);
            final LocalDate date_of_child_death = CommonLinkViabilityLogic.getDeathDateFromDeathRecord(death_record);
            final LocalDate date_of_parents_marriage = CommonLinkViabilityLogic.getMarriageDateFromMarriageRecord(marriage_record);

            final long years_from_marriage_to_birth = date_of_parents_marriage.until(date_of_child_birth, ChronoUnit.YEARS);
            final long years_from_marriage_to_death = date_of_parents_marriage.until(date_of_child_death, ChronoUnit.YEARS);

            return years_from_marriage_to_birth >= LinkageConfig.MIN_MARRIAGE_BIRTH_DIFFERENCE &&
                    years_from_marriage_to_birth <= LinkageConfig.MAX_MARRIAGE_BIRTH_DIFFERENCE &&
                    years_from_marriage_to_death >= LinkageConfig.MIN_MARRIAGE_BIRTH_DIFFERENCE &&
                    years_from_marriage_to_death <= LinkageConfig.MAX_MARRIAGE_BIRTH_DIFFERENCE + LinkageConfig.MAX_AGE_AT_DEATH;

        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static String toKeyFromDeath(LXP death_record) {
        return death_record.getString(Death.FATHER_IDENTITY) +
                "-" + death_record.getString(Death.MOTHER_IDENTITY);
    }

    private static String toKeyFromMarriage(LXP marriage_record) {
        return marriage_record.getString(Marriage.GROOM_IDENTITY) +
                "-" + marriage_record.getString(Marriage.BRIDE_IDENTITY);
    }

    public long getNumberOfGroundTruthTrueLinks() {

        int count = 0;

        for (LXP marriage : getMarriageRecords()) {

            String marriage_key_from_marriage = toKeyFromMarriage(marriage);

            for (LXP death : getDeathRecords()) {

                String birth_key_from_marriage = toKeyFromDeath(death);

                if (birth_key_from_marriage.equals(marriage_key_from_marriage)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Iterable<LXP> getQueryRecords() {

        Collection<LXP> filteredMarriageRecords = new HashSet<>();

        for (LXP record : getMarriageRecords()) {

            String groomForename = record.getString(Marriage.GROOM_FORENAME).trim();
            String groomSurname = record.getString(Marriage.GROOM_SURNAME).trim();
            String brideForename = record.getString(Marriage.BRIDE_FORENAME).trim();
            String brideSurname = record.getString(Marriage.BRIDE_SURNAME).trim();

            int populatedFields = 0;

            if (!(groomForename.equals("") || groomForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(groomSurname.equals("") || groomSurname.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideForename.equals("") || brideForename.equals("missing"))) {
                populatedFields++;
            }
            if (!(brideSurname.equals("") || brideSurname.equals("missing"))) {
                populatedFields++;
            }

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredMarriageRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredMarriageRecords;
    }

    private int requiredNumberOfPreFilterFields() {
        return 4;
    }

    @Override
    public Iterable<LXP> getStoredRecords() {

        HashSet<LXP> filteredDeathRecords = new HashSet<>();

        for (LXP record : getDeathRecords()) {

            String fatherForename = record.getString(Death.FATHER_FORENAME).trim();
            String fatherSurname = record.getString(Death.FATHER_SURNAME).trim();
            String motherForename = record.getString(Death.MOTHER_FORENAME).trim();
            String motherSurname = record.getString(Death.MOTHER_SURNAME).trim();

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

            if (populatedFields >= requiredNumberOfPreFilterFields()) {
                filteredDeathRecords.add(record);
            } // else reject record for linkage - not enough info
        }
        return filteredDeathRecords;
    }

    public String toKey(LXP query_record, LXP stored_record) {
        String s1 = stored_record.getString(Marriage.ORIGINAL_ID);
        String s2 = query_record.getString(Birth.ORIGINAL_ID);

        if (s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new SumOfFieldDistances(getBaseMeasure(), getLinkageFields());
    }

    public static LinkStatus trueMatch(LXP death, LXP marriage) {

        if (death.getString(Birth.FATHER_IDENTITY).isEmpty() ||
                death.getString(Birth.MOTHER_IDENTITY).isEmpty() ||
                marriage.getString(Marriage.GROOM_IDENTITY).isEmpty() ||
                marriage.getString(Marriage.BRIDE_IDENTITY).isEmpty()) {

            return LinkStatus.UNKNOWN;
        }

        String death_key_from_marriage = toKeyFromDeath(death);
        String marriage_key_from_marriage = toKeyFromMarriage(marriage);

        if (marriage_key_from_marriage.equals(death_key_from_marriage)) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
