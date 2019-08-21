package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Constants;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.RecordPair;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Sigma;
import uk.ac.standrews.cs.population_linkage.experiments.linkage.Utilities;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.storr.impl.LXP;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.StringMetric;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IntrinsicDimensionalityCalculator {

    private String sourceRepoName;
    private String populationName;
    private String populationSize;
    private String populationNumber;
    private String corruptionNumber;
    private Path resultsFile;
    private int numberOfRecords;

    public IntrinsicDimensionalityCalculator(String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber, Path resultsFile, int numberOfRecords) {

        this.populationName = populationName;
        this.populationSize = populationSize;
        this.populationNumber = populationNumber;
        this.corruptionNumber = corruptionNumber;
        this.resultsFile = resultsFile;

        if(corrupted)
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            sourceRepoName = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
            corruptionNumber = "0";
        }

        this.numberOfRecords = numberOfRecords;
    }

    public void calculate(String stringMetric, int sampleN, String fieldsDescriptor, List<Integer> fields) {
        System.out.println("Calculating intrinsic dimensionality for population: " + sourceRepoName);

        StringMetric metric = Constants.get(stringMetric, 2048);

        try {
            FileManipulation.createFileIfDoesNotExist(resultsFile);
            if(FileManipulation.countLines(resultsFile) == 0) {
                new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                        .appendToFile("population,size,pop#,corruption#,metric,intrinsic-dimensionality,sample-size,calc-time-seconds,on-fields" +
                        System.lineSeparator());
            }

            long startTime = System.currentTimeMillis();
            double instinsicDimensionality = calculateIntrinsicDimensionality(sourceRepoName, metric, fields, sampleN, numberOfRecords);
            long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

            new FileChannelHandle(resultsFile, FileChannelHandle.optionsWA)
                    .appendToFile((populationName + "," + populationSize + "," + populationNumber + "," +
                    corruptionNumber + "," + stringMetric +  "," + instinsicDimensionality + "," + sampleN + "," +
                    timeTakenInSeconds + "," + fieldsDescriptor + System.lineSeparator()));

//            Files.write(resultsFile, .getBytes(),
//                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double calculateIntrinsicDimensionality(final String source_repository_name, StringMetric baseMetric, List<Integer> fields, int sampleN, int numberOfRecords) {

        final Path store_path = ApplicationProperties.getStorePath();
        final RecordRepository record_repository = new RecordRepository(store_path, source_repository_name);

        Sigma distanceFunction = new Sigma(baseMetric, fields);

        List<RecordPair> pairs = new ArrayList<>();

        int everyNthPair = (int) Math.pow(numberOfRecords, 2) / sampleN;

        long consideredPairs = 0;
        long sampledPairs = 0;
        double sumOfDistances = 0;

        for(LXP record1 : Utilities.getBirthRecords(record_repository)) {
            for(LXP record2 : Utilities.getBirthRecords(record_repository)) {

                if(toSample(consideredPairs, everyNthPair)) {
                    double distance = distanceFunction.calculateDistance(record1, record2);
                    pairs.add(new RecordPair(record1, record2, distance));

                    sampledPairs++;
                    sumOfDistances += distance;
                }

                consideredPairs++;
            }
        }

        double mean = sumOfDistances / sampledPairs;

        double cumalativeSum = 0;

        for(RecordPair pair : pairs) {
            cumalativeSum += Math.pow(pair.distance - mean, 2) / sampledPairs;
        }

        double standardDeviation = Math.sqrt(cumalativeSum);

        double intrinsicDimensionality = (mean*mean) / Math.pow(2*standardDeviation, 2);

        System.out.println("Sampled Pairs: " + sampledPairs);
        System.out.println("mean of distances: " + mean);
        System.out.println("standard deviation: " + standardDeviation);
        System.out.println("Intrinsic Dimensionality: " + intrinsicDimensionality);

        record_repository.stopStoreWatcher();

        return intrinsicDimensionality;

    }

    private boolean toSample(long consideredPairs, int everyNthPair) {
        if(everyNthPair <= 1) return true;
        return consideredPairs % everyNthPair == 0;
    }

    public static void main(String[] args) {

        List<Integer> fields = Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS;
        String fieldDescriptors = Constants.SIBLING_BUNDLING_BIRTH_LINKAGE_FIELDS_AS_STRINGS;

        new IntrinsicDimensionalityCalculator(
                args[0], args[1], args[2], args[3].equals("true"), args[4], Paths.get(args[5]), Integer.valueOf(args[6])
        ).calculate(args[7], Integer.parseInt(args[8]), fieldDescriptors, fields);

    }

//    public static void countAll(Path resultsFile) {
//
//        String[] populationNames   = {"synthetic-scotland"};
//        String[] populationSizes   = {"13k","133k"}; // ,"530k"
//        String[] populationNumbers = {"1"}; // ,"2","3","4","5"
//        String[] corruptionNumbers = {"0"}; // ,"1","2"
//
//        for(String populationName : populationNames)
//            for (String populationSize : populationSizes)
//                for(String populationNumber : populationNumbers)
//                    for(String corruptionNumber : corruptionNumbers)
//                        new GroundTruthLinkCounter(populationName, populationSize,
//                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber, resultsFile).count();
//
//
//    }
}
