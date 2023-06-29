package uk.ac.standrews.cs.population_linkage.resolver.msed;

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
