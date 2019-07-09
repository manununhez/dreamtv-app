package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class VideoTests  {
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
        return "VideoTests{" +
                "id=" + id +
                ", videoId='" + videoId + '\'' +
                ", subtitleVersion=" + subtitleVersion +
                ", languageCode='" + subtitleLanguageCode + '\'' +
                '}';
    }
}
