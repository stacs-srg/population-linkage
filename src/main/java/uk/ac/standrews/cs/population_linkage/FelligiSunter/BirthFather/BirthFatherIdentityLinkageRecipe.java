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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthFather;

import org.neo4j.driver.Result;
import org.neo4j.driver.types.Relationship;
import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.FelligiSunterDistances;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.CommonLinkViabilityLogic;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.LinkageRecipe;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Links a person appearing as the child on a birth record with the same person appearing as the father on another birth record.
 */
public class BirthFatherIdentityLinkageRecipe extends LinkageRecipe {


    private static final double DISTANCE_THRESHOLD = 0.22; // in file UmeaBirthFatherViabilityPRFByThreshold.csv - looks very low!

    public static final String LINKAGE_TYPE = "birth-father-identity-feligi-sunter";

    public static final int ID_FIELD_INDEX1 = Birth.STANDARDISED_ID;
    public static final int ID_FIELD_INDEX2 = Birth.STANDARDISED_ID;

    public static final int ALL_LINKAGE_FIELDS = 2;

    private final List<Double> m_priors;
    private final List<Double> u_priors;
    private final double odds_prior;

    public static final List<Integer> LINKAGE_FIELDS = list(
            Birth.FORENAME,
            Birth.SURNAME
    );

    public static final List<Integer> SEARCH_FIELDS = list(
            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME
    );

    @SuppressWarnings("unchecked")
    public static final List<List<Pair>> TRUE_MATCH_ALTERNATIVES = list(
            list(pair(Birth.CHILD_IDENTITY, Birth.FATHER_IDENTITY)),
            list(pair(Birth.STANDARDISED_ID, Birth.FATHER_BIRTH_RECORD_IDENTITY))
    );

    public BirthFatherIdentityLinkageRecipe(String source_repository_name, List<Double> m_priors, List<Double> u_priors, double odds_prior, String links_persistent_name) {

        super(source_repository_name, links_persistent_name);
        this.m_priors = m_priors;
        this.u_priors = u_priors;
        this.odds_prior = odds_prior;
        setNumberOfLinkageFieldsRequired(ALL_LINKAGE_FIELDS);
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
        return Birth.class;
    }

    @Override
    public String getStoredRole() {
        return Birth.ROLE_BABY;
    }

    @Override
    public String getQueryRole() {
        return Birth.ROLE_FATHER;
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
     * Checks whether a plausible period has elapsed for the person to be the father.
     *
     * @return true if the link is viable
     */
    public static boolean isViable(final LXP record1, final LXP record2) {

        return CommonLinkViabilityLogic.birthParentIdentityLinkIsViable(record1, record2, false);
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
            count += countBirthFatherIdentityGTLinks(bridge, query_record);
        }
        return count;
    }

    private static final String BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY = "MATCH (a:Birth)-[r:GT_ID { actors: \"Child-Father\" } ]-(b:Birth) WHERE b.STANDARDISED_ID = $standard_id_from RETURN r";

    public static int countBirthFatherIdentityGTLinks(NeoDbCypherBridge bridge, LXP birth_record) {
        String standard_id_from = birth_record.getString(Birth.STANDARDISED_ID);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("standard_id_from", standard_id_from);
        Result result = bridge.getNewSession().run(BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY, parameters);
        List<Relationship> relationships = result.list(r -> r.get("r").asRelationship());
        return relationships.size();
    }

    @Override
    public double getThreshold() {
        return DISTANCE_THRESHOLD;
    }

    @Override
    public LXPMeasure getCompositeMeasure() {
        return new FelligiSunterDistances(getBaseMeasure(), getLinkageFields(), SEARCH_FIELDS, m_priors, u_priors, odds_prior);
    }

    public LXPMeasure getCompositeMeasure(StringMeasure base_measure) {
        return new FelligiSunterDistances(base_measure, getLinkageFields(), SEARCH_FIELDS, m_priors, u_priors, odds_prior );
    }
    @Override
    public Iterable<LXP> getStoredRecords() {
        return filterBySex(super.getStoredRecords(), Birth.SEX, "m");
    }
}
