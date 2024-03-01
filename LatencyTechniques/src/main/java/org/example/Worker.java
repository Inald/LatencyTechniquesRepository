package org.example;

import java.util.ArrayList;

public class Worker implements Runnable{

    private final ArrayList data;
    private final FileAnalyserMultiThreaded fileAnalyser;
    public Worker(ArrayList data, FileAnalyserMultiThreaded fileAnalyser) {
       this.data = data;
       this.fileAnalyser = fileAnalyser;
    }

    @Override
    public void run() {
        fileAnalyser.calculateNumberOfWords(data);
    }



}
