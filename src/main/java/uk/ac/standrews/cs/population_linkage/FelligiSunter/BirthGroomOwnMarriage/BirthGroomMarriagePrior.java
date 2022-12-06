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
package uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthGroomOwnMarriage;

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodes;

public class BirthGroomMarriagePrior extends ProcessNodes {
    public BirthGroomMarriagePrior() {
        super();
    }
    private static final String BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[rel:GT_ID { actors: \"Child-Groom\" } ]-(second_result:Marriage) RETURN first_result,second_result";
    private static final String COUNT_BIRTH_GROOM_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Marriage) WHERE " +
                    "r.FORENAME = s.GROOM_FORENAME AND " +
                    "r.SURNAME = s.GROOM_SURNAME AND " +
                    "r.FATHER_FORENAME = s.GROOM_FATHER_FORENAME AND " +
                    "r.FATHER_SURNAME = s.GROOM_FATHER_SURNAME AND " +
                    "r.MOTHER_FORENAME = s.GROOM_MOTHER_FORENAME AND " +
                    "r.MOTHER_MAIDEN_SURNAME = s.GROOM_MOTHER_MAIDEN_SURNAME " +
                    "RETURN count(r)";

    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!
    private static final String MARRIAGES_SAMPLE_QUERY = "MATCH (result:Marriage) WITH result WHERE rand() < 0.5 RETURN result";  // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";
    private static final String COUNT_DEATHS_QUERY = "MATCH (r:Death) return count(r)";
    private static final String COUNT_MARRIAGES_QUERY = "MATCH (r:Marriage) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthGroomMarriagePrior en = new BirthGroomMarriagePrior();

            en.provenanceComment();
            en.calculateOddsPrior(bridge,COUNT_BIRTH_GROOM_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_MARRIAGES_QUERY);
            en.calculatePriors(bridge,BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY,"FORENAME","SURNAME","GROOM_FORENAME","GROOM_SURNAME","baby_groom_first","baby_groom_second");
            en.calculatePriors(bridge,BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY,"FATHER_FORENAME","FATHER_SURNAME","GROOM_FATHER_FORENAME","GROOM_FATHER_SURNAME","baby_groom_father_first","baby_groom_father_surname");
            en.calculatePriors(bridge,BIRTH_GROOM_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, MARRIAGES_SAMPLE_QUERY,"MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","GROOM_MOTHER_FORENAME","GROOM_MOTHER_MAIDEN_SURNAME","baby_groom_mother_first","baby_groom_mother_surname");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }

}
