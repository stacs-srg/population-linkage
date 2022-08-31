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
package uk.ac.standrews.cs.population_linkage.linkageAccuracy;

import org.neo4j.driver.Result;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.standrews.cs.utilities.ClassificationMetrics.*;
import static uk.ac.standrews.cs.utilities.ClassificationMetrics.specificity;

public abstract class AbstractAccuracy {

    protected final NeoDbCypherBridge bridge;

    protected static final String ALL_BIRTHS = "MATCH (r:Birth) return count(r)"; // bad name r but saves writing another method
    protected static final String ALL_DEATHS = "MATCH (r:Death) return count(r)"; // bad name r but saves writing another method
    protected static final String ALL_MARRIAGES = "MATCH (r:Marriage) return count(r)"; // bad name r but saves writing another method

    public AbstractAccuracy(NeoDbCypherBridge bridge) {
        this.bridge = bridge;
    }

    protected NeoDbCypherBridge getBridge() {
        return bridge;
    }

    protected void report(long fpc,long tpc,long fnc,long all_pair_count) {

        System.out.println( this.getClass().getSimpleName() );

        long tnc = all_pair_count - fpc - tpc - fnc;

        double prec = precision(tpc, fpc);
        double rec = recall(tpc, fnc);
        double f = F1(tpc, fpc, fnc);
        double spec = specificity( tnc, fpc );

        NumberFormat format = new DecimalFormat("0.00");

        System.out.println( "False positive count\t" + fpc );
        System.out.println("True positive count\t" + tpc  );
        System.out.println("False negative count\t" + fnc  );
        System.out.println("True negative count\t" + tnc  );
        System.out.println();
        System.out.println("Precision\t" + format.format( prec ) );
        System.out.println("Recall\t" + format.format( rec ) );
        System.out.println("F1\t" + format.format( f ) );
        System.out.println("Specificity\t" + format.format( spec ) );
        System.out.println();
        System.out.println("----");
    }

    protected long doQuery(String query_string) {
        Map<String, Object> parameters = new HashMap<>();
        Result result = bridge.getNewSession().run(query_string, parameters);
        long value = result.list(r -> r.get("count(r)").asInt()).get(0);
        return value;
    }

    protected long nChoose2(long num) {
        return ( num * (num-1) ) /2;
    }

}
