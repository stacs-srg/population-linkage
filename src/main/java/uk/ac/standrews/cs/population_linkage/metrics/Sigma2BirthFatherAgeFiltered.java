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
 */
public class Sigma2BirthFatherAgeFiltered extends Sigma2 {

    private int child_birth_year_field;
    private int father_birth_year_field;

    private final static int min_fathering_age = 14; // father must be older than child by this amount
    private final static int max_fathering_age = 80;

    public Sigma2BirthFatherAgeFiltered(NamedMetric<String> baseDistance, List<Integer> fields1, List<Integer> fields2, int child_birth_year_field, int father_birth_year_field) {

        super(baseDistance, fields1, fields2);
        this.child_birth_year_field = child_birth_year_field;
        this.father_birth_year_field = father_birth_year_field;
    }

    @Override
    // gives distance of 1 for records in which the dob for a is earlier by min_fathering_age than dob for b
    // and for records for which b's dob is more than 80 after a's dob
    // years are enough
    public double distance(LXP child_birth_cert, LXP father_birth_cert) {

        try {
            String child_yob_as_string = child_birth_cert.getString(child_birth_year_field);
            String father_yob_as_string = father_birth_cert.getString(father_birth_year_field);

            int child_yob = Integer.parseInt(child_yob_as_string);
            int father_yob = Integer.parseInt(father_yob_as_string);

            if (child_yob > father_yob + max_fathering_age || child_yob < father_yob + min_fathering_age) {
                // TODO don't think this is right - sigma isn't normalised, so 1 may be a very small distance.
                return 1;
            }
        } catch (NumberFormatException e) {
            // eat exception and just do distance calc normally
        }
        return super.distance(father_birth_cert, child_birth_cert);
    }

    @Override
    public String getMetricName() {
        return "Sigma2AgeFiltered" + "-" + baseDistance.getMetricName() + "-" + hyphenConcat(fieldList1) + "--" + hyphenConcat(fieldList2);
    }
}

