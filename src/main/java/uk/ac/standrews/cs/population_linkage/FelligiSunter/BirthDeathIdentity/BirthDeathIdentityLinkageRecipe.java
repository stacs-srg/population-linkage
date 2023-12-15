/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.FelligiSunterDistance;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageConfig;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.helpers.RecordFiltering.filter;

/**
 * Links a person appearing as the child on a birth record with the same person appearing as the deceased on a death record.
 */
public class BirthDeathIdentityLinkageRecipe extends LinkageRecipe {

    public static double TOP_OF_BEST_F = 0.0089;
    public static double THRESHOLD = 0.01;  // a bit more than above!

    public static final String LINKAGE_TYPE = "birth-death-identity-feligi-sunter";

    private final int number_of_deaths;

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Death.STANDARDISED_ID;

    private List<LXP> cached_records = null;

    public int ALL_LINKAGE_FIELDS = 6;

    private final List<Double> m_priors;
    private final List<Double> u_priors;
    private final double odds_prior;

    // Don't use father/mother occupation due to likely long duration between birth and death events.

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Death.FORENAME,
            Death.SURNAME,
            Death.MOTHER_FORENAME,
            Death.MOTHER_MAIDEN_SURNAME,
            Death.FATHER_FORENAME,
            Death.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Death.DECEASED_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Death.BIRTH_RECORD_IDENTITY)),
            list(pair(Birth.DEATH_RECORD_IDENTITY, Death.STANDARDISED_ID))
    );

    public BirthDeathIdentityLinkageRecipe(String source_repository_name, String number_of_records,  List<Double> m_priors, List<Double> u_priors, double odds_prior, String links_persistent_name) {
        super(source_repository_name, links_persistent_name);
        this.m_priors = m_priors;
        this.u_priors = u_priors;
        this.odds_prior = odds_prior;
        if (number_of_records.equals(EVERYTHING_STRING)) {
            number_of_deaths = EVERYTHING;
        } else {
            number_of_deaths = Integer.parseInt(number_of_records);
        }
        setNumberOfLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
    }

    @Override
    protected Iterable<LXP> getDeathRecords() {
        if (cached_records == null) {
            System.out.println("Filtering death records require: " + getNumberOfLinkageFieldsRequired() + " fields");
            cached_records = filter(number_of_deaths, super.getDeathRecords(), getQueryMappingFields(), getNumberOfLinkageFieldsRequired());
        }
        return cached_records;
    }

    @Override
    public LinkStatus isTrueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2);
    }

    public static LinkStatus trueMatch(LXP record1, LXP record2) {
        return trueMatch(record1, record2, TRUE_MATCH_ALTERNATIVES);
    }

    @Override
    public String getLinkageType() {
        return LINKAGE_TYPE;
    }

    @Override
    public Class<? extends LXP> getStoredType() {
        return Birth.class;
    }

    @Override
    public Class<? extends LXP> getQueryType() {
        return Death.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Death.ROLE_DECEASED;
    }

    @Override
    public List<Integer> getLinkageFields() {
        return LINKAGE_FIELDS;
    }

    @Override
    public boolean isViableLink(final LXP record1, final LXP record2) {
        return isViable(record1, record2);
    }

    /**
     * Checks whether the birth occurs before the death, that the age at death is plausible, and that the ages at death
     * derived in three different ways are consistent (recorded on death record; difference between birth year on birth
     * record and death year; difference between birth year on death record and death year).
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP birth_record, final LXP death_record) {

        try {
            final LocalDate date_of_birth_from_birth_record = CommonLinkViabilityLogic.getBirthDateFromBirthRecord(birth_record);
            final LocalDate date_of_birth_from_death_record = CommonLinkViabilityLogic.getBirthDateFromDeathRecord(death_record);
            final LocalDate date_of_death_from_death_record = CommonLinkViabilityLogic.getDeathDateFromDeathRecord(death_record);

            final int age_at_death_recorded_on_death_record = Integer.parseInt(death_record.getString(Death.AGE_AT_DEATH));

            final long age_at_death_calculated_from_both_records = date_of_birth_from_birth_record.until(date_of_death_from_death_record, ChronoUnit.YEARS);
            final long age_at_death_calculated_from_death_record = date_of_birth_from_death_record.until(date_of_death_from_death_record, ChronoUnit.YEARS);

            final long age_at_death_discrepancy_1 = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_both_records);
            final long age_at_death_discrepancy_2 = Math.abs(age_at_death_recorded_on_death_record - age_at_death_calculated_from_death_record);

            return age_at_death_calculated_from_both_records >= 0 &&
                    age_at_death_calculated_from_both_records <= LinkageConfig.MAX_AGE_AT_DEATH &&
                    age_at_death_discrepancy_1 <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY &&
                    age_at_death_discrepancy_2 <= LinkageConfig.MAX_ALLOWABLE_AGE_DISCREPANCY;

        } catch (NumberFormatException e) { // Invalid year.
            return true;
        }
    }

    @Override
    public List<Integer> getQueryMappingFields() {
        return SEARCH_FIELDS;
    }

    @Override
    public Map<String, Link> getGroundTruthLinks() {
        return getGroundTruthLinksAsymmetric();
    }

    @Override
    public long getNumberOfGroundTruthTrueLinks() {
        int count = 0;
        for (LXP query_record : getQueryRecords()) {
            count += countBirthDeathIdentityGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GT_ID, { actors: \"Child-Deceased\" } ]-(b:Death) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";
    private static final String BIRTH_DEATH_ALL_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GT_ID, { actors: \"Child-Deceased\" } ]-(b:Death) RETURN r";

    public static List<Relationship> getBirthDeathIdentityGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {
        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, parameters);
        return result.list(r -> r.get("r").asRelationship());
    }

    public static List<Relationship> getAllBirthDeathIdentityGTLinks(NeoDbCypherBridge bridge) {
        Result result = bridge.getNewSession().run(BIRTH_DEATH_ALL_GT_IDENTITY_LINKS_QUERY);
        return result.list(r -> r.get("r").asRelationship());
    }

    public static int countBirthDeathIdentityGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {
        return getBirthDeathIdentityGTLinks(bridge, birth_record).size();
    }

//    @Override
//    public double getThreshold() {
//        return THRESHOLD;
//    }
//
//    @Override
//    public LXPMeasure getCompositeMeasure() {
//        return new FelligiSunterDistance(getLinkageFields(), SEARCH_FIELDS, getBaseMeasure(), m_priors, u_priors, odds_prior);
//    }

    public LXPMeasure getCompositeMeasure(StringMeasure base_measure) {
        return new FelligiSunterDistance(getLinkageFields(), SEARCH_FIELDS, base_measure, m_priors, u_priors, odds_prior );
    }
}
