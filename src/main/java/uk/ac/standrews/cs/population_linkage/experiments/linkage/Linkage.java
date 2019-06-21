package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.population_linkage.experiments.characterisation.LinkStatus;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.BucketKind;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Store;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.PersistentObjectException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.interfaces.IRepository;
import uk.ac.standrews.cs.storr.interfaces.IStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public abstract class Linkage {

    protected final String results_repository_name;
    protected final String links_persistent_name;
    protected final String ground_truth_persistent_name;
    protected final String source_repository_name;
    protected final RecordRepository record_repository;
    protected Path store_path;

    public Linkage(String results_repository_name, String links_persistent_name, String ground_truth_persistent_name, String source_repository_name, RecordRepository record_repository) {

        this.results_repository_name = results_repository_name;
        this.links_persistent_name = links_persistent_name;
        this.ground_truth_persistent_name = ground_truth_persistent_name;
        this.source_repository_name = source_repository_name;
        this.record_repository = record_repository;

        store_path = ApplicationProperties.getStorePath();
    }

    public abstract Iterable<LXP> getSourceRecords1();

    public abstract Iterable<LXP> getSourceRecords2();

    public abstract LinkStatus isTrueMatch(LXP record1, LXP record2);

    public abstract String getDatasetName();

    public abstract String getLinkageType();

    public abstract String getSourceType1();

    public abstract String getSourceType2();

    public abstract List<Integer> getLinkageFields1();

    public abstract List<Integer> getLinkageFields2();

    public abstract Role makeRole1(LXP lxp) throws PersistentObjectException;

    public abstract Role makeRole2(LXP lxp) throws PersistentObjectException;

    public abstract Set<Link> getGroundTruthLinks();

    public abstract void makeLinksPersistent(Iterable<Link> links);

    public abstract void makeGroundTruthPersistent(Iterable<Link> links);

    //////////////////////// Private ///////////////////////

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Iterable<Link> links) {

        try {
            IStore store = new Store(store_path);

            IRepository results_repository;
            try {
                results_repository = store.getRepository(results_repo_name);
            } catch (RepositoryException e) {
                results_repository = store.makeRepository(results_repo_name);
            }

            IBucket bucket;
            try {
                bucket = results_repository.getBucket(bucket_name);
            } catch (RepositoryException e) {
                bucket = results_repository.makeBucket(bucket_name, BucketKind.DIRECTORYBACKED, LXPLink.class);
            }

            for (Link link : links) {
                bucket.makePersistent(linkToLxp(link));
            }
        } catch (RepositoryException | BucketException e) {
            throw new RuntimeException(e);
        }
    }

    protected void makePersistentUsingFile(String name, Iterable<Link> links) {

        try {
            File f = new File(name);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Link l : links) {
                bw.write("Role1:\t" + l.getRole1().getRoleType() + "\tRole2:\t" + l.getRole2().getRoleType() + "\tid1:\t" + l.getRole1().getRecordId() + "\tid2:\t" + l.getRole2().getRecordId() + "\tprovenance:\t" + combineProvenance(l.getProvenance()));
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LXPLink linkToLxp(Link link) {
        return new LXPLink(link);
    }

    private String combineProvenance(final List<String> provenance) {

        final StringBuilder builder = new StringBuilder();

        for (String s : provenance) {
            if (builder.length() > 0) builder.append("/");
            builder.append(s);
        }

        return builder.toString();
    }
}
