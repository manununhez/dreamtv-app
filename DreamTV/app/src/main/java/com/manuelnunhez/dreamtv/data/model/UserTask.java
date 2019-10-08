package com.manuelnunhez.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

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

    public int id;
    private int userId;
    private int taskId;
    private int completed;
    private int rating;
    private int timeWatched;
    private String subVersion;
    private UserTaskError[] userTaskErrorList;


    public UserTask(int id, int userId, int taskId, int completed, int rating, int timeWatched,
                    String subVersion, UserTaskError[] userTaskErrorList) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.completed = completed;
        this.rating = rating;
        this.timeWatched = timeWatched;
        this.subVersion = subVersion;
        this.userTaskErrorList = userTaskErrorList;
    }

    protected UserTask(Parcel in) {
        id = in.readInt();
        userId = in.readInt();
        taskId = in.readInt();
        subVersion = in.readString();
        timeWatched = in.readInt();
        completed = in.readInt();
        rating = in.readInt();
        userTaskErrorList = in.createTypedArray(UserTaskError.CREATOR);
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
                if (userTaskError.getSubtitlePosition() > subtitlePosition)
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
        dest.writeString(subVersion);
        dest.writeInt(timeWatched);
        dest.writeInt(completed);
        dest.writeInt(rating);
        dest.writeTypedArray(userTaskErrorList, flags);
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

    public String getSubVersion() {
        return subVersion;
    }

    public void setSubVersion(String subVersion) {
        this.subVersion = subVersion;
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


    @Override
    public String toString() {
        return "UserTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", subVersion='" + subVersion + '\'' +
                ", completed=" + completed +
                ", rating=" + rating +
                ", userTaskErrorList=" + Arrays.toString(userTaskErrorList) +
                ", timeWatched=" + timeWatched +
                '}';
    }
}
