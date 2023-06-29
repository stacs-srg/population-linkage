package uk.ac.standrews.cs.population_linkage.resolver.msed;/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 ** Maintains a SparseProbabilityArray by keeping a map from keys to counts and probabilities.
 ** Based on Richard's code but keeps the Map with probablities to permit addition etc. for MSED
 ** N gram construction is much simpler due to using Strings rather than products of chars
 ** Keys are Strings rather than ints - probably as efficient pointer vs Int and no problems with character types.
 ** @author richard/al
 */
public class SparseProbabilityArray {

    private Map<String, ProbCount> cardMap;
    private int acc;
    private String[] events;
    private Double[] probs;

    /**
     *  Constructor: If this version is used finalise must be called when all the events have been added.
     */
    public SparseProbabilityArray() {
        cardMap = new TreeMap<String, ProbCount>();
        acc = 0; // keeps a count of the total number of events
        events = null;
        probs = null;
    }

    /**
     * Constructs a SparseProbabilityArray - prefered when Strings are known in advance
     * @param s - the Strings from which to construct the SparseProbabilityArray
     */
    public SparseProbabilityArray(String s) {
        this();
        Set<String> grams = bigrams(s);
        for( String bigram : grams ) {
            addEvent( bigram, 1);
        }
        finalise();
    }

    /**
     * @param string - s string from which to construct bigrams
     * @return - a list of bigrams from the string
     */
    private static Set<String> bigrams(String string) {
        Set<String> out = new HashSet<>();
        for (int i = 0; i + 1 < string.length(); i++) {
            out.add(string.substring(i, i + 2));
        }
        return out;
    }

    /**
     * @return the complexity of the representation
     */
    public double complexity() {
        return Math.pow(Math.E, this.getEntropy());
    }

    /**
     * @return the Shannon entropy of the representation
     */
    public double getEntropy() {
        double acc = 0;
        for (double d : probs ) {
            if (d != 0) {
                acc -= d * Math.log(d);
            }
        }
        return acc;
    }

    /**
     * Adds an event(s) (bigram(s)) to the representation
     * @param bigram - the bigram to add
     * @param count - the number of instances to add (normally/often 1)
     */
    public void addEvent(String bigram, int count) {
        ProbCount entry = cardMap.get(bigram);
        if (entry == null) {
            cardMap.put(bigram, new ProbCount(1,0.0d));
        } else {
            entry.count = entry.count + count;
        }
        acc += count;
    }

    /**
     * @return all the bigrams in the events used to construct the probability array
     */
    public String[] getEvents() {
        return events;
    }

    /**
     * @return all the probabilities of the events used to construct the probability array
     */
    public Double[] getProbs() {
        return probs;
    }

    /**
     * @param key - a key from which to find the probability
     * @return the probability associated with a key and zero if the key is not in the distribution.
     */
    public double getProb( String key ) {
        ProbCount entry = cardMap.get(key);
        if (entry == null) {
            return 0;
        } else
            return entry.prob;
    }

    /**
     * intialises the events array - a cache of the events
     */
    public void initEvents() {
        events = cardMap.keySet().toArray(String[]::new);
    }

    /**
     * intialises the probabilities array - a cache of the events
     */
    public void initProbs() {
        Set<Map.Entry<String, ProbCount>> entries = cardMap.entrySet();
        probs = entries.stream().flatMap(pair -> Stream.of(pair.getValue().prob)).toArray(Double[]::new);
    }

    /**
     * @return the map of events and ProbCounts
     */
    public Map<String, ProbCount> getMap() { return cardMap; }

    /**
     * Initialised the probabilities - makes them sum to 1.
     */
    private void balanceMap() {
        for (String event : cardMap.keySet()) {
            ProbCount entry = cardMap.get(event);
            entry.prob = (double) cardMap.get(event).count / acc;
        }
    }

    /**
     * finalisation code - must be explicity called if SparseProbabilityArray() constructor is used.
     */
    public void finalise() {
        balanceMap();
        initEvents();
        initProbs();
    }

    /**
     * @return a String rep of the class
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < events.length; i++ ) {
            sb.append( events[i] + ":" + probs[i] + ", " );
        }
        return sb.toString();
    }
}
