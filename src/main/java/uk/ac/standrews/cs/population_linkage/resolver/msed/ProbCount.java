package uk.ac.standrews.cs.population_linkage.resolver.msed;

/**
 * A helper class to count probabilites and counts
 */
public class ProbCount {
    public int count;
    public double prob;

    public ProbCount(int count, double prob) {
        this.count = count;
        this.prob = prob;
    }
}