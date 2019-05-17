package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UserTaskError implements Parcelable {
    @SerializedName("user_tasks_id")
    public int userTasksId;
    @SerializedName("reason_code")
    public String reasonCode;
    @SerializedName("subtitle_position")
    public int subtitlePosition;
    @SerializedName("comment")
    public String comment;

    protected UserTaskError(Parcel in) {
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
