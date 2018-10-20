package uk.ac.standrews.cs.population_linkage.model;

import uk.ac.standrews.cs.population_linkage.linkage.BirthLinkageSubRecord;

import java.util.List;

public interface Linker {

    Links link(List<BirthLinkageSubRecord> birth_records);
}
