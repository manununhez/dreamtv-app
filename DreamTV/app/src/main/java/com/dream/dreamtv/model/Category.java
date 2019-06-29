package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("name")
    public String name;
    @SerializedName("language")
    public String language;
    @SerializedName("image_name")
    public String imageName;

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", language='" + language + '\'' +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}
