package com.dreamproject.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

public class CategorySchema {
    @SerializedName("order_index")
    public int orderIndex;
    @SerializedName("name")
    public String name;
    @SerializedName("visible")
    public boolean visible;
}
