package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

public class UserTaskError implements Parcelable {
    @SerializedName("user_tasks_id")
    private int userTasksId;
    @SerializedName("reason_code")
    private String reasonCode;
    @SerializedName("subtitle_position")
    private int subtitlePosition;
    @SerializedName("comment")
    private String comment;
    @Ignore
    private int subtitleVersion;
    @Ignore
    private int taskId;

    public UserTaskError(Parcel in) {
        userTasksId = in.readInt();
        reasonCode = in.readString();
        subtitlePosition = in.readInt();
        comment = in.readString();
    }

    public static final Creator<UserTaskError> CREATOR = new Creator<UserTaskError>() {
        @Override
        public UserTaskError createFromParcel(Parcel in) {
            return new UserTaskError(in);
        }

        @Override
        public UserTaskError[] newArray(int size) {
            return new UserTaskError[size];
        }
    };

    public UserTaskError(int taskId, String reasonCode, int subtitleVersion, int subtitlePosition, String comment) {
        this.taskId = taskId;
        this.reasonCode = reasonCode;
        this.subtitleVersion = subtitleVersion;
        this.subtitlePosition = subtitlePosition;
        this.comment = comment;

    }

    public int getUserTasksId() {
        return userTasksId;
    }

    public void setUserTasksId(int userTasksId) {
        this.userTasksId = userTasksId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public int getSubtitlePosition() {
        return subtitlePosition;
    }

    public void setSubtitlePosition(int subtitlePosition) {
        this.subtitlePosition = subtitlePosition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getSubtitleVersion() {
        return subtitleVersion;
    }

    public void setSubtitleVersion(int subtitleVersion) {
        this.subtitleVersion = subtitleVersion;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userTasksId);
        dest.writeString(reasonCode);
        dest.writeInt(subtitlePosition);
        dest.writeString(comment);
    }

    @Override
    public String toString() {
        return "UserTaskError{" +
                "userTasksId=" + userTasksId +
                ", reasonCode='" + reasonCode + '\'' +
                ", subtitlePosition=" + subtitlePosition +
                ", comment='" + comment + '\'' +
                '}';
    }
}
