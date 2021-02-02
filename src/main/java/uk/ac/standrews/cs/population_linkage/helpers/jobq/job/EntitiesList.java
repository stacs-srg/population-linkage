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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EntitiesList<T> extends ArrayList<T> {

    private final RandomAccessFile randomAccessFile;
    private FileInputStream fileInputStream;
    private final CsvMapper csvMapper = new CsvMapper();
    private final Class<T> type;
    private final String fileName;

    private Random rand = new Random();

    public EntitiesList(Class<T> type, String jobListFile, Lock lock) throws IOException, InterruptedException {
        this.type = type;
        this.fileName = jobListFile;
        randomAccessFile = new RandomAccessFile(jobListFile, "rw");
        fileInputStream = new FileInputStream(jobListFile);
        lockFile(lock);
        addAll(readEntriesFromFile());
    }

    // for testing only
    protected EntitiesList(Class<T> type) {
        this.randomAccessFile = null;
        this.fileInputStream = null;
        this.type = type;
        this.fileName = null;
    }

    public enum Lock {
        RESULTS,
        JOBS
    }

    protected void lockFile(Lock lock) throws IOException, InterruptedException {
        System.out.println("Locking " + lock.name() + " job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        randomAccessFile.getChannel().lock(0, Long.MAX_VALUE, false);

        Thread.sleep(rand.nextInt(10000));

        File lockFile = new File(String.format("lock-%s.txt", lock));
        do {
            while (lockFile.isFile()) {
                Thread.sleep(10000 + rand.nextInt(10000));
            }
        } while(cantAquireLock(lockFile));
        System.out.println("Locked " + lock.name() + " job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private boolean cantAquireLock(File lockFile) throws InterruptedException, IOException {
        Thread.sleep(rand.nextInt(1000));
        return !lockFile.createNewFile();
    }

    public void releaseAndCloseFile(Lock lock) throws IOException {
        File lockFile = new File(String.format("lock-%s.txt", lock));
        randomAccessFile.getChannel().close();
        lockFile.delete();
        System.out.println("Released lock for " + lock.name() + " job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    protected List<T> readEntriesFromFile() throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        ObjectReader oReader = csvMapper.readerFor(type).with(schema);

        try {
            MappingIterator<T> mi = oReader.readValues(fileInputStream);
            return Streams.stream(mi).collect(Collectors.toList());
        } catch (CsvMappingException | RuntimeJsonMappingException e) {
            return new ArrayList<>();
        }
    }

    public void writeEntriesToFile() throws IOException {
        backUpResultFile();
        
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
