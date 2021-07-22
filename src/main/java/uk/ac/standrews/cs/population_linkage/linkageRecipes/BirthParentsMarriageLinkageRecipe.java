/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.List;
import java.util.Map;

/**
 * EvidencePair Recipe
 * In all linkage recipies the naming convention is:
 *     the stored type is the first part of the name
 *     the query type is the second part of the name
 * So for example in BirthBrideIdentityLinkageRecipe the stored type (stored in the search structure) is a birth and Marriages are used to query.
 * In all recipes if the query and the stored types are not the same the query type is converted to a stored type using getQueryMappingFields() before querying.
 *
 */

public class BirthParentsMarriageLinkageRecipe extends LinkageRecipe {

    private static final double DISTANCE_THESHOLD = 0.4;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Marriage.GROOM_FORENAME,
            Marriage.GROOM_SURNAME,
            Marriage.BRIDE_FORENAME,
            Marriage.BRIDE_SURNAME,
            Marriage.PLACE_OF_MARRIAGE,
            Marriage.MARRIAGE_DAY,
            Marriage.MARRIAGE_MONTH,
            Marriage.MARRIAGE_YEAR
    );

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Marriage.STANDARDISED_ID;

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY )),
            list(pair(Birth.MOTHER_IDENTITY,Marriage.BRIDE_IDENTITY))
    );

    public static final String LINKAGE_TYPE = "birth-parents-marriage-identity";

    public BirthParentsMarriageLinkageRecipe(String source_repository_name, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
    }

    @Override
    public LinkStatus isTrueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage);
    }

    public static LinkStatus trueMatch(LXP birth, LXP marriage) {
        return trueMatch(birth, marriage, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Marriage.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_PARENTS;
    } // mother and father

    @Override
    public String getQueryRole() {
        return Marriage.ROLE_PARENTS;  // bride and groom
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    public static boolean isViable(RecordPair proposedLink) {
        try {
            Birth birth_record = (Birth) proposedLink.record1;
            Marriage marriage_record = (Marriage) proposedLink.record2;
            int yob = Integer.parseInt( birth_record.getString( Birth.BIRTH_YEAR ) );
            int yom = Integer.parseInt( marriage_record.getString( Marriage.MARRIAGE_YEAR ) );

            return yob > yom && yom + LinkageConfig.MAX_MARRIAGE_BIRTH_DIFFERENCE > yob;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public boolean isViableLink(RecordPair proposedLink) {
        return isViable(proposedLink);
    }

    @Override
    public List<Integer> getQueryMappingFields() { return SEARCH_FIELDS; }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksOn(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

    @Override
    public int getNumberOfGroundTruthTrueLinks() {
        return getNumberOfGroundTruthTrueLinksOn(Birth.FATHER_IDENTITY, Marriage.GROOM_IDENTITY);
    }

//    @Override
//    public Map<String, Link> getGroundTruthLinks() { // TODO AL 777 Why not everywhere?
//        Map<String, Link> map = new HashMap<>();
//        for (LXP birth : getBirthRecords()) {
//            for (LXP marriage : getMarriageRecords()) {
//                LinkStatus ls = trueMatch(birth, marriage);
//                if (ls.equals(LinkStatus.TRUE_MATCH)) {
//                    try {
//                        Link l = new Link(birth, getStoredRole(), marriage, getQueryRole(), 1.0f, "GT", 0.0, "GT");
//                        map.put(l.toString(), l);
//                    } catch (PersistentObjectException e) {
//                        ErrorHandling.error("PersistentObjectException adding getGroundTruthLinks");
//                    }
//                }
//            }
//        }
//        return map;
//    }
//
//    public int getNumberOfGroundTruthTrueLinks() {
//
//        int count = 0;
//
//        for (LXP birth : getBirthRecords()) {
//            for(LXP marriage : getMarriageRecords()) {
//                if( trueMatch(birth,marriage).equals(LinkStatus.TRUE_MATCH) ) {
//                    count++;
//                }
//            }
//        }
//        return count;
//    }

    @Override
    public double getTheshold() {
        return DISTANCE_THESHOLD;
    }
}
