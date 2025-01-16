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
package uk.ac.standrews.cs.population_linkage.resolverExperiments.msed;

import java.util.List;

/**
 * Test code for MSED - doesn't use JUnit (but probably could).
 */
public class Test {

    public static void main(String[] args) {

        List<String> strings = null;
        MSED msed = null;

        strings = List.of( "Alan", "Alan" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Albatross", "Albatross" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Albatross", "Albatross", "Albatross"  );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Albatross", "Albatross", "Albatross", "Albatross", "Albatross"  );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "lA" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "moon", "moon" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "moon", "soon" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "moon", "soot");
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "moon", "lost");
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Alan" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Albatross" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "xpbatrozs" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "cheese" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "al", "alan", "an" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Alan", "Alex" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Alan", "cheese" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Albatros", "cheese" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );

        strings = List.of( "Al", "Albatros", "cheese", "water buffalo" );
        msed = new MSED( strings );
        System.out.println( show(strings) + msed.distance() );
    }

    private static String show(List<String> strings) {
        StringBuffer sb = new StringBuffer();
        for( String s : strings ) {
            sb.append( s + " " );
        }
        return sb.toString();
    }
}
