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
package uk.ac.standrews.cs.population_linkage.compositeMeasures;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.List;

import static uk.ac.standrews.cs.population_linkage.profiling.umea.UmeaDeathSiblingProfiling.base_measure;

public class FelligiSunterDistance extends LXPMeasure {

    private final List<Double> m_priors;
    private final List<Double> u_priors;
    private final double odds_prior;

    private static final double LN_2 = Math.log(2.0D);

    public FelligiSunterDistance(final List<Integer> field_indices1, final List<Integer> field_indices2, final StringMeasure base_measure, List<Double> m_priors, List<Double> u_priors, double odds_prior) {

        super(field_indices1, field_indices2, base_measure);

        if (m_priors.size() != u_priors.size() && m_priors.size() != field_indices1.size()) {
            throw new RuntimeException("Field lists and prior lists must be the same length");
        }

        this.m_priors = m_priors;
        this.u_priors = u_priors;
        this.odds_prior = odds_prior;
    }

    @Override
    public String toString() {
        return "FS field distances using: " + base_measure;
    }

    @Override
    public double getMaxDistance() {
        return 1;
    }

    @Override
    public double calculateDistance(final LXP x, final LXP y) {

        double sigma = 0.0d;

        for (int i = 0; i < field_indices1.size(); i++) {

            try {
                final int field_index1 = field_indices1.get(i);
                final int field_index2 = field_indices2.get(i);

                final String field_value1 = x.getString(field_index1);
                final String field_value2 = y.getString(field_index2);

                // final double prior_ratio = prior_ratios.get(i);

                double m_prior = m_priors.get(i);
                double u_prior = u_priors.get(i);

                double base_distance = base_measure.distance(field_value1, field_value2);
                if( base_distance > 1 || base_distance < 0 ) {
                    throw new RuntimeException("Base distance must be between zero and one - distance was " + base_distance + " for metric " + base_measure);
                }
                double r_g_prime = ( m_prior / u_prior ) - ( ( m_prior / u_prior ) - ( ( 1 - m_prior ) / ( 1- u_prior ) ) ) * base_distance;
                double log_2_R_g_prime = log2(r_g_prime);
                sigma = sigma + log_2_R_g_prime;

            } catch (Exception e) {
                throwExceptionWithDebug(x, y, i, e);
            }
        }
        double odds_posterior =  Math.exp( sigma * LN_2 ) * odds_prior;
        return 1 - odds_posterior / ( 1 + odds_posterior );
    }

    public static double log2(double x) {
        return Math.log(x) / LN_2;
    }
}
