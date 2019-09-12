package com.dream.dreamtv.data.networking.model;

import android.os.Parcel;
import android.os.Parcelable;

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

    public UserTaskError(String reasonCode, int subtitlePosition, String comment) {
        this.reasonCode = reasonCode;
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
