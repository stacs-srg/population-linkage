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
package uk.ac.standrews.cs.population_linkage.supportClasses;


import uk.ac.standrews.cs.neoStorr.impl.LXP;

public class RecordPair {

    public LXP stored_record;
    public LXP query_record;
    public double distance;

    public RecordPair(LXP stored_record, LXP query_record, double distance) {

        this.stored_record = stored_record;
        this.query_record = query_record;
        this.distance = distance;
    }

    public String toString() {

        return "{" + stored_record + ", " + query_record + "}";
    }
}
