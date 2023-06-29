package uk.ac.standrews.cs.population_linkage.resolver.msed;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Binomials {

    /**
     * Get all the n choose k perms as lists
     * Method suggested by Knuth
     * Keep picking randoms until you ghev them all! */
    public static List<List<Integer>> pickAll(int n, int k) {
        List<List<Integer>> results = new ArrayList<>();
        long count = binom(n,k);
        while( results.size() < count ) {
            List<Integer> next = null;
            do {
                next = pickOne(n, k);
                next.sort(null);
            } while( seenBefore(next,results) ); // keep generating whilst we have seen the list before
            results.add(next);
        }
        return results;
    }

    /*
     * input: two positive integers 𝑛 and 𝑘 with 𝑘≤𝑛
     * Output: a random permutation of 𝑘 integers from 1,2,⋯,𝑛
     */
    public static List<Integer> pickOne(int n, int k) {
        Random rand = new Random();
        boolean[] arr = new boolean[n];
        for( int i = 0; i < arr.length; i++ ) { arr[i] = false; }
        ArrayList<Integer> out = new ArrayList<>();
        while( out.size() < k ) {
            int i = rand.nextInt(n);
            if (!arr[i]) {
                out.add(i);
                arr[i] = true;
            }
        }
        return out;
    }


    private static long binom(int n, int k) {
        BigInteger numerator = new BigInteger("1");
        int limit = (int)(Math.pow(10,9) + 7);
        BigInteger modulus = new BigInteger(String.valueOf(limit));

        for (int i = n; i > n - k ; i--) {
            BigInteger ind = new BigInteger(String.valueOf(i));
            numerator = numerator.multiply(ind);
        }

        BigInteger fact = new BigInteger("1");

        for (int i = 2; i <= k; i++) {
            BigInteger elem = new BigInteger(String.valueOf(i));
            fact = fact.multiply(elem);
        }

        BigInteger ans = (numerator.divide(fact)).mod(modulus);

        return ans.intValue();
    }

    /**
     * @param newlist
     * @param results_so_far
     * @return true if the (sorted) list newlist contained in results_so_far
     */
    private static boolean seenBefore(List<Integer> newlist, List<List<Integer>> results_so_far) {
        for( List<Integer> already_seen : results_so_far ) {
            if( areTheSame(already_seen,newlist) ) {
                return true;  // we get a match we have seen it before so return true
            }
        }
        return false; // at the end of the list - not seen it - return false;
    }

    /**
     * @return true if the two (sorted) lists are the same
     */
    private static boolean areTheSame(List<Integer> l1, List<Integer> l2) {
        for( int i = 0; i < l1.size(); i++ ) {
            if( l1.get(i) != l2.get(i) ) {
                return false; // non match - return false
            }
        }
        return true; // get to the end - must be the same.
    }

    /*
     * debug
     */
    private static void showIntList(List<Integer> list) {
        for( int i : list ) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
