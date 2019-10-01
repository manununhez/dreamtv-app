package com.dream.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class VideoTestSchema {
    @SerializedName("id")
    public int id;
    @SerializedName("video_id")
    public String videoId;
    @SerializedName("subtitle_version")
    public int subtitleVersion;
    @SerializedName("subtitle_language")
    public String subLanguage;
}
