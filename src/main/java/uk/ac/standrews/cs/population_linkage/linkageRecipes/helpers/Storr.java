/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.supportClasses.Link;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;
import uk.ac.standrews.cs.population_records.record_types.Marriage;
import uk.ac.standrews.cs.storr.impl.BucketKind;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.storr.impl.Store;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.interfaces.IRepository;
import uk.ac.standrews.cs.storr.interfaces.IStore;

public class Storr {

    private final String resultsRepositoryName;
    private final String linksPersistentName;
    private final String sourceRepositoryName;
    private final RecordRepository recordRepository;
    private final Path storePath;

    private Map<Class<? extends LXP>, Iterable<LXP>> recordIterables = new HashMap<>();
    private Map<String, IBucket> storeRepoBucketLookUp = new HashMap<>();

    public Storr(String recordsRepositoryName, String linksRepositoryName, String resultsRepositoryName) {
        this.resultsRepositoryName = resultsRepositoryName;
        this.linksPersistentName = linksRepositoryName;
        this.sourceRepositoryName = recordsRepositoryName;
        this.storePath = ApplicationProperties.getStorePath();
        this.recordRepository = new RecordRepository(storePath, recordsRepositoryName);
    }

    public Iterable<LXP> getIterable(Class<? extends LXP> sourceType) {
        if(recordIterables.containsKey(sourceType)) {
            return recordIterables.get(sourceType);
        } else {
            Iterable<LXP> iterable = initIterable(sourceType);
            recordIterables.put(sourceType, iterable);
            return iterable;
        }
    }

    public String getSourceRepositoryName() {
        return sourceRepositoryName;
    }

    public RecordRepository getRecordRepository() {
        return recordRepository;
    }

    private Iterable<LXP> initIterable(Class<? extends LXP> sourceType) {

        if (sourceType.equals(Birth.class)) {
            return Utilities.getBirthRecords(recordRepository);
        } else if (sourceType.equals(Marriage.class)) {
            return Utilities.getMarriageRecords(recordRepository);
        } else if (sourceType.equals(Death.class)) {
            return Utilities.getDeathRecords(recordRepository);
        } else {
            throw new RuntimeException("Invalid source type");
        }
    }

    public int getSize(Class<? extends LXP> sourceType) {

        try {
            if (sourceType.equals(Birth.class)) {
                return recordRepository.getNumberOfBirths();
            }

            if (sourceType.equals(Marriage.class)) {
                return recordRepository.getNumberOfMarriages();
            }

            if (sourceType.equals(Death.class)) {
                return recordRepository.getNumberOfDeaths();
            }
        } catch (BucketException e) {
            throw new RuntimeException(e.getMessage());
        }

        throw new Error("Invalid source type");
    }

    public void makeLinksPersistent(Iterable<Link> links) {
        makePersistentUsingStorr(storePath, resultsRepositoryName, linksPersistentName, links);
    }

    public void makeLinkPersistent(Link link) {
        makePersistentUsingStorr(storePath, resultsRepositoryName, linksPersistentName, link);
    }

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Link link) {

        try {
            //noinspection unchecked
            getBucket(store_path, results_repo_name, bucket_name).makePersistent(link);

        } catch (BucketException e) {
            throw new RuntimeException(e);
        }
    }

    protected void makePersistentUsingStorr(Path store_path, String results_repo_name, String bucket_name, Iterable<Link> links) {

        for (Link link : links)
            makePersistentUsingStorr(store_path, results_repo_name, bucket_name, link);
    }

    protected void makePersistentUsingFile(String name, Iterable<Link> links) {

        try {
            File f = new File(name);
            if (!f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Link l : links) {
                bw.write("Role1:\t" + l.getRole1() + "\tRole2:\t" + l.getRole2() + "\tid1:\t" + l.getRecord1().getReferend().getId() + "\tid2:\t" + l.getRecord2().getReferend().getId() + "\tprovenance:\t" + combineProvenance(l.getProvenance()));
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException | BucketException e) {
            throw new RuntimeException(e);
        }
    }

    private String combineProvenance(final List<String> provenance) {

        final StringBuilder builder = new StringBuilder();

        for (String s : provenance) {
            if (builder.length() > 0) builder.append("/");
            builder.append(s);
        }

        return builder.toString();
    }

    private IBucket getBucket(Path store_path, String results_repo_name, String bucket_name) {

        IBucket bucket = storeRepoBucketLookUp.get(getSRBString(store_path, results_repo_name, bucket_name));
        if (bucket == null) {

            try {
                IStore store = new Store(store_path);

                IRepository results_repository;
                try {
                    results_repository = store.getRepository(results_repo_name);
                } catch (RepositoryException e) {
                    results_repository = store.makeRepository(results_repo_name);
                }

                try {
                    bucket = results_repository.getBucket(bucket_name);
                } catch (RepositoryException e) {
                    bucket = results_repository.makeBucket(bucket_name, BucketKind.DIRECTORYBACKED, Link.class);
                }

                storeRepoBucketLookUp.put(getSRBString(store_path, results_repo_name, bucket_name), bucket);

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return bucket;
    }

    private String getSRBString(Path store_path, String results_repo_name, String bucket_name) {
        return store_path.toString() + "|" + results_repo_name + "|" + bucket_name;
    }

    public void stopStoreWatcher() {
        recordRepository.stopStoreWatcher();
    }
}
