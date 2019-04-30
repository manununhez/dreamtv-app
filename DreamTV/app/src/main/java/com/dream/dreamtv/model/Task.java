package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.utils.Utils;

import java.time.Instant;

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
    public int task_id;
    public String video_id;
    public String language;
    public String type;
    public String created;
    public String modified;
    public String completed;
    public String created_at;
    public String updated_at;
    public Video videos;


    protected Task(Parcel in) {
        task_id = in.readInt();
        video_id = in.readString();
        language = in.readString();
        type = in.readString();
        created = in.readString();
        modified = in.readString();
        completed = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        videos = in.readParcelable(Video.class.getClassLoader());
    }

    public TaskEntity getEntity(String category) {
        return new TaskEntity(task_id, language,
                type, created, completed, modified, category, Utils.getUnixTimeNowInSecs(), new Video(videos.video_id, videos.primary_audio_language_code,
                videos.title, videos.description, videos.duration, videos.thumbnail, videos.team,
                videos.project, videos.video_url));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(task_id);
        parcel.writeString(video_id);
        parcel.writeString(language);
        parcel.writeString(type);
        parcel.writeString(created);
        parcel.writeString(modified);
        parcel.writeString(completed);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeParcelable(videos, i);
    }
}
