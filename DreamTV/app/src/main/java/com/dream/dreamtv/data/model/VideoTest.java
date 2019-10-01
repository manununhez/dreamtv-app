package com.dream.dreamtv.data.model;

/**
 * Created by manuel on 7/18/17.
 */

public class VideoTest {
    public int id;
    public String videoId;
    public int subVersion;
    public String subLanguage;

    public VideoTest(int id, String videoId, int subVersion, String subLanguage) {
        this.id = id;
        this.videoId = videoId;
        this.subVersion = subVersion;
        this.subLanguage = subLanguage;
    }

    @Override
    public String toString() {
        return "VideoTest{" +
                "id=" + id +
                ", videoId='" + videoId + '\'' +
                ", subVersion=" + subVersion +
                ", languageCode='" + subLanguage + '\'' +
                '}';
    }
}
