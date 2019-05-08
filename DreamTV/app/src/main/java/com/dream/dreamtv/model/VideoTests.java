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
    @SerializedName("version")
    public int version;
    @SerializedName("language_code")
    public String languageCode;

}
