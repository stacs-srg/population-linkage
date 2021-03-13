/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.Pair;
import uk.ac.standrews.cs.storr.impl.LXP;

public class GroundTruthLinkCounter {

    public static long countGroundTruthLinksStandard(List<List<Pair>> pairs,
            Iterable<LXP> sourceRecords1, Iterable<LXP> sourceRecords2, boolean isSymmetric, boolean isSibling) {

        Map<List<Pair>, Map<String, Collection<LXP>>> record1Maps = new HashMap<>();

        for(List<Pair> pairList : pairs) {
            Map<String, Collection<LXP>> pairMap = new HashMap<>();

            sourceRecords1.forEach(record1 -> {
                try {
                    String value = getPairListFirstRepresentation(pairList, record1);
                    pairMap.putIfAbsent(value, new ArrayList<>());
                    pairMap.get(value).add(record1);
                } catch (EmptyFieldException ignore) {}
            });
            record1Maps.put(pairList, pairMap);
        }

        long c = 0;

        for (LXP record2 : sourceRecords2) {
            Set<LXP> links = new HashSet<>();
            for(List<Pair> pairList : record1Maps.keySet()) {
                Map<String, Collection<LXP>> linksMap = record1Maps.get(pairList);
                try {
                    Collection<LXP> matchedLinks = linksMap.get(getPairListSecondRepresentation(pairList, record2));
                    if (matchedLinks != null) {
                        links.addAll(matchedLinks);
                    }
                } catch (EmptyFieldException ignore) {}
            }
            c += toGTCount(links.size(), isSymmetric, isSibling);
        }

        return c;
    }

    private static long toGTCount(long count, boolean isSymmetric, boolean isSibling) {
        if(isSibling && isSymmetric) {
            return (long) (count * (count - 1) / 2.0); // need to account for self links and links in both directions
        }

        return count; // default standard identify linkage (no need to account for self links or counting the a link in both directions)
    }

    private static String getPairListFirstRepresentation(List<Pair> pairs, LXP record) throws EmptyFieldException {
        StringBuilder sb = new StringBuilder();
        for(Pair pair : pairs) {
            sb.append(getStringButThrowIfEmpty(record, pair.first)).append("|");
        }
        return sb.toString();
    }

    private static String getPairListSecondRepresentation(List<Pair> pairs, LXP record) throws EmptyFieldException {
        StringBuilder sb = new StringBuilder();
        for(Pair pair : pairs) {
            sb.append(getStringButThrowIfEmpty(record, pair.second)).append("|");
        }
        return sb.toString();
    }

    private static String getStringButThrowIfEmpty(LXP record, int field) throws EmptyFieldException {
        String value = record.getString(field);
        if(value.equals("")) {
            throw new EmptyFieldException();
        }
        return value;
    }
}
