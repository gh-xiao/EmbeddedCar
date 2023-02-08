package com.xiao.embeddedcar.Utils.TrafficSigns;

public class TSResult {

    private final String title;
    private final Float confidence;

    public TSResult(String title, Float confidence) {
        this.title = title;
        this.confidence = confidence;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "TSResult{" +
                "title='" + title + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
