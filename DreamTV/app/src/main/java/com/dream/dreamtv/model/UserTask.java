package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by manuel on 7/20/17.
 */

public class UserTask implements Parcelable {
    @SerializedName("id")
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("subtitle_position")
    public int subtitlePosition;
    @SerializedName("subtitle_version")
    public String subtitleVersion;
    @SerializedName("comments")
    public String comments;
    @SerializedName("time_watched")
    public int timeWatched;
    @SerializedName("completed")
    public boolean completed;
    @SerializedName("rating")
    public int rating;
    @SerializedName("user_task_errors")
    public List<UserTaskError> userTaskErrorList;


    protected UserTask(Parcel in) {
        id = in.readInt();
        userId = in.readInt();
        taskId = in.readInt();
        subtitlePosition = in.readInt();
        subtitleVersion = in.readString();
        comments = in.readString();
        timeWatched = in.readInt();
        completed = in.readByte() != 0;
        rating = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(userId);
        dest.writeInt(taskId);
        dest.writeInt(subtitlePosition);
        dest.writeString(subtitleVersion);
        dest.writeString(comments);
        dest.writeInt(timeWatched);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserTask> CREATOR = new Creator<UserTask>() {
        @Override
        public UserTask createFromParcel(Parcel in) {
            return new UserTask(in);
        }

        @Override
        public UserTask[] newArray(int size) {
            return new UserTask[size];
        }
    };
}
