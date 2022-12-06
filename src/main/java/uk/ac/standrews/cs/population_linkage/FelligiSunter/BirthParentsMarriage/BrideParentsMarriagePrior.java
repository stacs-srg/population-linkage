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

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;

/**
 * Copied from ExploreNames.ProcessBrideMother
 */

public class BrideParentsMarriagePrior extends ProcessNodes {

    public BrideParentsMarriagePrior() {
        super();
    }

    private static final String BRIDE_MOTHER_GT_IDENTITY_QUERY = "MATCH (first_result:Birth)-[rel:GT_ID { actors: \"Mother-Bride\" } ]-(second_result:Marriage) RETURN first_result,second_result";
    private static final String COUNT_BRIDE_MOTHER_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Marriage) WHERE " +
            "r.MOTHER_FORENAME = s.BRIDE_FORENAME AND " +
            "r.MOTHER_MAIDEN_SURNAME = s.BRIDE_SURNAME AND " +
            "r.PARENTS_PLACE_OF_MARRIAGE = s.PLACE_OF_MARRIAGE AND " +
            "r.PARENTS_DAY_OF_MARRIAGE = s.MARRIAGE_DAY AND " +
            "r.PARENTS_YEAR_OF_MARRIAGE = s.MARRIAGE_YEAR " +
            "RETURN count(r)";
    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!
    private static final String MARRIAGES_SAMPLE_QUERY = "MATCH (result:Marriage) WITH result WHERE rand() < 0.5 RETURN result";  // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";
    private static final String COUNT_DEATHS_QUERY = "MATCH (r:Death) return count(r)";
    private static final String COUNT_MARRIAGES_QUERY = "MATCH (r:Marriage) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BrideParentsMarriagePrior en = new BrideParentsMarriagePrior();

            en.provenanceComment();
            en.calculateOddsPrior(bridge, COUNT_BRIDE_MOTHER_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_MARRIAGES_QUERY);
            // Order these are called affects the array declaration so must be in the right order!
            en.calculatePriors(bridge, BRIDE_MOTHER_GT_IDENTITY_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY,"MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","BRIDE_FORENAME","BRIDE_SURNAME","bride_mother_first","bride_mother_second");
            en.calculatePriors(bridge, BRIDE_MOTHER_GT_IDENTITY_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY, "FATHER_FORENAME", "FATHER_SURNAME", "GROOM_FORENAME", "GROOM_SURNAME", "father_first", "father_surname" );
            en.calculatePriors(bridge, BRIDE_MOTHER_GT_IDENTITY_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY, "PARENTS_PLACE_OF_MARRIAGE","PARENTS_DAY_OF_MARRIAGE","PLACE_OF_MARRIAGE","MARRIAGE_DAY","pom","dom");
            en.calculatePriors(bridge, BRIDE_MOTHER_GT_IDENTITY_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY,"PARENTS_MONTH_OF_MARRIAGE","PARENTS_YEAR_OF_MARRIAGE","MARRIAGE_MONTH","MARRIAGE_YEAR","mom","yom");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }
}
