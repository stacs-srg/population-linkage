/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatusFileHandler {

    private static final String COMMENT_INDICATOR = "#";

    public static boolean getStatus(Path statusPath) throws IOException, InterruptedException {

        do {
            ArrayList<String> lines = new ArrayList<>(getAllLines(statusPath));

            if (!lines.isEmpty()) {
                switch (lines.get(0)) {
                    case "run":
                        return true;
                    case "terminate":
                        return false;
                    case "pause":
                        System.out.println("Status job file indicates pause @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        Thread.sleep(10000);
                        break;
                }
            }
        } while (true);
    }

    private static List<String> getAllLines(Path path) throws IOException {

        List<String> lines = new ArrayList<>();

        // Reads in all lines to a collection of Strings
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.startsWith(COMMENT_INDICATOR) && line.length() != 0) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }
}
