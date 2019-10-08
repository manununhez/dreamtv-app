package com.manuelnunhez.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

public class UserTaskErrorSchema {
    @SerializedName("user_tasks_id")
    public int userTasksId;
    @SerializedName("reason_code")
    public String reasonCode;
    @SerializedName("subtitle_position")
    public int subtitlePosition;
    @SerializedName("comment")
    public String comment;

    public UserTaskErrorSchema(int userTasksId, String reasonCode, int subtitlePosition, String comment) {
        this.userTasksId = userTasksId;
        this.reasonCode = reasonCode;
        this.subtitlePosition = subtitlePosition;
        this.comment = comment;
    }
}
