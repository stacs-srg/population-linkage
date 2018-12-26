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
 * Average function for combining metrics
 * Created by al on 13/12/18
 */
public class Average extends Sigma implements NamedMetric<LXP>  {

    private int field_size;

    public Average(NamedMetric<String> baseDistance, List<Integer> fields) {
        super( baseDistance,fields );
        field_size = fields.size();
    }

    @Override
    public double distance(LXP a, LXP b) {

        return super.distance(a,b) / field_size;
    }

    @Override
    public String getMetricName() {
        return "Average"+ "Over" + baseDistance.getMetricName();
    }
}

