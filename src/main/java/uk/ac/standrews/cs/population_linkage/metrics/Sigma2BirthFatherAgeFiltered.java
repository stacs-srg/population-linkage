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

    // A large distance, since distances are not necessarily normalised.
    private static final double LARGE_DISTANCE = 1000;

    private final static int MIN_FATHERING_AGE = 14; // father must be older than child by this amount
    private final static int MAX_FATHERING_AGE = 80;

    private final int child_birth_year_field;
    private final int father_birth_year_field;

    public Sigma2BirthFatherAgeFiltered(NamedMetric<String> baseDistance, List<Integer> fields1, List<Integer> fields2, int child_birth_year_field, int father_birth_year_field) {

        super(baseDistance, fields1, fields2);
        this.child_birth_year_field = child_birth_year_field;
        this.father_birth_year_field = father_birth_year_field;
    }

    @Override
    public double distance(LXP child_birth_cert, LXP father_birth_cert) {

        try {

            final int child_year_of_birth = Integer.parseInt(child_birth_cert.getString(child_birth_year_field));
            final int father_year_of_birth = Integer.parseInt(father_birth_cert.getString(father_birth_year_field));

            if (child_year_of_birth > father_year_of_birth + MAX_FATHERING_AGE || child_year_of_birth < father_year_of_birth + MIN_FATHERING_AGE) {
                return LARGE_DISTANCE;
            }
        } catch (NumberFormatException e) {
            // eat exception and just do distance calc normally
        }
        return super.distance(father_birth_cert, child_birth_cert);
    }

    @Override
    public String getMetricName() {
        return "Sigma2BirthFatherAgeFiltered" + "-" + baseDistance.getMetricName() + "-" + hyphenConcat(fieldList1) + "--" + hyphenConcat(fieldList2);
    }
}
