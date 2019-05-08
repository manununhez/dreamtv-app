package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

public class UserTaskError {
    @SerializedName("id")
    public int id;
    @SerializedName("userTasksId")
    public int user_tasks_id;
    @SerializedName("reasonCode")
    public String reason_code;

}
