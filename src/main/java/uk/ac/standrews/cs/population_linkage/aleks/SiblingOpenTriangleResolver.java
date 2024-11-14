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
package uk.ac.standrews.cs.population_linkage.aleks;

import uk.ac.standrews.cs.neoStorr.impl.Store;
import uk.ac.standrews.cs.neoStorr.util.NeoDbCypherBridge;
import uk.ac.standrews.cs.population_records.RecordRepository;

public abstract class SiblingOpenTriangleResolver {
    protected static NeoDbCypherBridge bridge;
    protected static RecordRepository record_repository;

    //Various constants for predicates
    protected static final int MAX_AGE_DIFFERENCE  = 24;
    protected static final double DATE_THRESHOLD = 0.5;
    protected static final double NAME_THRESHOLD = 0.5;
    protected static final int BIRTH_INTERVAL = 270;

    protected static String CREATE_SIBLING_QUERY;
    protected static String DELETE_SIBLING_QUERY;



    public SiblingOpenTriangleResolver(String sourceRepo) {
        bridge = Store.getInstance().getBridge();
        record_repository= new RecordRepository(sourceRepo);
    }
}
