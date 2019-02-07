package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 7/8/17.
 */

public class Task implements Parcelable {
    public int id;
    public int task_id;
    public String video_id;
    public String language;
    public String type;
    public int priority;
    public String created;
    public String modified;
    public String completed;
    public String created_at;
    public String updated_at;
    public String primary_audio_language_code;
    public String original_language;
    public String title;
    public String description;
    public int duration;
    public String thumbnail;
    public String team;
    public String project;
    public String video_url;

    protected Task(Parcel in) {
        id = in.readInt();
        task_id = in.readInt();
        video_id = in.readString();
        language = in.readString();
        type = in.readString();
        priority = in.readInt();
        created = in.readString();
        modified = in.readString();
        completed = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        primary_audio_language_code = in.readString();
        original_language = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readInt();
        thumbnail = in.readString();
        team = in.readString();
        project = in.readString();
        video_url = in.readString();
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(task_id);
        parcel.writeString(video_id);
        parcel.writeString(language);
        parcel.writeString(type);
        parcel.writeInt(priority);
        parcel.writeString(created);
        parcel.writeString(modified);
        parcel.writeString(completed);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeString(primary_audio_language_code);
        parcel.writeString(original_language);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeInt(duration);
        parcel.writeString(thumbnail);
        parcel.writeString(team);
        parcel.writeString(project);
        parcel.writeString(video_url);
    }
}
