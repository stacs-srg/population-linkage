/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers.jobq.job;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Streams;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Hex;

public class EntitiesList<T> extends ArrayList<T> {

    private RandomAccessFile randomAccessFile;
    private String jobListFile;
    private final CsvMapper csvMapper = new CsvMapper();
    private final Class<T> type;
    private final String fileName;

    private Random rand = new Random();

    public EntitiesList(Class<T> type, String jobListFile, Lock lock) throws IOException, InterruptedException {
        this.type = type;
        this.fileName = jobListFile;
        this.jobListFile = jobListFile;
        lockFile(lock);
        addAll(readEntriesFromFile());
    }

    // for testing only
    protected EntitiesList(Class<T> type) {
        this.randomAccessFile = null;
        this.jobListFile = null;
        this.type = type;
        this.fileName = null;
    }

    public enum Lock {
        RESULTS,
        JOBS
    }

    protected void lockFile(Lock lock) throws InterruptedException {
        System.out.println("Locking " + lock.name() + " job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Thread.sleep(rand.nextInt(1000));

        File lockFile = new File(String.format("lock-%s.txt", lock));
        while(!aquiredLock(lockFile, lock)) {
            Thread.sleep(rand.nextInt(10000));
            System.out.println("Sleeping on " + lock.name() + " file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        System.out.println("Locked " + lock.name() + " file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private boolean aquiredLock(File lockFile, Lock lock) throws InterruptedException {
        Thread.sleep(5000);
        try {
            System.out.println("Trying to lock file on " + lock.name() + " lock file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            boolean createdNewFile = lockFile.createNewFile();

            if (createdNewFile && getContentOf(lockFile).isEmpty()) {
                String token = generateRandomString();
                writeToFile(lockFile, token);
                System.out.println("My token is: " + token);
                return ifFirstInFile(lockFile, token, lock);
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean ifFirstInFile(File file, String token, Lock lock) throws FileNotFoundException, InterruptedException {
        ArrayList<String> lines = getContentOf(file);

        if(!lines.isEmpty() && lines.get(0).length() != 64) {
            System.out.println("Deleting " + lock.name() + " lock file (borked) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            file.delete(); // the lock file is borked, we're deleting it and someone else can try for the lock
            return false;
        }

        if(!lines.isEmpty() && lines.get(0).equals(token)) {
            System.out.println("My lock file on" + lock.name() + " lock file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return true;
        } else {
            System.out.println("Not my lock file on " + lock.name() + " lock file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return false;
        }
    }

    private void writeToFile(File file, String string) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.append(string + "\n");
        fw.close();
    }

    private ArrayList<String> getContentOf(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
        return lines;
    }

    private static String generateRandomString() {
        final byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);

        return Hex.encodeHexString(bytes)
                .substring(64)
                .toUpperCase();
    }

    public void releaseAndCloseFile(Lock lock) throws IOException, InterruptedException {
        File lockFile = new File(String.format("lock-%s.txt", lock));
        if(randomAccessFile != null) {
            randomAccessFile.getChannel().close();
        }
        Thread.sleep(10000);
        lockFile.delete();
        System.out.println("Released lock for " + lock.name() + " job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    protected List<T> readEntriesFromFile() throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        ObjectReader oReader = csvMapper.readerFor(type).with(schema);

        try {
            MappingIterator<T> mi = oReader.readValues(new FileInputStream(jobListFile));
            return Streams.stream(mi).collect(Collectors.toList());
        } catch (CsvMappingException | RuntimeJsonMappingException e) {
            return new ArrayList<>();
        }
    }

    public void writeEntriesToFile() throws IOException {
        backUpResultFile();

        randomAccessFile = new RandomAccessFile(jobListFile, "rw");
        randomAccessFile.getChannel().truncate(0);

        CsvSchema.Builder schemaBuilder = CsvSchema.builder();

        List<Field> declaredFields = new ArrayList<>();

        Class temp = type;

        while(!temp.isInstance(Object.class)) {
            declaredFields.addAll(Arrays.asList(temp.getDeclaredFields()));
            temp = temp.getSuperclass();
        }

        declaredFields
                .forEach(field -> schemaBuilder.addColumn(toKebabCase(field.getName())));
        CsvSchema schema = schemaBuilder.build().withHeader();

        ObjectWriter oWriter = csvMapper.writerFor(type).with(schema);
        oWriter.writeValues(randomAccessFile).writeAll(asArray());
    }

    private void backUpResultFile() throws IOException {
        String[] split = fileName.split("\\.");
        Files.copy(Paths.get(fileName), Paths.get(String.join(".", split[0] + "_backup_" + getHostname(), split[1])), StandardCopyOption.REPLACE_EXISTING);
    }

    public String getHostname() {
        try {
            return execCmd("hostname").trim();
        } catch (IOException e) {
            return "NA";
        }
    }

    private static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private Object[] asArray() {
        if(isEmpty()) {
            try {
                return Collections.singleton(type.getConstructor().newInstance()).toArray();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return new Object[0];
            }
        }
        return toArray();
    }

    private String toKebabCase(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, string);
    }
}
