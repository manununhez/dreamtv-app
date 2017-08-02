package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 7/20/17.
 */

public class UserTaskList extends JsonRequestBaseBean implements Parcelable{
    public List<UserTask> data;

    protected UserTaskList(Parcel in) {
        data = in.createTypedArrayList(UserTask.CREATOR);
    }

    public static final Creator<UserTaskList> CREATOR = new Creator<UserTaskList>() {
        @Override
        public UserTaskList createFromParcel(Parcel in) {
            return new UserTaskList(in);
        }

        @Override
        public UserTaskList[] newArray(int size) {
            return new UserTaskList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(data);
    }
}
