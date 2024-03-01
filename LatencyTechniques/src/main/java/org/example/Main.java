package org.example;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) {

//        FileAnalyser fileAnalyser = new FileAnalyser(10);
        FileAnalyserMultiThreaded fileAnalyserMultiThreaded = new FileAnalyserMultiThreaded(1);
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
//        forkJoinPool.invoke(new CustomTask());
    }
}