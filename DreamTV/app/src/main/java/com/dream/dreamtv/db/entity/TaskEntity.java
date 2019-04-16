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
    @PrimaryKey
    public int task_id;
    public String language;
    public String type;
    public String created;
    public String completed;
    public String modified;
    public String category;
    @Embedded
    public Video video;


    public TaskEntity(int task_id,
                      String language, String type, String created,
                      String completed, String modified, Video video, String category) {
        this.task_id = task_id;
        this.language = language;
        this.type = type;
        this.created = created;
        this.completed = completed;
        this.modified = modified;
        this.category = category;
        this.video = video;
    }


    protected TaskEntity(Parcel in) {
        task_id = in.readInt();
        language = in.readString();
        type = in.readString();
        created = in.readString();
        completed = in.readString();
        modified = in.readString();
        category = in.readString();
        video = in.readParcelable(Video.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(task_id);
        dest.writeString(language);
        dest.writeString(type);
        dest.writeString(created);
        dest.writeString(completed);
        dest.writeString(modified);
        dest.writeString(category);
        dest.writeParcelable(video, flags);
    }

    @Override
    public int describeContents() {
        return 0;
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
    public String toString() {
        return "TaskEntity{" +
                ", task_id=" + task_id +
                ", language='" + language + '\'' +
                ", type='" + type + '\'' +
                ", created='" + created + '\'' +
                ", completed='" + completed + '\'' +
                ", modified='" + modified + '\'' +
                ", category='" + category + '\'' +
                ", video=" + video +
                '}';
    }


}
