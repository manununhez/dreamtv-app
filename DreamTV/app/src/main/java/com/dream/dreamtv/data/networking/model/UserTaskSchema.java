package com.dream.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/20/17.
 */

public class UserTaskSchema {
    @SerializedName("id")
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("completed")
    public int completed;
    @SerializedName("rating")
    public int rating;
    @SerializedName("time_watched")
    public int timeWatched;
    @SerializedName("sub_version")
    public String subVersion;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;
    @SerializedName("user_task_errors")
    public UserTaskErrorSchema[] userTaskErrorList;
}
