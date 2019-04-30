package com.dream.dreamtv.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.dream.dreamtv.model.Video;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Room;


/*
  Created by manuel on 21,August,2018
 */

/**
 * Defines the schema of a table in {@link Room} for a single weather
 * forecast.
 */
@Entity(tableName = "task_table")
public class TaskEntity implements Parcelable {
    /**
     * Makes sure the id is the primary key (ensures uniqueness), is auto generated by {@link Room}.
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int task_id;
    public String language;
    public String type;
    public String created;
    public String completed;
    public String modified;
    public String category;
    public long sync_time;//in secs //TODO delete if it is not used
    @Embedded
    public Video video;


    public TaskEntity(int task_id, String language, String type,
                      String created, String completed, String modified,
                      String category, long sync_time, Video video) {
        this.task_id = task_id;
        this.language = language;
        this.type = type;
        this.created = created;
        this.completed = completed;
        this.modified = modified;
        this.category = category;
        this.sync_time = sync_time;
        this.video = video;
    }


    protected TaskEntity(Parcel in) {
        id = in.readInt();
        task_id = in.readInt();
        language = in.readString();
        type = in.readString();
        created = in.readString();
        completed = in.readString();
        modified = in.readString();
        category = in.readString();
        sync_time = in.readLong();
        video = in.readParcelable(Video.class.getClassLoader());
    }

    public static final Creator<TaskEntity> CREATOR = new Creator<TaskEntity>() {
        @Override
        public TaskEntity createFromParcel(Parcel in) {
            return new TaskEntity(in);
        }

        @Override
        public TaskEntity[] newArray(int size) {
            return new TaskEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(task_id);
        dest.writeString(language);
        dest.writeString(type);
        dest.writeString(created);
        dest.writeString(completed);
        dest.writeString(modified);
        dest.writeString(category);
        dest.writeLong(sync_time);
        dest.writeParcelable(video, flags);
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "task_id=" + task_id +
                ", language='" + language + '\'' +
                ", type='" + type + '\'' +
                ", created='" + created + '\'' +
                ", completed='" + completed + '\'' +
                ", modified='" + modified + '\'' +
                ", category='" + category + '\'' +
                ", sync_time='" + sync_time + '\'' +
                ", video=" + video +
                '}';
    }
}
