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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthParentsMarriage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.FelligiSunterDistances;
import uk.ac.standrews.cs.population_linkage.compositeMeasures.LXPMeasure;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.utilities.measures.Jaccard;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

public class FelligiSunterBirthParentsMarriageTest {

    Birth birth1, birth2,birth3;
    Marriage marriage1, marriage2,marriage3;

    private StringMeasure baseMeasure = new Jaccard(); // Jaccard(); // Levenshtein(); // JensenShannon();
    private List<Integer> linkage_fields = BirthParentsMarriageIdentityLinkageRecipe.LINKAGE_FIELDS;
    private List<Integer> search_fields = BirthParentsMarriageIdentityLinkageRecipe.SEARCH_FIELDS;
    private List<Double> m_priors = BirthParentsMarriageBuilder.m_priors;
    private List<Double> u_priors = BirthParentsMarriageBuilder.u_priors;
    private double odds_prior = BirthParentsMarriageBuilder.odds_prior;

    LXPMeasure measure = new FelligiSunterDistances(baseMeasure, linkage_fields, search_fields, m_priors, u_priors, odds_prior); // from BirthParentsMarriageIdentityLinkageRecipe

    @BeforeEach
    public void setup() {

        birth1 = makeBirth( "Maria","Svensdtr","Anders","Andersson","Crail","25", "12", "1879" );
        birth2 = makeBirth( "Marie","Curie","Anders","Andersson","Crail","25", "12", "1879" );
        birth3 = makeBirth( "Maria","Calvados","Anders","Anders","Anstruther","9", "5", "1900" );
        marriage1 = makeMarriage("Maria","Svensdtr","Anders","Andersson","Crail","25", "12", "1879" );
        marriage2 = makeMarriage("Mary","Svensdtr","Anders","Anderson","Creel","25", "12", "1879" );
        marriage3 = makeMarriage("Anna","Calvi","Robert","Plant","Anstruther","9", "4", "1972" );
    }

    @Test
    public void test1() {
        show( birth1,marriage1 );
        show(  birth1,marriage2 );
        show(  birth2,marriage2 );
        show(  birth1,marriage3 );
        show(  birth3,marriage1 );
    }

    private void show(Birth birth, Marriage marriage) {
        double dist = measure.distance( birth,marriage );
        System.out.println( baseMeasure.toString() );

        System.out.println( dist );
    }

    private Birth makeBirth(String mother_fn, String mother_ln, String father_fn, String father_ln, String pom, String dofm, String mofm, String yofm) {
        Birth b = new Birth();
        b.put(Birth.MOTHER_FORENAME, mother_fn);
        b.put(Birth.MOTHER_MAIDEN_SURNAME, mother_ln);
        b.put(Birth.FATHER_FORENAME, father_fn);
        b.put(Birth.FATHER_SURNAME, father_ln);
        b.put(Birth.PARENTS_PLACE_OF_MARRIAGE, pom);
        b.put(Birth.PARENTS_DAY_OF_MARRIAGE, dofm);
        b.put(Birth.PARENTS_MONTH_OF_MARRIAGE, mofm);
        b.put(Birth.PARENTS_YEAR_OF_MARRIAGE, yofm);
        return b;
    }

    private Marriage makeMarriage(String bride_fn, String bride_ln, String groom_fn, String groom_ln, String pom, String dofm, String mofm, String yofm) {

        Marriage m = new Marriage();
        m.put(Marriage.BRIDE_FORENAME, bride_fn);
        m.put(Marriage.BRIDE_SURNAME, bride_ln);
        m.put(Marriage.GROOM_FORENAME, groom_fn);
        m.put(Marriage.GROOM_SURNAME, groom_ln);
        m.put(Marriage.PLACE_OF_MARRIAGE, pom);
        m.put(Marriage.MARRIAGE_DAY, dofm);
        m.put(Marriage.MARRIAGE_MONTH, mofm);
        m.put(Marriage.MARRIAGE_YEAR, yofm);
        return m;
    }
}