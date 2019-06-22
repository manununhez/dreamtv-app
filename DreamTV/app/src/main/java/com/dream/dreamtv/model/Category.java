package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    public String name;
    public String language;
    @SerializedName("image_name")
    public String imageName;
}
