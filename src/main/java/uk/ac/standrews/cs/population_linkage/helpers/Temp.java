/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.util.ArrayList;
import java.util.Iterator;

public class Temp {

    public static void main(String[] args) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("A");
        arrayList.add("B");
        arrayList.add("C");

        Iterable<String> it = getBirthRecords(arrayList);
        it.forEach(System.out::println);

        System.out.println(it.iterator().next());

        it.forEach(System.out::println);
    }

    public static Iterable<String> getBirthRecords(ArrayList<String> a) {

        return () -> new Iterator<String>() {

            Iterator<String> birth_records = a.iterator();

            @Override
            public boolean hasNext() {
                return birth_records.hasNext();
            }

            @Override
            public String next() {
                return birth_records.next();
            }
        };
    }
}
