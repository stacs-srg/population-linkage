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
package uk.ac.standrews.cs.population_linkage.groundTruth.groundTruthNeoLinks;

import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;

import java.util.Iterator;
import java.util.TreeSet;

public class CheckGTCalculations {

    // Death Groom ID

    String  DEATH_GROOM_IDENTITY = "MATCH (d:Death),(m:Marriage) WHERE " +
            "d.DECEASED_IDENTITY <> \"\" AND " +
            "m.GROOM_IDENTITY <> \"\" AND " +
            "d.DECEASED_IDENTITY = m.GROOM_IDENTITY " +
            "MERGE (d)-[:GT_ID { actors: \"Deceased-Groom\" } ]-(m)";

    Iterator<Death> deaths;
    Iterator<Marriage> marriages;

    @SuppressWarnings("unchecked")
    public CheckGTCalculations() throws Exception {

        RecordRepository record_repository = new RecordRepository("umea");
        deaths = (Iterator<Death>) record_repository.getBucket("death_records").getInputStream().iterator();
        marriages = (Iterator<Marriage>) record_repository.getBucket("marriage_records").getInputStream().iterator();

        TreeSet<Integer> d_ids = new TreeSet<>();

        while( deaths.hasNext() ) {
            Death d = deaths.next();
            String id = d.getString(Death.DECEASED_IDENTITY);
            if( ! id.equals("")) {
                d_ids.add(Integer.parseInt(id));
            }
        }

        int matches = 0;
        int count = 0;

        while( marriages.hasNext() ) {
            Marriage m = marriages.next();
            System.out.println( count++ );

            String mid = m.getString(Marriage.GROOM_IDENTITY);
            if( ! mid.equals("") ) {
                Integer mid_i = Integer.parseInt(mid);
                if( d_ids.contains(mid_i)) {
                    matches = matches + 1;
                }
            }
        }
        record_repository.close();
        System.out.println("Death-groom identity\t" + matches);

    }

    public static void main(String[] args) throws Exception {
        CheckGTCalculations c = new CheckGTCalculations();
    }


}