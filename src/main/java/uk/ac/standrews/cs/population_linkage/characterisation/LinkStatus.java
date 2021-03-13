/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.characterisation;

public enum LinkStatus {

    TRUE_MATCH,
    NOT_TRUE_MATCH,
    UNKNOWN,
    EXCLUDED // for example in symmetric sibling linkage (i.e. birth-birth-sibling)
                // we should be neither rewarded or penalising for identifying a record/person as being in their own
                // sibling bundle - hence we return EXCLUDED and the link is neither counted an a TP or a FP
}
