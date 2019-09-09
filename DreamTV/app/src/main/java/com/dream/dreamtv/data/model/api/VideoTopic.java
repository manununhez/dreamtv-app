package com.dream.dreamtv.data.model.api;

import com.google.gson.annotations.SerializedName;

public class VideoTopic {
    @SerializedName("name")
    public String name;
    @SerializedName("language")
    public String language;
    @SerializedName("image_name")
    public String imageName;

    @Override
    public String toString() {
        return "VideoTopic{" +
                "name='" + name + '\'' +
                ", language='" + language + '\'' +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}
