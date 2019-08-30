package uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.helpers;

import com.google.common.collect.Sets;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.linkage_runners.SSBirthDeathSiblingLinkageRunner;
import uk.ac.standrews.cs.population_linkage.experiments.synthetic.linkage.linkage_runners.SyntheticBirthBirthSiblingLinkageRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LinkageJobQueueHandler {

    public static void main(String[] args) throws Exception {
        Path jobQ = Paths.get(args[0]);
        Path linkageResultsFile = Paths.get(args[1]);
        Path recordCountsFile = Paths.get(args[2]);
        Path gtCountsFile = Paths.get(args[3]);
        Path statusFile = Paths.get(args[4]);

        while(getStatus(statusFile)) {

            FileChannel fileChannel = getFileChannel(jobQ);
            System.out.println("Locking job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            fileChannel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Locked job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            List<List<String>> jobs = readInJobFile(fileChannel);

            if (jobs.size() > 1) {
                // jobs in queue
                List<String> columnLabels = jobs.get(0);
                List<String> job = jobs.remove(1);

                System.out.println("Job taken: " + job);

                overwriteToJobFile(fileChannel, jobs);
                fileChannel.close();
                System.out.println("Released job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // At this point we have a linkage job no body else has - now we need to:
                // put job info into variables

                String populationName = job.get(columnLabels.indexOf("population")).trim();
                String populationSize = job.get(columnLabels.indexOf("size")).trim();
                String populationNumber = job.get(columnLabels.indexOf("pop#")).trim();
                String corruptionNumber = job.get(columnLabels.indexOf("corruption#")).trim();
                boolean corrupted = !corruptionNumber.equals("0");
                double threshold = Double.valueOf(job.get(columnLabels.indexOf("threshold")).trim());
                String metric = job.get(columnLabels.indexOf("metric")).trim();
                int maxSiblingGap = Integer.valueOf(job.get(columnLabels.indexOf("max-sibling-gap")).trim());
                int birthsCacheSize = Integer.valueOf(job.get(columnLabels.indexOf("births-cache-size")).trim());
                int marriagesCacheSize = Integer.valueOf(job.get(columnLabels.indexOf("marriages-cache-size")).trim());
                int deathsCacheSize = Integer.valueOf(job.get(columnLabels.indexOf("deaths-cache-size")).trim());
                int numROs = Integer.valueOf(job.get(columnLabels.indexOf("#ROs")).trim());
                String linkageType = job.get(columnLabels.indexOf("linkage-type")).trim();

                // validate the data is in the storr (local scratch space on clusters - but anyway, it's defined in application.properties)
                new ValidatePopulationInStorr(populationName, populationSize, populationNumber, corrupted, corruptionNumber)
                        .validate(recordCountsFile);

                switch (linkageType) {
                    case SyntheticBirthBirthSiblingLinkageRunner.linkageApproach:
                        SyntheticBirthBirthSiblingLinkageRunner sbbslr = new SyntheticBirthBirthSiblingLinkageRunner(populationName, populationSize, populationNumber, corrupted,
                                corruptionNumber, linkageResultsFile, birthsCacheSize, numROs);

                        int numberOfGTLinks = new GroundTruthLinkCounter(populationName, populationSize, populationNumber,
                                corrupted, corruptionNumber, gtCountsFile).count(sbbslr); //, SyntheticBirthBirthSiblingLinkageRunner.linkageApproach);

                        sbbslr.link(threshold, metric, numberOfGTLinks, maxSiblingGap);
                        break;
                    case SSBirthDeathSiblingLinkageRunner.linkageApproach:
                        SSBirthDeathSiblingLinkageRunner ssbdslr = new SSBirthDeathSiblingLinkageRunner(populationName, populationSize, populationNumber, corrupted,
                                corruptionNumber, linkageResultsFile, birthsCacheSize, deathsCacheSize, numROs);

                        numberOfGTLinks = new GroundTruthLinkCounter(populationName, populationSize, populationNumber,
                                corrupted, corruptionNumber, gtCountsFile).count(ssbdslr); //, SSBirthDeathSiblingLinkageRunner.linkageApproach);

                        ssbdslr.link(threshold, metric, numberOfGTLinks, maxSiblingGap);
                }

            } else {
                fileChannel.close();
                System.out.println("No jobs in job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                Thread.sleep(60000);
            }
        }


    }

    private static FileChannel getFileChannel(Path jobFile) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<>(Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE));
        return FileChannel.open(jobFile, options);
    }

    private static List<List<String>> readInJobFile(FileChannel jobFile) throws IOException, InterruptedException {

        ByteBuffer buffer = ByteBuffer.allocate((int) jobFile.size());
        int noOfBytesRead = jobFile.read(buffer);

        List<List<String>> data = new ArrayList<>();

        while (noOfBytesRead != -1) {

            buffer.flip();

            String line = "";

            while (buffer.hasRemaining()) {

                char c = (char) buffer.get();
                if(c == '\n') {

                    data.add(Arrays.asList(line.split(",")));

                    line = "";
                } else {
                    line += c;
                }

            }

            if(!line.equals("")) data.add(Arrays.asList(line.split(",")));

            buffer.clear();
            Thread.sleep(1000);
            noOfBytesRead = jobFile.read(buffer);
        }


        return data;
    }


    public static void overwriteToJobFile(FileChannel jobFile, List<List<String>> jobs) throws IOException {
        jobFile.truncate(0);


        StringBuilder sb = new StringBuilder();

        for(List<String> row : jobs) {
            boolean first = true;
            for (String value : row) {
                if(first) {
                    sb.append(value);
                    first = false;
                } else {
                    sb.append(",").append(value);
                }
            }
            sb.append(System.lineSeparator());
        }

        String toFileString = sb.toString();


        ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
        buf.clear();
        buf.put(toFileString.getBytes());

        buf.flip();

        while(buf.hasRemaining()) {
            jobFile.write(buf);
        }
    }

    private static boolean getStatus(Path statusPath) throws IOException {
        // read in file
        ArrayList<String> lines = new ArrayList<>(getAllLines(statusPath));

        if(!lines.isEmpty()) {
            switch (lines.get(0)) {
                case "run": return true;
                case "terminate" : return false;
            }
        }

        return true;
    }

    private static final String COMMENT_INDICATOR = "#";

    public static List<String> getAllLines(Path path) throws IOException {

        List<String> lines = new ArrayList<>();

        // Reads in all lines to a collection of Strings
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.startsWith(COMMENT_INDICATOR) && line.length() != 0) {
                    lines.add(line);
                }
            }
            reader.close();
        }

        return lines;
    }

}
