package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.storr.impl.LXP;

public class ExactMatchMatcher implements Matcher {

    private int[] match_fields;

    public ExactMatchMatcher(int... match_fields) {

        this.match_fields = match_fields;
    }

    @Override
    public boolean match(LXP record1, LXP record2) {

        for (int field : match_fields) {
            if (!record1.getString(field).equals(record2.getString(field))) return false;
        }

        return true;
    }
}
