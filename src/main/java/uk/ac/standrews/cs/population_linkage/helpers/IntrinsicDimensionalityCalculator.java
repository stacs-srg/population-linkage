/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.supportClasses.Constants;
import uk.ac.standrews.cs.population_linkage.supportClasses.RecordPair;
import uk.ac.standrews.cs.population_linkage.supportClasses.Sigma;
import uk.ac.standrews.cs.population_linkage.supportClasses.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntrinsicDimensionalityCalculator {

    public static final List<Integer> SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS = Arrays.asList(

            Birth.FATHER_FORENAME,
            Birth.FATHER_SURNAME,
            Birth.MOTHER_FORENAME,
            Birth.MOTHER_MAIDEN_SURNAME,
            Birth.PARENTS_PLACE_OF_MARRIAGE,
            Birth.PARENTS_DAY_OF_MARRIAGE,
            Birth.PARENTS_MONTH_OF_MARRIAGE,
            Birth.PARENTS_YEAR_OF_MARRIAGE
    );

    private String sourceRepoName;
    private String populationName;
    private String populationSize = "";
    private String populationNumber = "";
    private String corruptionNumber = "";
    private Path resultsFile;
    private int numberOfRecords;

    public IntrinsicDimensionalityCalculator(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber, Path resultsFile, int numberOfRecords) {

        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;

        if (corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

        this.numberOfRecords = numberOfRecords;
    }

    public IntrinsicDimensionalityCalculator(String sourceRepoName, Path resultsFile, int numberOfRecords) {
        this.sourceRepoName = sourceRepoName;
        this.numberOfRecords = numberOfRecords;
        this.populationName = sourceRepoName;
        this.resultsFile = resultsFile;
    }

    public static void main(String[] args) throws Exception {

        List<Integer> fields = SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS; // Insert desired fields
        String fieldDescriptors = Constants.stringRepresentationOf(fields, Birth.class, Birth.getLabels());

        new IntrinsicDimensionalityCalculator(
                args[0], args[1], args[2], args[3].equals("true"), args[4], Paths.get(args[5]), Integer.parseInt(args[6])
        ).calculate(args[7], Integer.parseInt(args[8]), fieldDescriptors, fields);
    }

    public static void countAll(Path idCalcsFile, Path recordCountsFile) throws Exception {

        String[] populationNames = {"synthetic-scotland"};
        String[] populationSizes = {"13k", "133k", "530k"};
        String[] populationNumbers = {"1", "2", "3", "4", "5"};
        String[] corruptionNumbers = {"0"}; //,"1","2"};

        for (String populationName : populationNames)
            for (String populationSize : populationSizes)
                for (String populationNumber : populationNumbers)
                    for (String corruptionNumber : corruptionNumbers) {
                        new ValidatePopulationInStorr(populationName, populationSize, populationNumber, !corruptionNumber.equals("0"), corruptionNumber)
                                .validate(recordCountsFile);

                        int recordCount = ValidatePopulationInStorr.getCountFromLog(recordCountsFile,
                                RecordRepository.BIRTHS_BUCKET_NAME, populationName, populationSize,
                                populationNumber, corruptionNumber);

                        new IntrinsicDimensionalityCalculator(populationName, populationSize,
                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber,
                                idCalcsFile, recordCount).calculate("JENSEN_SHANNON", 100000,
                                Constants.stringRepresentationOf(SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS, Birth.class, Birth.getLabels()),
                                SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS);
                    }


    }

    public void calculate(String stringMetric, int sampleN, String fieldsDescriptor, List<Integer> fields) {
        System.out.println("Calculating intrinsic dimensionality for population: " + sourceRepoName);

        StringMetric metric = Constants.get(stringMetric, 2048);

        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if (FileManipulation.countLines(resultsFile) == 0) {
                new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                        .appendToFile("population,size,pop#,corruption#,metric,intrinsic-dimensionality,sample-size,calc-time-seconds,on-fields" +
                                System.lineSeparator());
            }

            long startTime = System.currentTimeMillis();
            double instinsicDimensionality = calculateIntrinsicDimensionality(sourceRepoName, metric, fields, sampleN, numberOfRecords);
            long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

            new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                    .appendToFile((populationName + "," + populationSize + "," + populationNumber + "," +
                            corruptionNumber + "," + stringMetric + "," + instinsicDimensionality + "," + sampleN + "," +
                            timeTakenInSeconds + "," + fieldsDescriptor + System.lineSeparator()));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double calculateIntrinsicDimensionality(final String source_repository_name, StringMetric baseMetric, List<Integer> fields, int sampleN, int numberOfRecords) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        Sigma distanceFunction = new Sigma(baseMetric, fields, 0);

        List<RecordPair> pairs = new ArrayList<>();

        long sampledPairs = 0;
        double sumOfDistances = 0;

        List<LXP> births = new ArrayList<>();

        for (LXP r : Utilities.getBirthRecords(record_repository))
            births.add(r);


        for (LXP record1 : births) {
            for (LXP record2 : births) {

                if (record1.getString(Birth.FAMILY).trim().equals(record2.getString(Birth.FAMILY).trim())) {
                    double distance = distanceFunction.calculateDistance(record1, record2);
                    pairs.add(new RecordPair(record1, record2, distance));

                    sampledPairs++;
                    sumOfDistances += distance;
                }
            }
        }

        double mean = sumOfDistances / sampledPairs;

        double cumulativeDeviation = 0;

        for (RecordPair pair : pairs) {
            cumulativeDeviation += Math.pow(pair.distance - mean, 2);
        }

        double standardDeviation = Math.sqrt(cumulativeDeviation / (double) sampledPairs);

        double intrinsicDimensionality = (Math.pow(mean, 2)) / (2 * Math.pow(standardDeviation, 2));

        System.out.println("Sampled Pairs: " + sampledPairs);
        System.out.println("mean of distances: " + mean);
        System.out.println("standard deviation: " + standardDeviation);
        System.out.println("Intrinsic Dimensionality: " + intrinsicDimensionality);

        record_repository.stopStoreWatcher();

        return intrinsicDimensionality;
    }
}
