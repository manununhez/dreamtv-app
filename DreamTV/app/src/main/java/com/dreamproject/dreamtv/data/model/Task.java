package com.dreamproject.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by manuel on 7/8/17.
 */

public class Task implements Parcelable {
    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    private int taskId;
    private String videoId;
    private String videoTitleTranslated;
    private String videoDescriptionTranslated;
    private String subLanguage;
    private String type;
    private Video video;
    private UserTask[] userTasks;

    public Task(int taskId, String videoId, String videoTitleTranslated, String videoDescriptionTranslated,
                String subLanguage, String type, Video video, UserTask[] userTasks) {
        this.taskId = taskId;
        this.videoId = videoId;
        this.videoTitleTranslated = videoTitleTranslated;
        this.videoDescriptionTranslated = videoDescriptionTranslated;
        this.subLanguage = subLanguage;
        this.type = type;
        this.video = video;
        this.userTasks = userTasks;
    }

    protected Task(Parcel in) {
        taskId = in.readInt();
        videoId = in.readString();
        videoTitleTranslated = in.readString();
        videoDescriptionTranslated = in.readString();
        subLanguage = in.readString();
        type = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
        userTasks = in.createTypedArray(UserTask.CREATOR);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitleTranslated() {
        return videoTitleTranslated;
    }

    public void setVideoTitleTranslated(String videoTitleTranslated) {
        this.videoTitleTranslated = videoTitleTranslated;
    }

    public String getVideoDescriptionTranslated() {
        return videoDescriptionTranslated;
    }

    public void setVideoDescriptionTranslated(String videoDescriptionTranslated) {
        this.videoDescriptionTranslated = videoDescriptionTranslated;
    }

    public String getSubLanguage() {
        return subLanguage;
    }

    public void setSubLanguage(String subLanguage) {
        this.subLanguage = subLanguage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public UserTask[] getUserTasks() {
        return userTasks;
    }

    public void setUserTasks(UserTask[] userTasks) {
        this.userTasks = userTasks;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskId);
        dest.writeString(videoId);
        dest.writeString(videoTitleTranslated);
        dest.writeString(videoDescriptionTranslated);
        dest.writeString(subLanguage);
        dest.writeString(type);
        dest.writeParcelable(video, flags);
        dest.writeTypedArray(userTasks, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", videoId='" + videoId + '\'' +
                ", videoTitleTranslated='" + videoTitleTranslated + '\'' +
                ", videoDescriptionTranslated='" + videoDescriptionTranslated + '\'' +
                ", subLanguage='" + subLanguage + '\'' +
                ", type='" + type + '\'' +
                ", video=" + video +
                ", userTasks=" + Arrays.toString(userTasks) +
                '}';
    }
}
