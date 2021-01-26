/*
 * ************************************************************************
 *
 * Copyright 2021 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 * ************************************************************************
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntitiesList<T> extends ArrayList<T> {

    private final RandomAccessFile randomAccessFile;
    private final FileInputStream fileInputStream;
    private final CsvMapper csvMapper = new CsvMapper();
    private final Class<T> type;
    private final String fileName;

    public EntitiesList(Class<T> type, String jobListFile) throws IOException {
        this.type = type;
        this.fileName = jobListFile;
        randomAccessFile = new RandomAccessFile(jobListFile, "rw");
        System.out.println("Locking job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        lockFile();
        System.out.println("Locked job file @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        fileInputStream = new FileInputStream(jobListFile);
        addAll(readEntriesFromFile());
    }

    // for testing only
    protected EntitiesList(Class<T> type) {
        this.randomAccessFile = null;
        this.fileInputStream = null;
        this.type = type;
        this.fileName = null;
    }

    private void lockFile() throws IOException {
        randomAccessFile.getChannel().lock(0, Long.MAX_VALUE, false);
    }

    public void releaseAndCloseFile() throws IOException {
        randomAccessFile.getChannel().close();
    }

    private List<T> readEntriesFromFile() throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        ObjectReader oReader = csvMapper.readerFor(type).with(schema);

        try {
            MappingIterator<T> mi = oReader.readValues(fileInputStream);
            return Streams.stream(mi).collect(Collectors.toList());
        } catch (CsvMappingException | RuntimeJsonMappingException e) {
            return List.of();
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
        Files.copy(Path.of(fileName), Path.of(String.join(".", split[0] + "_backup", split[1])), StandardCopyOption.REPLACE_EXISTING);
    }

    private Object[] asArray() {
        if(isEmpty()) {
            try {
                return Collections.singleton(type.getConstructor().newInstance()).toArray();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return new Object[0];
            }
        }
        return this.toArray();
    }

    private String toKebabCase(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, string);
    }
}
