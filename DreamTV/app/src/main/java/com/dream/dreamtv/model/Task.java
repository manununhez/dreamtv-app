package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/8/17.
 */

public class Task implements Parcelable {
    @SerializedName("task_id")
    public int taskId;
    @SerializedName("video_id")
    public String videoId;
    @SerializedName("language")
    public String language;
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

    protected Task(Parcel in) {
        taskId = in.readInt();
        videoId = in.readString();
        language = in.readString();
        type = in.readString();
        created = in.readString();
        modified = in.readString();
        completed = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(taskId);
        parcel.writeString(videoId);
        parcel.writeString(language);
        parcel.writeString(type);
        parcel.writeString(created);
        parcel.writeString(modified);
        parcel.writeString(completed);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeParcelable(video, i);
    }
}
