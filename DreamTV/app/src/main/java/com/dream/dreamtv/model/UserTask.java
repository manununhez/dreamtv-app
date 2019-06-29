package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private int userId;
    @SerializedName("task_id")
    private int taskId;
    @SerializedName("subtitle_version")
    private String subtitleVersion;
    @SerializedName("completed")
    private int completed;
    @SerializedName("rating")
    private int rating;
    @SerializedName("user_task_errors")
    private UserTaskError[] userTaskErrorList;
    @SerializedName("time_watched")
    private int timeWatched;
    @SerializedName("created_at")
    private String created_at;
    @SerializedName("updated_at")
    private String updated_at;

    public UserTask() {
    }

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

    public ArrayList<UserTaskError> getUserTaskErrorsForASpecificSubtitlePosition(int subtitlePosition) {
        ArrayList<UserTaskError> userTaskErrorList = new ArrayList<>();

        if (this.userTaskErrorList != null && this.userTaskErrorList.length > 0)
            for (UserTaskError userTaskError : this.userTaskErrorList) {
                if(userTaskError.getSubtitlePosition() > subtitlePosition)
                    break;
                else if (userTaskError.getSubtitlePosition() == subtitlePosition) {
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


    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getSubtitleVersion() {
        return subtitleVersion;
    }

    public void setSubtitleVersion(String subtitleVersion) {
        this.subtitleVersion = subtitleVersion;
    }

    public UserTaskError[] getUserTaskErrorList() {
        return userTaskErrorList;
    }

    public void setUserTaskErrorList(UserTaskError[] userTaskErrorList) {
        this.userTaskErrorList = userTaskErrorList;
    }

    public void addUserTaskErrorToList(UserTaskError[] newUserTaskErrors) {
        List<UserTaskError> userTaskErrors = new ArrayList<>(Arrays.asList(userTaskErrorList)); //mutable list
        List<UserTaskError> userTaskErrorsList = Arrays.asList(newUserTaskErrors);//inmutable list
        userTaskErrors.addAll(userTaskErrorsList);
        this.userTaskErrorList = userTaskErrors.toArray(this.userTaskErrorList);
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", subtitleVersion='" + subtitleVersion + '\'' +
                ", completed=" + completed +
                ", rating=" + rating +
                ", userTaskErrorList=" + Arrays.toString(userTaskErrorList) +
                ", timeWatched=" + timeWatched +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }
}
