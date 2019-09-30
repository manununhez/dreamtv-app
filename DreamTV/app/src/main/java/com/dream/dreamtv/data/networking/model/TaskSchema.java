package com.dream.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/8/17.
 */

public class TaskSchema {
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("video_id")
    public String videoId;
    @SerializedName("video_title")
    public String videoTitleTranslated;
    @SerializedName("video_description")
    public String videoDescriptionTranslated;
    @SerializedName("language")
    public String subLanguage;
    @SerializedName("type")
    public String type;
    @SerializedName("created")
    public String created;
    @SerializedName("modified")
    public String modified;
    @SerializedName("completed")
    public String completed;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;
    @SerializedName("videos")
    public VideoSchema video;
    @SerializedName("user_tasks")
    public UserTaskSchema[] userTasks;

}
