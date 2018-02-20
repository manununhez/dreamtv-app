package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 7/20/17.
 */

public class UserTask extends JsonRequestBaseBean implements Parcelable {
    public int task_id;
    public int subtitle_position;
    public String subtitle_version;
    public String reason_id; //reasons id list
    public String reasonList;
    public String comments;

    public UserTask() {
    }

    protected UserTask(Parcel in) {
        task_id = in.readInt();
        subtitle_position = in.readInt();
        subtitle_version = in.readString();
        reason_id = in.readString();
        reasonList = in.readString();
        comments = in.readString();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(task_id);
        parcel.writeInt(subtitle_position);
        parcel.writeString(subtitle_version);
        parcel.writeString(reason_id);
        parcel.writeString(reasonList);
        parcel.writeString(comments);
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "task_id=" + task_id +
                ", subtitle_position=" + subtitle_position +
                ", subtitle_version='" + subtitle_version + '\'' +
                ", reason_id=" + reason_id +
                ", reasonList='" + reasonList + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}
