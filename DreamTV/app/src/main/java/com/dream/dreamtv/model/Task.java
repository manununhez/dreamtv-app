package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by manuel on 7/8/17.
 */

public class Task implements Parcelable {
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("video_id")
    public String videoId;
    @SerializedName("language")
    public String subLanguage;
    @SerializedName("type")
    public String type;
    @SerializedName("created")
    public String created;
    @SerializedName("modified")
    public String modified;
    @SerializedName("completed")
    public String completed;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;
    @SerializedName("videos")
    public Video video;
    @SerializedName("user_tasks")
    public UserTask[] userTasks;


    protected Task(Parcel in) {
        taskId = in.readInt();
        videoId = in.readString();
        subLanguage = in.readString();
        type = in.readString();
        created = in.readString();
        modified = in.readString();
        completed = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
        userTasks = in.createTypedArray(UserTask.CREATOR);
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskId);
        dest.writeString(videoId);
        dest.writeString(subLanguage);
        dest.writeString(type);
        dest.writeString(created);
        dest.writeString(modified);
        dest.writeString(completed);
        dest.writeString(created_at);
        dest.writeString(updated_at);
        dest.writeParcelable(video, flags);
        dest.writeTypedArray(userTasks, flags);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", videoId='" + videoId + '\'' +
                ", subLanguage='" + subLanguage + '\'' +
                ", type='" + type + '\'' +
                ", created='" + created + '\'' +
                ", modified='" + modified + '\'' +
                ", completed='" + completed + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", video=" + video +
                ", userTasks=" + Arrays.toString(userTasks) +
                '}';
    }
}
