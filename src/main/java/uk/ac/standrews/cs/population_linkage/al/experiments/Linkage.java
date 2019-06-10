package uk.ac.standrews.cs.population_linkage.al.experiments;

import uk.ac.standrews.cs.population_linkage.groundTruth.LinkStatus;
import uk.ac.standrews.cs.population_linkage.model.Link;
import uk.ac.standrews.cs.population_linkage.model.Role;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;

import java.util.List;
import java.util.Set;

public abstract class Linkage {

    public abstract Iterable<LXP> getSourceRecords1();

    public abstract Iterable<LXP> getSourceRecords2();

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract String getDatasetName();

    public abstract String getLinkageType();

    public abstract String getSourceType1();

    public abstract String getSourceType2();

    public abstract List<Integer> getLinkageFields1();

    public abstract List<Integer> getLinkageFields2();

    public abstract Role makeRole1( LXP lxp ) throws PersistentObjectException;

    public abstract Role makeRole2(LXP lxp ) throws PersistentObjectException;

    public abstract Set<Link> getGroundTruthLinks();

    public abstract void makeLinksPersistent(Iterable<Link> links);

    public abstract void makeGroundTruthPersistent(Iterable<Link> links);
}
