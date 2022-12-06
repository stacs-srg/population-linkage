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

import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.FelligiSunter.ProcessNodesOLD;

public class BirthDeathPriorOLD extends ProcessNodesOLD {
    public BirthDeathPriorOLD() {
        super();
    }
    private static final String BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY = "MATCH (first_result:Birth)-[rel:GT_ID { actors: \"Child-Deceased\" } ]-(second_result:Death) RETURN first_result,second_result";
    private static final String COUNT_BIRTH_DEATH_STRINGS_IDENTICAL_QUERY =
            "MATCH (r:Birth),(s:Death) WHERE " +
                    "r.FORENAME = s.FORENAME AND " +
                    "r.SURNAME = s.SURNAME AND " +
                    "r.FATHER_FORENAME = s.FATHER_FORENAME AND " +
                    "r.FATHER_SURNAME = s.FATHER_SURNAME AND " +
                    "r.MOTHER_FORENAME = s.MOTHER_FORENAME AND " +
                    "r.MOTHER_MAIDEN_SURNAME = s.MOTHER_MAIDEN_SURNAME " +
                    "RETURN count(r)";

    private static final String BIRTHS_SAMPLE_QUERY = "MATCH (result:Birth) WITH result WHERE rand() < 0.5 RETURN result"; // 1 in 10 chance of selection!
    private static final String DEATHS_SAMPLE_QUERY = "MATCH (result:Death) WITH result WHERE rand() < 0.5 RETURN result";  // 1 in 10 chance of selection!

    private static final String COUNT_BIRTHS_QUERY = "MATCH (r:Birth) return count(r)";
    private static final String COUNT_DEATHS_QUERY = "MATCH (r:Death) return count(r)";
    private static final String COUNT_MARRIAGES_QUERY = "MATCH (r:Marriage) return count(r)";

    public static void main(String[] args) throws BucketException {

        try ( NeoDbCypherBridge bridge = new NeoDbCypherBridge() ) {

            BirthDeathPriorOLD en = new BirthDeathPriorOLD();

            en.provenanceComment();
            en.calculateOddsPrior(bridge,COUNT_BIRTH_DEATH_STRINGS_IDENTICAL_QUERY,COUNT_BIRTHS_QUERY,COUNT_DEATHS_QUERY);
            en.calculatePriorsOLDVERSION(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"FORENAME","SURNAME","FORENAME","SURNAME","baby_deceased_first","baby_deceased_second");
            en.calculatePriorsOLDVERSION(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","MOTHER_FORENAME","MOTHER_MAIDEN_SURNAME","baby_groom_mother_first","baby_deceased_mother_surname");
            en.calculatePriorsOLDVERSION(bridge,BIRTH_DEATH_GT_IDENTITY_LINKS_QUERY, BIRTHS_SAMPLE_QUERY, DEATHS_SAMPLE_QUERY,"FATHER_FORENAME","FATHER_SURNAME","FATHER_FORENAME","FATHER_SURNAME","baby_deceased_father_first","baby_deceased_father_surname");
            en.generatePriorArrays();

        } catch (Exception e) {
            System.out.println("Runtime exception:");
            e.printStackTrace();
        } finally {
            System.out.println("Run finished");
        }
    }

}
