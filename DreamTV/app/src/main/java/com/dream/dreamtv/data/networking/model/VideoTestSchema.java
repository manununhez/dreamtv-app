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
    @SerializedName("sub_version")
    public int subVersion;
    @SerializedName("subtitle_language")
    public String subLanguage;
}
