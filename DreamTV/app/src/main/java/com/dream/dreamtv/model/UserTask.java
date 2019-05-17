package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by manuel on 7/20/17.
 */

public class UserTask implements Parcelable {
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

    private static final int ONE_SEC_IN_MS = 1000;
    private static final int SECS_IN_ONE_MIN = 60;

    @SerializedName("id")
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("subtitle_version")
    public String subtitleVersion;
    @SerializedName("completed")
    public int completed;
    @SerializedName("rating")
    public int rating;
    @SerializedName("user_task_errors")
    public UserTaskError[] userTaskErrorList;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;
    @SerializedName("time_watched")
    private int timeWatched;

    protected UserTask(Parcel in) {
        id = in.readInt();
        userId = in.readInt();
        taskId = in.readInt();
        subtitleVersion = in.readString();
        timeWatched = in.readInt();
        completed = in.readInt();
        rating = in.readInt();
        userTaskErrorList = in.createTypedArray(UserTaskError.CREATOR);
        created_at = in.readString();
        updated_at = in.readString();
    }

    public int getTimeWatchedInSecs() {
        return timeWatched / ONE_SEC_IN_MS;
    }

    public int getTimeWatchedInMins() {
        return timeWatched / ONE_SEC_IN_MS / SECS_IN_ONE_MIN;
    }

    public int getTimeWatched() {
        return timeWatched;
    }

    public void setTimeWatched(int timeWatched) {
        this.timeWatched = timeWatched;
    }

    public ArrayList<UserTaskError> getUserTaskErrorsForASpecificSubtitlePosition(Subtitle subtitle) {
        ArrayList<UserTaskError> userTaskErrorList = new ArrayList<>();

        if (this.userTaskErrorList != null && this.userTaskErrorList.length > 0)
            for (UserTaskError userTaskError : this.userTaskErrorList) {
                //TODO si los errores vienen ordenados por subtitle_position (en forma ascendente), se puede mejorar la busqueda.
                if (userTaskError.subtitlePosition == subtitle.position) {
                    userTaskErrorList.add(userTaskError);
                }
            }


        return userTaskErrorList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(userId);
        dest.writeInt(taskId);
        dest.writeString(subtitleVersion);
        dest.writeInt(timeWatched);
        dest.writeInt(completed);
        dest.writeInt(rating);
        dest.writeTypedArray(userTaskErrorList, flags);
        dest.writeString(created_at);
        dest.writeString(updated_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", subtitleVersion='" + subtitleVersion + '\'' +
                ", timeWatched=" + timeWatched +
                ", completed=" + completed +
                ", rating=" + rating +
                ", userTaskErrorList=" + Arrays.toString(userTaskErrorList) +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }
}
