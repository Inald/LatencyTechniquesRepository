package org.example;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileAnalyserMultiThreaded {

    public final String C_DEV_MSTR_CSV = "src/main/resources/MSTR.csv";
    private final ArrayList<String> mstrDataString = new ArrayList<>();
    private final ArrayList<byte[]> mstrDataBytes = new ArrayList<>();

    private ConcurrentHashMap<String, Integer> differentWordCountString = new ConcurrentHashMap<>();
    private ConcurrentHashMap<AsciiString, Integer> differentWordCountBytes = new ConcurrentHashMap<>();

    private final Benchmarking bm = new Benchmarking();
    private final Benchmarking bm2 = new Benchmarking();

    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public FileAnalyserMultiThreaded(int numberOfIterations) {

        for(int i = 0; i < numberOfIterations; i++) {
            System.out.println("getDataFromCSV");
            getDataFromCSV(bm);
            System.out.println("getDataFromCSVMemoryMapped3");
            getDataFromCSVMemoryMapped3(bm2);
        }
        printResults();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printResults() {
        System.out.println("----------------------------");
        getBm().printHistogramStats();
        System.out.println("different word count for String: " + differentWordCountString.keySet().size());
        System.out.println("----------------------------");
        getBm2().printHistogramStats();
        System.out.println("different word count for Bytes: " + differentWordCountBytes.keySet().size());
//        System.out.println("word with highest number: " + differentWordCountBytes);

    }

    public void numberOfEntriesString() {
        System.out.println("Number of Entries: " + mstrDataString.size());
        System.out.println("Time taken = " + bm.getDifference() + "ms");
    }

    public void numberOfEntriesBytes() {
        System.out.println("Number of Entries: " + mstrDataBytes.size());
        System.out.println("Time taken = " + bm2.getDifference() + "ms");
    }

    public void calculateNumberOfWords(ArrayList mstrData) {
        if(mstrData.get(0) instanceof String) {
            for (int i = 0; i < mstrData.size(); i++) {
                String[] entrySplit = ((String) mstrData.get(i)).split(",");
                for(int j = 0; j < entrySplit.length; j++) {
                    if(differentWordCountString.containsKey(entrySplit[j])) {
                        differentWordCountString.put(entrySplit[j], differentWordCountString.get(entrySplit[j]) + 1);
                    } else {
                        differentWordCountString.put(entrySplit[j], 1);
//                        appendToCSV("src/main/resources/string.csv", entrySplit[j]);
                    }
                }
            }
        } else {
            for (int i = 0; i < mstrData.size(); i++) {
                byte[] entrySplit = ((byte[]) mstrData.get(i));
                int startWordFlag = 0;
                int endWordFlag = 0;
                for (int j = 0; j < entrySplit.length; j++) {
                    if(entrySplit[j] == ',' || j == entrySplit.length - 1) {
                        int flagDifference = endWordFlag - startWordFlag;
                        byte[] wordData = new byte[flagDifference];
                        AsciiString word = new AsciiString(wordData);
                        System.arraycopy(entrySplit, startWordFlag, word.getBytes(), 0, flagDifference);
                        if(differentWordCountBytes.containsKey(word)) {
                            differentWordCountBytes.put(word, differentWordCountBytes.get(word) + 1);
                        } else {
                            differentWordCountBytes.put(word, 1);
//                            try {
//                                appendToCSV(word.getBytes(), "src/main/resources/bytes.csv");
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
                        }

                        startWordFlag = j + 1;
                        endWordFlag++;
                    } else {
                        endWordFlag++;
                    }
                }
            }
        }

    }


    public void getDataFromCSV(Benchmarking bm) {
        differentWordCountString.clear();
        boolean running = true;
        try {
            final BufferedReader bf = new BufferedReader(new FileReader(C_DEV_MSTR_CSV));

            bm.setStartTime(System.currentTimeMillis());
            while (running) {
                String nextLine = bf.readLine();
                if (nextLine == null) {
                    running = false;
                } else {
                    mstrDataString.add(nextLine);
                }
            }

            createStringWorkers();

            bm.setEndTime(System.currentTimeMillis());
            bm.addEntryToHistogram(bm.getDifference());
            numberOfEntriesString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void getDataFromCSVMemoryMapped3(Benchmarking bm2) {
        differentWordCountBytes.clear();
        try (FileChannel channel = FileChannel.open(Path.of(C_DEV_MSTR_CSV),
                StandardOpenOption.READ)) {

            MappedByteBuffer out = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, channel.size());

            bm2.setStartTime(System.currentTimeMillis());
            final byte[] byteToRead;
            byte[] byteEntry;

            final int size = (int) channel.size();
            byteToRead = new byte[size];
            out.get(byteToRead, 0, size);

            int startflag = 0;
            int newline = 0;
            for (int i = 0; i < size; i++) {
                newline++;
                if (byteToRead[i] == '\n' || i == size - 1) {
                    final int entrySize = newline - startflag;
                    byteEntry = new byte[entrySize];
                    System.arraycopy(byteToRead, startflag, byteEntry, 0, entrySize);
                    mstrDataBytes.add(byteEntry);
                    startflag = i;
                }
            }

            createByteWorkers();

            bm2.setEndTime(System.currentTimeMillis());
            bm2.addEntryToHistogram(bm2.getDifference());
            numberOfEntriesBytes();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createByteWorkers() {
        int firstQuarter = mstrDataBytes.size() / 4;
        int secondQuarter = mstrDataBytes.size() / 2;
        int thirdQuarter = firstQuarter + secondQuarter;

        ArrayList<byte[]> mstrCopy1 = new ArrayList<>();
        ArrayList<byte[]> mstrCopy2 = new ArrayList<>();
        ArrayList<byte[]> mstrCopy3 = new ArrayList<>();
        ArrayList<byte[]> mstrCopy4 = new ArrayList<>();

        for(int i = 0; i < mstrDataBytes.size(); i++) {
            if(i < firstQuarter) {
                mstrCopy1.add(mstrDataBytes.get(i));
            } else if (i < secondQuarter) {
                mstrCopy2.add(mstrDataBytes.get(i));
            } else if (i < thirdQuarter) {
                mstrCopy3.add(mstrDataBytes.get(i));
            } else {
                mstrCopy4.add(mstrDataBytes.get(i));
            }
        }

        Worker worker1 = new Worker(mstrCopy1, this);
        Worker worker2 = new Worker(mstrCopy2, this);
        Worker worker3 = new Worker(mstrCopy3, this);
        Worker worker4 = new Worker(mstrCopy4, this);

        threadPool.submit(worker1);
        threadPool.submit(worker2);
        threadPool.submit(worker3);
        threadPool.submit(worker4);

    }

    private void createStringWorkers() {
        int firstQuarter = mstrDataString.size() / 4;
        int secondQuarter = mstrDataString.size() / 2;
        int thirdQuarter = firstQuarter + secondQuarter;

        ArrayList<String> mstrCopy1 = new ArrayList<>();
        ArrayList<String> mstrCopy2 = new ArrayList<>();
        ArrayList<String> mstrCopy3 = new ArrayList<>();
        ArrayList<String> mstrCopy4 = new ArrayList<>();

        for(int i = 0; i < mstrDataString.size(); i++) {
            if(i < firstQuarter) {
                mstrCopy1.add(mstrDataString.get(i));
            } else if (i < secondQuarter) {
                mstrCopy2.add(mstrDataString.get(i));
            } else if (i < thirdQuarter) {
                mstrCopy3.add(mstrDataString.get(i));
            } else {
                mstrCopy4.add(mstrDataString.get(i));
            }
        }

        Worker worker1 = new Worker(mstrCopy1, this);
        Worker worker2 = new Worker(mstrCopy2, this);
        Worker worker3 = new Worker(mstrCopy3, this);
        Worker worker4 = new Worker(mstrCopy4, this);

        threadPool.submit(worker1);
        threadPool.submit(worker2);
        threadPool.submit(worker3);
        threadPool.submit(worker4);

    }

    public void appendToCSV(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            // Append a new line with the provided data to the CSV file
            writer.append(data);
            writer.append("\n"); // Add newline character to separate lines
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception as needed (e.g., logging, reporting, etc.)
        }
    }

    public void appendToCSV(byte[] data, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName, true)) {
            for (int i = 0; i < data.length; i++) {
                // Write the byte directly to the file
                fos.write(data[i]);
            }

            // Add a newline at the end (optional, depending on your CSV format)
            fos.write('\n');
        }
    }

    public Benchmarking getBm() {
        return bm;
    }

    public Benchmarking getBm2() {
        return bm2;
    }
}
