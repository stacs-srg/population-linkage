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
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_linkage.linkageRunners.BitBlasterLinkageRunner;
import uk.ac.standrews.cs.population_linkage.linkageRunners.MakePersistent;
import uk.ac.standrews.cs.population_linkage.linkers.Linker;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.LinkageResult;
import uk.ac.standrews.cs.population_records.record_types.Birth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity.BirthDeathIdentityLinkageRecipe.THRESHOLD;
import static uk.ac.standrews.cs.population_linkage.FelligiSunter.BirthDeathIdentity.BirthDeathIdentityLinkageRecipe.TOP_OF_BEST_F;

public class BitBlasterLinkageRunnerAnalyseLinks extends BitBlasterLinkageRunner {

    private static final String BIRTH_DEATH_IDENTITY_STD_ID = "MATCH (b:Birth),(d:Death) WHERE b.STANDARDISED_ID = $standard_id RETURN count(d)";

    public boolean doesAGTLinkExist(NeoDbCypherBridge bridge, String standard_id) {
        try( Session session = bridge.getNewSession(); Transaction tx = session.beginTransaction();) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("standard_id", standard_id);
            Result r = tx.run(BIRTH_DEATH_IDENTITY_STD_ID, parameters);
            int count = r.stream().findFirst().get().get( "count(d)" ).asInt();
            return count != 0;
        }
    }

    @Override
    public LinkageResult linkLists(Linker linker, NeoDbCypherBridge bridge, MakePersistent make_persistent, boolean evaluate_quality, long numberOfGroundTruthTrueLinks, boolean persist_links, boolean isIdentityLinkage) throws Exception {

        List<Link> linked_pairs = new ArrayList<>();

        System.out.println( "Expected threshold distance for max F2\t" + TOP_OF_BEST_F );
        System.out.println( "Threshold used in experiment\t" + THRESHOLD );
        System.out.println( "GT EXISTS\tmatches found\tfound correct\tfound distances" );
        for (List<Link> list_of_links : linker.getListsOfLinks()) {

            if( list_of_links.size() > 0 ) {                    // printout if there is a match in GT
                Link first_link = list_of_links.get(0);
                String std_id = first_link.getRecord1().getReferend().getString(Birth.STANDARDISED_ID);
                System.out.print( doesAGTLinkExist(bridge,std_id) + "\t" );
            } else {
                System.out.print("false" + "\t");
            }
            System.out.print( list_of_links.size() + "\t" );    // print number of results
            for( int i = 0; i < 5 ; i++ ) {            // print first 10 - true or not true
                if (i < list_of_links.size()) {
                    System.out.print(doesGTSayIsTrue(list_of_links.get(i)) + "\t");
                } else {
                    System.out.print("-\t");
                }
            }
            for( int i = 0; i < 5 ; i++ ) {            // print first 10 distances
                if (i < list_of_links.size()) {
                    System.out.print(list_of_links.get(i).getDistance() + "\t");
                } else {
                    System.out.print("-\t");
                }
            }
            System.out.println();
        }

        return processLinks(make_persistent, evaluate_quality, persist_links, linked_pairs);
    }
}
