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

import java.util.*;

/**
 * @author Al Dearle/Richard Connor/David Morrison - various contributions!
 * Implements Multiple Structural Entropic Distance over strings as described in:
 * A Multi-way Divergence Metric for Vector Spaces, Robert Moss and Richard Connor

 */
public class MSED {
    
    private final int num_of_vals;                          // number of data points in the base.
    private final double[] av_data;                         // average of the probability distributions
    private final List<SparseProbabilityArray> sparse_reps; // These are the reps corresponding to the input Strings
    private final double[] comps;                           // The complexities of each of the probability distributions
    private final double product;                           // the product of the complexities.
    private final double comp_av;                           // the average cpomplexity.

    /**
     * the power to which the "raw" sed calculation is raised to give the
     * triangle inequality property
     */
    public static final double FINAL_POWER = 0.486; // 6 or 3??

    /**
     * @param strings - the list of strings over which MSED is calculated
     */
    public MSED(List<String> strings) {
        //Debug.showStrings("Strings:", strings);
        num_of_vals = strings.size();

        this.sparse_reps = stringsToProbs(strings);
        //Debug.showProbabilites( "sparse_reps:", sparse_reps);

        this.av_data = normalisedColumnSums(sparse_reps);
        //Debug.showDoubles( "av_data:", av_data);
        comp_av = complexity(matrixIt(av_data))[0];
        //Debug.showDouble( "comp_av: ", comp_av );
        double[][] consistent_probs = makeConsistent(sparse_reps);
        comps = complexity(consistent_probs);
        //Debug.showDoubles( "comps:", comps );
        product = product(comps);
        //Debug.showDouble( "product: ",product );
    }

    /**
     * @return the distance between the strings passed in
     * This was created as a class rather than a static method since there was a consideration about being able to add
     * in more strings - not sure if possible due to extensive probability manipulations that would be needed.
     */
    public double distance() {
        final double num_features = (double) num_of_vals; // av_data.getNumFeatures();
        double bottom_line = Math.pow(product, 1 / num_features);               // average complexity.
        //Debug.showDouble( "num_features: ",num_features );
        //Debug.showDouble( "bottom_line: ",bottom_line );
        double result = (1 / (num_features - 1 )) * (comp_av / bottom_line - 1);
        if(  Double.valueOf(result).isNaN() ) {
            return (comp_av / bottom_line - 1);
        }
        return result;
    }

    /**
     * A convenience method to be able to perform MSED in a single call
     * @param strings - the strings over MSED to be calculated
     * @return the divergence/distance between the strings - perfect match returns 0, perfect non-match 1.
     */
    public static double distance(List<String> strings) {
        MSED msed = new MSED(strings);
        return msed.distance();
    }

    /**
     * Turns a one dimensional vector into a 2D matrix
     * @param vector - the vector to be converted
     * @return a matrix with a single row - the data passed in
     */
    private double[][] matrixIt(double[] vector) {
        double[][] matrix = new double[1][vector.length];
        matrix[0] = vector;
        return matrix;
    }

    /**
     * @param vector - the data over which to caculate a product
     * @return the product of the vector elements
     */
    private double product(double[] vector) {
        double result = 1.0;
        for (double comp : vector ) {
            result *= comp;
        }
        return result;
    }

    /**
     * Creates a list of SparseProbabilityArrays from a list of Strings
     * @param strings - the strings to be converted
     * @return a corresponding list of SparseProbabilityArrays
     */
    private List<SparseProbabilityArray> stringsToProbs(List<String> strings) {
        List<SparseProbabilityArray> probabilities = new ArrayList<>();
        for( String s : strings) {
            probabilities.add( new SparseProbabilityArray(s) );
        }
        return probabilities;
    }

    /**
     * Get the (row-wise) complexities of a matrix
     * @param matrix - the input data
     * @return the complexities of the rows of the matrix
     */
    public double[] complexity(double[][] matrix) {
        double[][] hs = calcHs(matrix);
        //Debug.showMatrix("hs", hs);
        double[] cs = nanSum(hs);
        //Debug.showDoubles("cs: ",cs);
        double[] C = exp(cs);
        return C;
    }

    /**
     * Calculate the H values for the matrix
     * @param matrix - the input data
     * @return a matrix of H values
     */
    private double[][] calcHs(double[][] matrix) {
        double[][] hs = new double[matrix.length][matrix[0].length];
        int count = 0;
        for( double[] row : matrix) {
            int row_len = row.length;
            double[] new_hs_row = new double[row_len];
            for( int i = 0; i < row_len; i++ ) {
                if( row[i] == 0 ) {  // can be Nan
                    new_hs_row[i] = Double.NaN;
                } else {
                    new_hs_row[i] = row[i] * Math.log(row[i]);
                }
            }
            hs[count++] = new_hs_row;
        }
        return hs;
    }

    /**
     * This method takes a list of sparse reps and returns a list of corresponding probability vectors
     * each of which is the same length as the total number of features in the combined dataset.
     * Furthermore the features in the row are in a consistent order.
     * For example if we have the Strings "moon" and "soon",
     * the bigrams are "mo", "oo" and "on" and "so", "oo" and "on"
     * In both the probabilities are 0.333, 0.333, 0.333.
     * When combined there are 4 bigrams: "mo", "so", "oo" and "on" 2 appear once 2 appear twice.
     * @param reps - the reps to be made consistent
     * @return - the probabilites as a matrix.
     */
    private double[][] makeConsistent(List<SparseProbabilityArray> reps) {
        List<String>  sorted_features = collectFeatures(reps);
        double[][] results = new double[reps.size()][sorted_features.size()];
        int count = 0;
        for( SparseProbabilityArray spa : reps ) {
            results[count++] = padFeatures( spa,sorted_features );
        }
        return results;
    }

    /**
     * Pads a string, so for example, moon with 3 bigrams in a moon/soon context becomes something like [0.333,0.3333,0.3333,0]
     * @param spa - a SparseProbabilityArray to be padded
     * @param sorted_features - the features in the String - note these are coded bigrams.
     * @return a padded probability array of the same size as sorted_features containing the features in spa plus zeros as appropriate.
     */
    private double[] padFeatures(SparseProbabilityArray spa, List<String> sorted_features) {
        double[] result = new double[sorted_features.size()];
        for( int i = 0; i < sorted_features.size(); i++ ) {
            result[i] = spa.getProb(sorted_features.get(i));
        }
        return result;
    }

    /**
     * @param sparse_reps - the reps from which to gather the lexicon.
     * @return all the features in a sorted order
     */
    private List<String> collectFeatures(List<SparseProbabilityArray> sparse_reps) {
        Set<String> all_features = new TreeSet<>();
        for( SparseProbabilityArray spa : sparse_reps ) {
            all_features.addAll( Arrays.asList( spa.getEvents() ) );
        }
        List<String> sorted_fetures = new ArrayList<String>(all_features);
        Collections.sort(sorted_fetures);
        return sorted_fetures;
    }

    /**
     * @param vector - a vector of data
     * @return elements such that thet are all e to the power of the input vector
     */
    private double[] exp(double[] vector) {
        double[] exps = new double[vector.length];
        for( int i = 0; i < vector.length; i++ ) {
            exps[i] = Math.pow( Math.E,vector[i] );
        }
        return exps;
    }

    /**
     * @param matrix - the input matrix
     * @return the nan sum of the columns (nans are ignored in the sum)
     */
    private double[] nanSum(double[][] matrix) {
        int num_rows = matrix.length;
        int num_cols = matrix[0].length;
        double[] sums = new double[num_rows];
        for( int row = 0; row < num_rows; row++ ) {
            double rowsum = 0;
            for( int col = 0; col < num_cols; col++ ) { // sum the rows
                if( ! Double.valueOf(matrix[row][col]).isNaN() ) {
                    rowsum = rowsum + matrix[row][col];
                }
            }
            sums[row] = -rowsum;
        }
        return sums;
    }

    /**
     * @param probabilities - a set of probabilities
     * @return the average column sums
     */
    private double[] normalisedColumnSums(List<SparseProbabilityArray> probabilities) {
        double[][] data = makeConsistent(probabilities);
        //Debug.showMatrix("consistent",data);
        double num_rows = (double) data.length;
        double[] sums = columnSums(data);
        for( int i = 0; i < sums.length; i++ ) {
            sums[i] = sums[i] / num_rows;
        }
        return sums;
    }

    /**
     * @param matrix
     * @return the column sums of the matrix
     */
    private double[] columnSums(double[][] matrix) {

        int num_rows = matrix.length;
        int num_cols = matrix[0].length;

        double[] sums = new double[num_cols];
        for( int col = 0; col < num_cols; col++ ) {
            double colsum = 0;
            for( int row = 0; row < num_rows; row++ ) {
                colsum = colsum + matrix[row][col];
            }
            sums[col] = colsum;
        }
        return sums;
    }
}