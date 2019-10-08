package com.manuelnunhez.dreamtv.data.model;

public class VideoDuration {
    private int minDuration;
    private int maxDuration;

    public VideoDuration(int minDuration, int maxDuration) {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }
}
