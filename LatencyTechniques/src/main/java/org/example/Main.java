package org.example;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Main {

    public static final String C_DEV_MSTR_CSV = "c:\\quant\\historicalStockPrices\\historical_MSTR.csv";
    private static final ArrayList<String> mstrDataString = new ArrayList<>();
    private static final ArrayList<byte[]> mstrDataBytes = new ArrayList<>();


    private static final Benchmarking bm = new Benchmarking();

    public static void main(String[] args) {

        System.out.println("getDataFromCSV");
        getDataFromCSV();
        System.out.println("getDataFromCSVMemoryMapped");
        getDataFromCSVMemoryMapped();
        System.out.println("getDataFromCSVMemoryMapped2");
        getDataFromCSVMemoryMapped2();
        System.out.println("getDataFromCSVMemoryMapped3");
        getDataFromCSVMemoryMapped3();

    }

    public static void numberOfEntriesString() {
        System.out.println("Number of Entries: " + mstrDataString.size());
        System.out.println("Time taken = " + bm.getDifference() + "ms");
    }
    public static void numberOfEntriesBytes() {
        System.out.println("Number of Entries: " + mstrDataBytes.size());
        System.out.println("Time taken = " + bm.getDifference() + "ms");
    }

    public static void getDataFromCSV() {
        boolean running = true;
        try {
            final BufferedReader bf = new BufferedReader(new FileReader(C_DEV_MSTR_CSV));

            bm.setStartTime(System.currentTimeMillis());
            while(running) {
                String nextLine = bf.readLine();
                if(nextLine == null) {
                    running = false;
                } else {
                    mstrDataString.add(nextLine);
                }
            }
            bm.setEndTime(System.currentTimeMillis());
            numberOfEntriesString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getDataFromCSVMemoryMapped2() {
        boolean running = true;
        try (FileChannel channel = FileChannel.open(Path.of(C_DEV_MSTR_CSV),
                StandardOpenOption.READ))  {

            MappedByteBuffer out = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size());

            StringBuilder stringBuilder = new StringBuilder();
            bm.setStartTime(System.currentTimeMillis());

            for (int i = 0; i < channel.size(); i++) {
                char c = (char)out.get(i);
                stringBuilder.append(c);
                if(c == '\n'){
                    mstrDataString.add(stringBuilder.toString());
                    stringBuilder.replace(0, stringBuilder.length(), "");
                }
            }
            bm.setEndTime(System.currentTimeMillis());

            numberOfEntriesString();
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
                    mstrDataBytes.add(nextline);
                    bsb.reset();
                }
            }
            bm.setEndTime(System.currentTimeMillis());
            numberOfEntriesBytes();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getDataFromCSVMemoryMapped3() {
        try (FileChannel channel = FileChannel.open(Path.of(C_DEV_MSTR_CSV),
                StandardOpenOption.READ))  {

            MappedByteBuffer out = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size());

            bm.setStartTime(System.currentTimeMillis());
            final byte[] byteToRead;
            byte[] byteEntry;

            final int size = (int) channel.size();
            byteToRead = new byte[size];
            out.get(0, byteToRead, 0, size);

            int startflag = 0;
            int newline = 0;
            for (int i = 0; i < size; i++) {
                newline++;
                if(byteToRead[i] == '\n'){
                    final int entrySize = newline - startflag;
                    byteEntry = new byte[entrySize];
                    System.arraycopy(byteToRead, startflag, byteEntry, 0, entrySize);
                    mstrDataBytes.add(byteEntry);
                    startflag = i;
                }
            }
            bm.setEndTime(System.currentTimeMillis());
            numberOfEntriesBytes();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}