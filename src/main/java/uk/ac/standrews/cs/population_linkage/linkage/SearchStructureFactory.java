package uk.ac.standrews.cs.population_linkage.linkage;

import uk.ac.standrews.cs.population_linkage.model.SearchStructure;
import uk.ac.standrews.cs.storr.impl.LXP;

public interface SearchStructureFactory {

     SearchStructure<LXP> newSearchStructure();
}
