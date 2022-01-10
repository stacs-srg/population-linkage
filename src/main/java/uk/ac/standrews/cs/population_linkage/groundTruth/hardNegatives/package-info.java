/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
/**
 * <p>
 * This module is to create hard negatives for training of ML package.
 * It uses the links created in the groundTruthNeolinks package.
 * It works by finding the GT links then for each of the nodes it finds non links by metric search.
 * The non links are NNs of the nodes not including the GT nodes.
 * <p>
 *
 * @since 15/9/21
 * @author al@st-andrews.ac.uk
 */
package uk.ac.standrews.cs.population_linkage.groundTruth.hardNegatives;