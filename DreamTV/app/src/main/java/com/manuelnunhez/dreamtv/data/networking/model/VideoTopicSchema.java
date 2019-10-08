package com.manuelnunhez.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

public class VideoTopicSchema {
    @SerializedName("name")
    public String name;
    @SerializedName("language")
    public String language;
    @SerializedName("image_name")
    public String imageName;

}
