package com.dream.dreamtv.data.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class VideoTest {
    @SerializedName("id")
    public int id;
    @SerializedName("video_id")
    public String videoId;
    @SerializedName("subtitle_version")
    public int subtitleVersion;
    @SerializedName("subtitle_language_code")
    public String subtitleLanguageCode;

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
