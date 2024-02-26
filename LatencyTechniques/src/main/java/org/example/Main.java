package org.example;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static final String C_DEV_MSTR_CSV = "c:/dev/MSTR.csv";
    private static final ArrayList<byte[]> mstrData = new ArrayList<>();

    private static final Benchmarking bm = new Benchmarking();

    public static void main(String[] args) {

//        bm.setStartTime(System.currentTimeMillis());
//        getDataFromCSV();
        getDataFromCSVMemoryMapped();
        numberOfEntries();
//        bm.setEndTime(System.currentTimeMillis());

        System.out.println("Time taken = " + bm.getDifference() + "ms");
    }

    public static void numberOfEntries() {
        System.out.println("Number of Entries: " + mstrData.size());
    }

    public static void getDataFromCSV() {
        boolean running = true;
        try {
            final BufferedReader bf = new BufferedReader(new FileReader(C_DEV_MSTR_CSV));

            while(running) {
                String nextLine = bf.readLine();
                if(nextLine == null) {
                    running = false;
                } else {
//                    mstrData.add(nextLine);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void getDataFromCSVMemoryMapped() {
        boolean running = true;
        try (FileChannel channel = FileChannel.open(Path.of(C_DEV_MSTR_CSV),
                StandardOpenOption.READ))  {

            MappedByteBuffer out = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size());

            ByteStringBuilder bsb = new ByteStringBuilder();

            bm.setStartTime(System.currentTimeMillis());
            for (int i = 0; i < channel.size(); i++) {
                byte b = out.get(i);
                bsb.appendByte(b);
                if(b == '\n'){
                    byte[] nextline = bsb.getTrimmedNextLineArray();
//                    String line = new String(nextline);
                    mstrData.add(nextline);
                    bsb.reset();
                }
            }
            bm.setEndTime(System.currentTimeMillis());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}