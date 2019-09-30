package com.dream.dreamtv.data.model;

/**
 * Created by manuel on 7/18/17.
 */

public class VideoTest {
    public int id;
    public String videoId;
    public int subtitleVersion;
    public String subtitleLanguageCode;

    public VideoTest(int id, String videoId, int subtitleVersion, String subtitleLanguageCode) {
        this.id = id;
        this.videoId = videoId;
        this.subtitleVersion = subtitleVersion;
        this.subtitleLanguageCode = subtitleLanguageCode;
    }

    @Override
    public String toString() {
        return "VideoTest{" +
                "id=" + id +
                ", videoId='" + videoId + '\'' +
                ", subtitleVersion=" + subtitleVersion +
                ", languageCode='" + subtitleLanguageCode + '\'' +
                '}';
    }
}
