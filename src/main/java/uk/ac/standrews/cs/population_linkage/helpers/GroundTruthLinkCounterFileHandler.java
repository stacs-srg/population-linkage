/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.util.ArrayList;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import uk.ac.standrews.cs.population_linkage.linkageRecipes.helpers.evaluation.approaches.EvaluationApproach;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GroundTruthLinkCounterFileHandler {

    private Path gtCountsFile;

    public GroundTruthLinkCounterFileHandler(Path gtCountsFile) {
        this.gtCountsFile = gtCountsFile;
    }

    private static final String DATASET = "dataset";
    private static final String LINKAGE_RECIPE = "linkage-recipe";
    private static final String EVALUATION_APPROACH = "evaluation-approach";
    private static final String GT_LINKS = "gt-links";
    private static final String TIME = "count-time-seconds";

    public long getOrCalcCount(EvaluationApproach evaluationApproach) {
        System.out.printf("Count ground truth links in population: %s using evaluation approach: %s\n",
                evaluationApproach.getDataSetName(), evaluationApproach.getEvaluationDescription());

        if(gtCountsFile == null) {
            return evaluationApproach.getNumberOfGroundTruthTrueLinks();
        }

        try {
            FileManipulation.createFileIfDoesNotExist(gtCountsFile);
            if(FileManipulation.countLines(gtCountsFile) == 0) {
                new FileChannelHandle(gtCountsFile, FileChannelHandle.optionsWA)
                        .appendToFile(makeFileRow(DATASET, LINKAGE_RECIPE, EVALUATION_APPROACH, GT_LINKS, TIME));
            }

            // check if count in file
            long numberOfGTLinks = getCountFromLog(gtCountsFile, evaluationApproach.getDataSetName(), evaluationApproach.getLinkageRecipeClassName(), evaluationApproach.getEvaluationDescription());

            if(numberOfGTLinks == -1) { // if count not already done then do count
                System.out.println("Ground truth links count not in file will count from repo: " + evaluationApproach.getDataSetName());
                long startTime = System.currentTimeMillis();
                numberOfGTLinks = evaluationApproach.getNumberOfGroundTruthTrueLinks();
                long timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000;

                new FileChannelHandle(gtCountsFile, FileChannelHandle.optionsWA)
                        .appendToFile(makeFileRow(evaluationApproach.getDataSetName(),
                                evaluationApproach.getLinkageRecipeClassName(), evaluationApproach.getEvaluationDescription().name(),
                                String.valueOf(numberOfGTLinks), String.valueOf(timeTakenInSeconds)));
            }

            return numberOfGTLinks;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeFileRow(String... labels) {
        return StringUtils.join(labels, ",") + System.lineSeparator();
    }

    private int getCountFromLog(Path gtCountsFile, String datasetName, String linkageRecipe, EvaluationApproach.Type evaluationApproach) throws IOException {

        Table table = new Table(FileManipulation.readAllLines(FileManipulation.getInputStream(gtCountsFile)));

        if(table.size() == 0) {
            return -1;
        }

        for(Row row : table.rows) {
            if(row.valueEquals(datasetName, DATASET) &&
                    row.valueEquals(linkageRecipe, LINKAGE_RECIPE) &&
                    row.valueEquals(evaluationApproach.name(), EVALUATION_APPROACH)) {
                return Integer.parseInt(row.getValue(GT_LINKS));
            }
        }

        return -1;
    }

    private static class Table {
        static Row columnLabels;
        List<Row> rows = new ArrayList<>();

        Table(List<String> rows) {
            if(rows.size() != 0) {
                columnLabels = new Row(rows.get(0));
                for(int r = 1; r < rows.size(); r++)
                    this.rows.add(new Row(rows.get(r)));
            }
        }

        int size() {
            return rows.size();
        }

    }

    private static class Row {
        List<String> columns;

        Row(String row) {
            columns = Arrays.asList(row.split(","));
        }

        public boolean valueEquals(String value, String columnLabel) {
            return Objects.equals(value, getValue(columnLabel));
        }

        public String getValue(String columnLabel) {
            return columns.get(Table.columnLabels.columns.indexOf(columnLabel));
        }
    }
}
