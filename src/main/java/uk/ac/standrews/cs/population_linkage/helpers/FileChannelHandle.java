/*
 * Copyright 2020 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 */
package uk.ac.standrews.cs.population_linkage.helpers;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

public class FileChannelHandle {

    private FileChannel fc;
    public static HashSet<StandardOpenOption> optionsRW = Sets.newHashSet(StandardOpenOption.READ, StandardOpenOption.WRITE);
    public static HashSet<StandardOpenOption> optionsWA = Sets.newHashSet(StandardOpenOption.WRITE, StandardOpenOption.APPEND);


    public FileChannelHandle(Path file) throws IOException {
        fc = getFileChannel(file, optionsRW);
    }

    public FileChannelHandle(Path file, HashSet<StandardOpenOption> openOptions) throws IOException {
        fc = getFileChannel(file, openOptions);
    }

    public void lock() throws IOException {
        System.out.println("Locking job file (R) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        fc.lock(0, Long.MAX_VALUE, false);
        System.out.println("Locked job file (R) @ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public void releaseAndClose() throws IOException {
        fc.close();
    }

    private static FileChannel getFileChannel(Path file, HashSet<StandardOpenOption> openOptions) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<>(openOptions);
        return FileChannel.open(file, options);
    }

    public void overwriteFile(FileChannel jobFile, String toFileString) throws IOException {

        fc.lock(0, Long.MAX_VALUE, false);

        jobFile.truncate(0);

        ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
        buf.clear();
        buf.put(toFileString.getBytes());

        buf.flip();

        while(buf.hasRemaining()) {
            jobFile.write(buf);
        }

        releaseAndClose();

    }

    public void appendToFile(String toFileString) throws IOException {

        fc.lock(0, Long.MAX_VALUE, false);


        ByteBuffer buf = ByteBuffer.allocate(toFileString.getBytes().length + 1000);
        buf.clear();
        buf.put(toFileString.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            fc.write(buf);
        }

        releaseAndClose();
    }

}
