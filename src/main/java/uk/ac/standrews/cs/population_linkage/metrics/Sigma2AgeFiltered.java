/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module linkage-java.
 *
 * linkage-java is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * linkage-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with linkage-java. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.metrics;


import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.List;

/**
 * Sigma function for combining metrics  - compares different field lists over data
 * Created by al on 13/12/18
 */
public class Sigma2AgeFiltered extends Sigma2 implements NamedMetric<LXP>  {

    protected Integer birthyear_field1;
    protected Integer birthyear_field2;

    final static int min_fathering_age = 14; // father must be older than child by this amount
    final static int max_fathering_age = 80;

    public Sigma2AgeFiltered(NamedMetric<String> baseDistance, List<Integer> fields1, List<Integer> fields2, Integer birthyear_field1, Integer birthyear_field2   ) {

        super( baseDistance,fields1,fields2);
        this.birthyear_field1 = birthyear_field1;
        this.birthyear_field2 = birthyear_field2;
    }


    @Override
    // gives distance of 1 for records in which the dob for a is earlier by min_fathering_age than dob for b
    // and for records for which b's dob is more than 80 after a's dob
    // years are enough
    public double distance(LXP father_birth_cert, LXP child_birth_cert) {

        try {
            String father_yob_as_string = father_birth_cert.getString(birthyear_field1);
            String child_yob_as_string = child_birth_cert.getString(birthyear_field2);

            int father_yob = Integer.parseInt(father_yob_as_string);
            int child_yob = Integer.parseInt(child_yob_as_string);

            if ( child_yob > father_yob + max_fathering_age || child_yob < father_yob + min_fathering_age) {
                return 1;
            }
        } catch( NumberFormatException e ) {
            // eat exception and just do distance calc normally
        }
        return super.distance(father_birth_cert,child_birth_cert);
    }

    @Override
    public String getMetricName() {
        return "Sigma2AgeFiltered" + "-" + baseDistance.getMetricName() + "-" + hyphenConcat( fieldList1 ) + "--" + hyphenConcat( fieldList2 );
    }

    private static String hyphenConcat(List<Integer> fieldList) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < fieldList.size() - 1; i++ ) {
            sb.append( Integer.toString( fieldList.get(i) ) );
            sb.append( "-" );
        }
        sb.append( Integer.toString( fieldList.get( fieldList.size() - 1 ) ) );
        return sb.toString();
    }

}

