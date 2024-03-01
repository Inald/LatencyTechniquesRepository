package org.example;

import org.HdrHistogram.Histogram;

public class Benchmarking {

    private long startTime;
    private long endTime;

    private Histogram hdrHistrogram;

    public Benchmarking() {
        hdrHistrogram = new Histogram(5);
    }

    public void addEntryToHistogram(long value) {
        hdrHistrogram.recordValue(value);
    }

    public void printHistogramStats() {
        System.out.println("50th percentile: " + hdrHistrogram.getValueAtPercentile(50));
        System.out.println("90th Percentile: "+ hdrHistrogram.getValueAtPercentile(90));
        System.out.println("99th percentile: " + hdrHistrogram.getValueAtPercentile(99));
        System.out.println("99.99th percentile: " + hdrHistrogram.getValueAtPercentile(99.99));
        System.out.println("Mean: " + hdrHistrogram.getMean());
    }

    public long getDifference() {
        return endTime - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
