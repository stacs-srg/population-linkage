/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.archive.ErrorHandling;

import java.util.*;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */
public class ParentsMarriageDeathLinkageRecipe extends LinkageRecipe {

    public static final String LINKAGE_TYPE = "parents-marriage-death-identity";
    private static final double DISTANCE_THESHOLD = 0;

    public ParentsMarriageDeathLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
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
    public Class getStoredType() {
        return Marriage.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Marriage.ROLE_PARENTS;  // bride and groom
    }

    @Override
    public String getQueryRole() {
        return Death.ROLE_MOTHER + Death.ROLE_FATHER;
    } // mother and father

    @Override
    public List<Integer> getLinkageFields() {
        return Arrays.asList(
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME,
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME
        );
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return true;
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return Arrays.asList(
                Death.FATHER_FORENAME,
                Death.FATHER_SURNAME,
                Death.MOTHER_FORENAME,
                Death.MOTHER_MAIDEN_SURNAME
        );
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {

        final Map<String, Link> links = new HashMap<>();

        for (LXP marriage_record : getMarriageRecords()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage_record );

            for (LXP death_record : getDeathRecords()) {

                String birth_key_from_marriage = toKeyFromDeath( death_record );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
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

    private static String toKeyFromDeath(LXP death_record) {
        return  death_record.getString(Death.FATHER_IDENTITY ) +
                "-" + death_record.getString(Death.MOTHER_IDENTITY );
    }

    private static String toKeyFromMarriage(LXP marriage_record) {
        return  marriage_record.getString(Marriage.GROOM_IDENTITY ) +
                "-" + marriage_record.getString(Marriage.BRIDE_IDENTITY );
    }

    public int getNumberOfGroundTruthTrueLinks() {

        int count = 0;

        for(LXP marriage : getMarriageRecords()) {

            String marriage_key_from_marriage = toKeyFromMarriage( marriage );

            for (LXP death : getDeathRecords()) {

                String birth_key_from_marriage = toKeyFromDeath( death );

                if( birth_key_from_marriage.equals( marriage_key_from_marriage ) ) {
                    count++;
                }
            }
        }
        return count;
    }

    public Iterable<LXP> getQueryRecords() {

        Collection<LXP> filteredMarriageRecords = new HashSet<>();

        for(LXP record : getMarriageRecords()) {

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
        String s2= query_record.getString(Birth.ORIGINAL_ID);

        if(s1.compareTo(s2) < 0)
            return s1 + "-" + s2;
        else
            return s2 + "-" + s1;

    }

    @Override
    public double getTheshold() {
        return DISTANCE_THESHOLD;
    }

    public static LinkStatus trueMatch(LXP death, LXP marriage) {

        if(     death.getString( Birth.FATHER_IDENTITY ).isEmpty()  ||
                death.getString( Birth.MOTHER_IDENTITY ).isEmpty()  ||
                marriage.getString(Marriage.GROOM_IDENTITY ).isEmpty() ||
                marriage.getString(Marriage.BRIDE_IDENTITY ).isEmpty() ) {

                    return LinkStatus.UNKNOWN;

        }
        String death_key_from_marriage = toKeyFromDeath( death );
        String marriage_key_from_marriage = toKeyFromMarriage( marriage );

        if (marriage_key_from_marriage.equals( death_key_from_marriage ) ) {
            return LinkStatus.TRUE_MATCH;
        } else {
            return LinkStatus.NOT_TRUE_MATCH;
        }
    }
}
