package org.example;

public class Benchmarking {

    private long startTime;
    private long endTime;
    public Benchmarking() {

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
