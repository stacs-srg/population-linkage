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

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;

public class BirthFatherPrior extends ProcessNodes {
    public BirthFatherPrior() {
        super();
    }
    private static final String BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[r:GT_ID { actors: \"Child-Father\" } ]-(second_result:Birth) RETURN first_result,second_result";

    private static final String COUNT_BIRTH_FATHER_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Birth) WHERE " +
                    "r.FORENAME = s.FATHER_FORENAME AND " +
                    "r.SURNAME = s.FATHER_SURNAME " +
                    "RETURN count(r)";

    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthFatherPrior en = new BirthFatherPrior();

            en.provenanceComment();
            en.calculateOddsPrior(bridge,COUNT_BIRTH_FATHER_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_BIRTHS_QUERY);
            en.calculatePriors(bridge,BIRTH_FATHER_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, BIRTHS_SAMPLE_QUERY,"FORENAME","SURNAME","FATHER_FORENAME","FATHER_SURNAME","baby_father_first","baby_father_second");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }

}
