/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import com.google.common.collect.Sets;
import uk.ac.standrews.cs.data.synthetic.SyntheticBirthsDataSet;
import uk.ac.standrews.cs.data.synthetic.SyntheticDeathsDataSet;
import uk.ac.standrews.cs.data.synthetic.SyntheticMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.data.synthetic.ImportSyntheticScotlandRecordsToStore;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ValidatePopulationInStorr {

    private String sourceRepoName;
    private String populationName;
    private String populationSize;
    private String populationNumber;
    private boolean corrupted;
    private String corruptionNumber;

    public ValidatePopulationInStorr(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber) {

        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corrupted = corrupted;
        this.corruptionNumber = corruptionNumber;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

    }

    public void validate(Path recordCounts) throws Exception {

        final Path store_path = ApplicationProperties.getStorePath();

        RecordRepository record_repository;

        try {
            record_repository = new RecordRepository(store_path, sourceRepoName);
        } catch (RuntimeException e) {
            System.out.println("Repository not found --- will now create: " + sourceRepoName);
            record_repository = new ImportSyntheticScotlandRecordsToStore(store_path, populationName, populationSize,
                    populationNumber, !corruptionNumber.equals("0"), corruptionNumber).run();
        }

        // this section checks to make sure the the storr repo contains the same number of records as was in the source
        // if not it deletes the offending bucket and repopulates it

        for(String bucket : RecordRepository.getBucketNames()) {

            int recordsInSource = getCountFromLog(recordCounts, bucket, populationName, populationSize, populationNumber, corruptionNumber);

            if(recordsInSource == -1) { // count not in log file - thus calc it
                System.out.println("No count in file - will count from source for: " + sourceRepoName + ":" + bucket);
                recordsInSource = getCountFromRawFile(bucket);
                System.out.println("Count of: " + recordsInSource + " will be added to log for: " + sourceRepoName + ":" + bucket);
                writeCountToLog(recordsInSource, recordCounts, bucket, populationName, populationSize, populationNumber, corruptionNumber);
            }

            IBucket recordBucket = record_repository.getBucket(bucket);
            int recordsInBucket = recordBucket.size();

            if(recordsInBucket != recordsInSource) { // there's data missing in the storr
                System.out.println("Records missing from storr for: " + sourceRepoName + ":" + bucket);
                record_repository.deleteBucket(bucket);
                System.out.println("Bucket Deleted: " + sourceRepoName + ":" + bucket);
                record_repository.initialiseBuckets(sourceRepoName);
                importRecordsTo(bucket, record_repository);
                System.out.println("Bucket re-populated from source: " + sourceRepoName + ":" + bucket);
                if(record_repository.getBucket(bucket).size() != recordsInSource)
                    throw new RuntimeException("Bucket size still incorrect after re-population for: " + sourceRepoName + ":" + bucket);
            }
        }
    }

    private void writeCountToLog(int count, Path recordCounts, String bucket, String populationName, String populationSize, String populationNumber, String corruptionNumber) throws IOException {
        FileChannel fc = getFileChannel(recordCounts);

        fc.lock(0, Long.MAX_VALUE, false);

        int checkCount = getCountFromLog(recordCounts, bucket, populationName, populationSize, populationNumber, corruptionNumber);

        if(checkCount == -1) { // if still not in file then write (may have been written by another instance in the meantime)

            String toFileString = populationName + "," + populationSize + "," + populationNumber + ","
                    + corruptionNumber + "," + bucket + "," + count + "," + LocalDateTime.now().toString()
                    + System.lineSeparator();

            ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
            buf.clear();
            buf.put(toFileString.getBytes());

            buf.flip();

            while (buf.hasRemaining()) {
                fc.write(buf);
            }
        }

        fc.close();
    }

    private void importRecordsTo(String bucket, RecordRepository recordRepository) throws IOException, BucketException {
        switch (bucket) {
            case RecordRepository.BIRTHS_BUCKET_NAME:
                recordRepository.importBirthRecords(SyntheticBirthsDataSet.factory(populationName, populationSize,
                        populationNumber, corrupted, corruptionNumber));
                break;
            case RecordRepository.DEATHS_BUCKET_NAME:
                recordRepository.importDeathRecords(SyntheticDeathsDataSet.factory(populationName, populationSize,
                        populationNumber, corrupted, corruptionNumber));
                break;
            case RecordRepository.MARRIAGES_BUCKET_NAME:
                recordRepository.importMarriageRecords(SyntheticMarriagesDataSet.factory(populationName, populationSize,
                        populationNumber, corrupted, corruptionNumber));
                break;

        }
    }


    private int getCountFromRawFile(String bucket) throws IOException {

        switch (bucket) {
            case RecordRepository.BIRTHS_BUCKET_NAME:
                return SyntheticBirthsDataSet.factory(populationName, populationSize, populationNumber,
                        corrupted, corruptionNumber).getRecords().size();
            case RecordRepository.DEATHS_BUCKET_NAME:
                return SyntheticDeathsDataSet.factory(populationName, populationSize, populationNumber,
                        corrupted, corruptionNumber).getRecords().size();
            case RecordRepository.MARRIAGES_BUCKET_NAME:
                return SyntheticMarriagesDataSet.factory(populationName, populationSize, populationNumber,
                        corrupted, corruptionNumber).getRecords().size();

        }

        throw new RuntimeException("Bucket not found: " + sourceRepoName + ":" + bucket);

    }

    public static int getCountFromLog(Path recordCounts, String bucket, String populationName, String populationSize, String populationNumber, String corruptionNumber) throws IOException {

        List<String> counts = FileManipulation.readAllLines(FileManipulation.getInputStream(recordCounts));

        if(counts.size() == 0) {
            return -1;
        }

        List<String> columnLabels = Arrays.asList(counts.get(0).split(","));

        for(int i = 1; i < counts.size(); i++) {
            List<String> row = Arrays.asList(counts.get(i).split(","));

            if(populationName.equals(row.get(columnLabels.indexOf("population"))) &&
                    populationSize.equals(row.get(columnLabels.indexOf("size"))) &&
                    populationNumber.equals(row.get(columnLabels.indexOf("pop#"))) &&
                    corruptionNumber.equals(row.get(columnLabels.indexOf("corruption#"))) &&
                    bucket.equals(row.get(columnLabels.indexOf("bucket"))))
            {
                return Integer.parseInt(row.get(columnLabels.indexOf("count")));
            }

        }

        return -1;

    }

    private static FileChannel getFileChannel(Path jobFile) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<>(Sets.newHashSet(StandardOpenOption.WRITE, StandardOpenOption.APPEND));
        return FileChannel.open(jobFile, options);
    }

}
