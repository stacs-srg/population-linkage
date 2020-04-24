/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.data.synthetic;


import uk.ac.standrews.cs.data.synthetic.SyntheticBirthsDataSet;
import uk.ac.standrews.cs.data.synthetic.SyntheticDeathsDataSet;
import uk.ac.standrews.cs.data.synthetic.SyntheticMarriagesDataSet;
import uk.ac.standrews.cs.population_linkage.ApplicationProperties;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.nio.file.Path;

public class ImportSyntheticScotlandRecordsToStore {

    private final Path store_path;
    private final String repo_name;
    private final String populationName;
    private final String populationSize;
    private final String populationNumber;
    private final boolean corrupted;
    private final String corruptionNumber;

    public ImportSyntheticScotlandRecordsToStore(Path store_path, String populationName, String populationSize, String populationNumber, boolean corrupted, String corruptionNumber) {

        this.store_path = store_path;
        this.populationNumber = populationNumber;
        this.populationName = populationName;
        this.populationSize = populationSize;
        this.corrupted = corrupted;
        this.corruptionNumber = corruptionNumber;

        if(corrupted)
            this.repo_name = populationName + "_" + populationSize + "_" + populationNumber + "_corrupted_" + corruptionNumber;
        else {
            this.repo_name = populationName + "_" + populationSize + "_" + populationNumber + "_clean";
        }

        System.out.println("REPO NAME: " + this.repo_name);
    }

    public RecordRepository run() throws Exception {

        RecordRepository record_repository = new RecordRepository(store_path, repo_name);

        System.out.println("Importing Synthetic records into repository: " + repo_name);
        System.out.println();

        DataSet birth_records = SyntheticBirthsDataSet.factory(populationName, populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importBirthRecords(birth_records);
        System.out.println("Imported " + birth_records.getRecords().size() + " birth records");

        DataSet death_records = SyntheticDeathsDataSet.factory(populationName, populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importDeathRecords(death_records);
        System.out.println("Imported " + death_records.getRecords().size() + " death records");

        DataSet marriage_records = SyntheticMarriagesDataSet.factory(populationName, populationSize, populationNumber, corrupted, corruptionNumber);
        record_repository.importMarriageRecords(marriage_records);
        System.out.println("Imported " + marriage_records.getRecords().size() + " marriage records");

        System.out.println();
        System.out.println("Complete");

        return record_repository;
    }

    public static void main(String[] args) throws Exception {

        Path store_path = ApplicationProperties.getStorePath();

//        new ImportSyntheticScotlandRecordsToStore(store_path, args[0], args[1], args[2], args[3].equals("true"), args[4]).run();
        addAllToStore(store_path);
    }

    public static void addAllToStore(Path store_path) throws Exception {

        String[] populationNames   = {"synthetic-scotland"};
        String[] populationSizes   = {"13k"};    // TOM: ,"133k","530k"
        String[] populationNumbers = {"1","2","3","4","5"};     // TOM: ,"2","3","4","5"
        String[] corruptionNumbers = {"0","A","B","C"};

        for(String populationName : populationNames)
            for (String populationSize : populationSizes)
                for(String populationNumber : populationNumbers)
                    for(String corruptionNumber : corruptionNumbers)
                        new ImportSyntheticScotlandRecordsToStore(store_path, populationName, populationSize,
                                populationNumber, !corruptionNumber.equals("0"), corruptionNumber).run();


    }
}
