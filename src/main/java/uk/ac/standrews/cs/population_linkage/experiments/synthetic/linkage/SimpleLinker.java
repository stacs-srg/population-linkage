package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage;

import uk.ac.standrews.cs.data.synthetic.scot_test.SyntheticScotlandBirthsDataSet;
import uk.ac.standrews.cs.utilities.ClassificationMetrics;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleLinker {

    public static void main(String[] args) throws IOException {
//        Path store_path = ApplicationProperties.getStorePath();

        long startTime = System.nanoTime();

        DataSet birth_records = SyntheticScotlandBirthsDataSet.factory(args[0], args[1], args[2].equals("true"), args[3]);

        long readInTime = System.nanoTime();
        System.out.println("Read in in (s): " + (readInTime - startTime)/1E9);

        HashMap<String, ArrayList<SyntheticBirthRecord>> siblingBundles = new HashMap<>();
        HashMap<String, ArrayList<SyntheticBirthRecord>> gtSiblingBundles = new HashMap<>();

        birth_records.getRecords().forEach((record) -> {
            SyntheticBirthRecord birthRecord = convertToRecord(birth_records.getColumnLabels(), record);

            addToLinkingStructure(siblingBundles, birthRecord, birthRecord.FFN_MFN_FSN_MMSN());
            addToGTStructure(gtSiblingBundles, birthRecord);
        });

        long linkageTime = System.nanoTime();
        System.out.println("Linkage in (s): " + (linkageTime - readInTime)/1E9);

        HashMap<String, ArrayList<ArrayList<SyntheticBirthRecord>>> restructuredSiblingBundles = new HashMap<>();

        int[] triangleNumbers = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 66, 78, 91, 105, 120,136, 153, 171, 190, 210,
                231, 253, 276, 300, 325, 351, 378, 406, 435, 465, 496, 528, 561, 595, 630, 666, 703, 741, 780, 820, 861,
                903, 946, 990, 1035, 1081, 1128, 1176, 1225, 1275, 1326, 1378, 1431};

        AtomicInteger true_positives = new AtomicInteger();
        AtomicInteger false_positives = new AtomicInteger();
        AtomicInteger false_negatives = new AtomicInteger();

        siblingBundles.forEach((linkageKey, linkedFamily) -> {
            HashMap<String, Integer> famIDCounts = new HashMap<>();
            for(SyntheticBirthRecord br: linkedFamily) {
                Integer count = 1;
                if((count = famIDCounts.get(br.familyID)) != null) {
                    famIDCounts.put(br.familyID, ++count);
                } else {
                    famIDCounts.put(br.familyID, 1);
                }
            }

            AtomicReference<String> dominantFamID = new AtomicReference<>();
            AtomicInteger dominantCount = new AtomicInteger();

            famIDCounts.forEach((id, idCount) -> {
                if(idCount > dominantCount.get()) {
                    dominantFamID.set(id);
                    dominantCount.set(idCount);
                }
            });

            ArrayList<ArrayList<SyntheticBirthRecord>> idGroup = restructuredSiblingBundles.get(dominantFamID.get());
            if(idGroup ==  null) {
                idGroup = new ArrayList<>();
            }
            idGroup.add(linkedFamily);
            restructuredSiblingBundles.put(dominantFamID.get(), idGroup);

        });

        Map<String, ArrayList<Integer>> peopleCountsInWrongFamilyGroupedByWrongFamilyGroupedByGTFamily = new HashMap<>();

        restructuredSiblingBundles.forEach((ID, families) -> {

            ArrayList<SyntheticBirthRecord> gtFamily = gtSiblingBundles.get(ID);

            for(ArrayList<SyntheticBirthRecord> fam : families) {

                // how many people incorrectly in family? I
                int i = incorrectlyInFamily(fam, ID);
                // how many people correctly in family? C
                int c = correctlyInFamily(fam, ID);
                // how many people missing from family? M
                int m = missingFromFamily(fam, gtFamily, ID);

                // fp = I*C
                false_positives.addAndGet(i * c);

                // tp = trinagleNumbers[C]
                true_positives.addAndGet(triangleNumbers[c]);

                // fn = M*C
                false_negatives.addAndGet(m*c);

                if(m*c != 0) {

                    String famString = familyToLabeledString(gtFamily, "GT");
                    famString += familyToLabeledString(fam, "Linked");

                    System.out.println("FN Fam\n" + famString);

                }

                if(i > 1) {
                    Map<String, Integer> grouped = incorrectlyInFamilyGroupedByGTFamilyID(fam, ID);
                    grouped.forEach((famID, n) -> {
                        true_positives.addAndGet(triangleNumbers[n]);
                        peopleCountsInWrongFamilyGroupedByWrongFamilyGroupedByGTFamily.putIfAbsent(famID, new ArrayList<>());
                        peopleCountsInWrongFamilyGroupedByWrongFamilyGroupedByGTFamily.get(famID).add(n);
                    });
                }
            }
        });

        AtomicInteger sum = new AtomicInteger();
        peopleCountsInWrongFamilyGroupedByWrongFamilyGroupedByGTFamily.forEach((gtID, groups) -> {
            groups.forEach((group) -> {
                sum.addAndGet(group * (groups.size() - 1));
            });
        });

        System.out.println("Out of set FNs: " + sum.get() / 2);

        false_negatives.addAndGet(sum.get() / 2);

        long evalTime = System.nanoTime();
        System.out.println("Evaluation in (s): " + (evalTime - linkageTime)/1E9);

        System.out.println("TP: " + true_positives);
        System.out.println("FP: " + false_positives);
        System.out.println("FN: " + false_negatives);

        double precision = ClassificationMetrics.precision(true_positives.get(), false_positives.get());
        double recall = ClassificationMetrics.recall(true_positives.get(), false_negatives.get());
        double f_measure = ClassificationMetrics.F1(true_positives.get(), false_positives.get(), false_negatives.get());

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F-Measure: " + f_measure);

        System.out.println("");




    }

    private static String familyToLabeledString(ArrayList<SyntheticBirthRecord> fam, String label) {

        StringBuilder s = new StringBuilder();

        s.append("-S----- " + label + " -------\n");
        for(SyntheticBirthRecord r : fam) {
            s.append(r.toString());
        }
        s.append("-E----- " + label + " -------\n");

        return s.toString();


    }

    private static int incorrectlyInFamily(ArrayList<SyntheticBirthRecord> fam, String ID) {

        AtomicInteger count = new AtomicInteger();

        fam.forEach((person) -> {
            if(!person.familyID.equals(ID))
                count.getAndIncrement();
        });

        return count.get();
    }

    private static Map<String, Integer> incorrectlyInFamilyGroupedByGTFamilyID(ArrayList<SyntheticBirthRecord> fam, String ID) {

        Map<String, Integer> map = new HashMap<>();

        fam.forEach((person) -> {


            if(!person.familyID.equals(ID)) {
                map.putIfAbsent(person.familyID, 0);
                Integer c = map.get(person.familyID);
                map.put(person.familyID, ++c);
            }
        });

        return map;

    }

    private static int correctlyInFamily(ArrayList<SyntheticBirthRecord> fam, String ID) {

        AtomicInteger count = new AtomicInteger();

        fam.forEach((person) -> {
            if(person.familyID.equals(ID))
                count.getAndIncrement();
        });

        return count.get();
    }

    private static int missingFromFamily(ArrayList<SyntheticBirthRecord> fam, ArrayList<SyntheticBirthRecord> gtFam, String ID) {

        AtomicInteger count = new AtomicInteger();

        gtFam.forEach((person) -> {
            if(!fam.contains(person)) {
                count.getAndIncrement();
            }
        });

        return count.get();
    }

    private static void addToLinkingStructure(HashMap<String, ArrayList<SyntheticBirthRecord>> siblingBundles, SyntheticBirthRecord birthRecord, String linkageKey) {
        ArrayList<SyntheticBirthRecord> family = siblingBundles.get(linkageKey);

        if(family != null) {
            family.add(birthRecord);
        } else {
            ArrayList<SyntheticBirthRecord> newFam = new ArrayList<>();
            newFam.add(birthRecord);
            siblingBundles.put(linkageKey, newFam);
        }
    }

    private static void addToGTStructure(HashMap<String, ArrayList<SyntheticBirthRecord>> gtSiblingBundles, SyntheticBirthRecord birthRecord) {
        ArrayList<SyntheticBirthRecord> family = gtSiblingBundles.get(birthRecord.familyID);

        if(family != null) {
            family.add(birthRecord);
        } else {
            ArrayList<SyntheticBirthRecord> newFam = new ArrayList<>();
            newFam.add(birthRecord);
            gtSiblingBundles.put(birthRecord.familyID, newFam);
        }
    }

    public static  SyntheticBirthRecord convertToRecord(List<String> labels, List<String> fields) {

        return new SyntheticBirthRecord(
                fields.get(labels.indexOf("FORENAME")),
                        fields.get(labels.indexOf("SURNAME")),
                                fields.get(labels.indexOf("FATHER_FORENAME")),
                                        fields.get(labels.indexOf("FATHER_SURNAME")),
                                                fields.get(labels.indexOf("MOTHER_FORENAME")),
                                                        fields.get(labels.indexOf("MOTHER_MAIDEN_SURNAME")),
                                                                        fields.get(labels.indexOf("PARENTS_YEAR_OF_MARRIAGE")),
                                                                                fields.get(labels.indexOf("PARENTS_PLACE_OF_MARRIAGE")),
                                                                                        fields.get(labels.indexOf("FAMILY")),
                                                                                                fields.get(labels.indexOf("CHILD_IDENTITY"))
        );


    }





}
