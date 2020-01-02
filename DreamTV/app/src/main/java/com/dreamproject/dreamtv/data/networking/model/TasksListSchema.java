package com.dreamproject.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 6/12/17.
 */

public class TasksListSchema {
    @SerializedName("tasks")
    public TaskSchema[] tasks;
    @SerializedName("category")
    public CategorySchema category;
}
