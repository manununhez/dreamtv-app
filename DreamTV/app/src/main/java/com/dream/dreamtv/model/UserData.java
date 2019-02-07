package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class UserData implements Parcelable {
    public Video mSelectedVideo; //selected video from respect category
    public UserTask[] userTaskList; //all user tasks saved
    public SubtitleResponse subtitle_json; //To keep subtitle data between screens

    public int category; //indicates task category

    public UserData() {
    }


    public Subtitle getSyncSubtitleText(long l) {
        List<Subtitle> subtitleList = this.subtitle_json.subtitles;
        Subtitle subtitle = null;
        for (Subtitle subtitleTemp : subtitleList) {
            if (l >= subtitleTemp.start && l <= subtitleTemp.end) { //esta adentro del ciclo
                subtitle = subtitleTemp;
                break;
            } else if (l < subtitleTemp.start)
                break;

        }

        return subtitle;
    }

    public UserTask getUserTask(long l) {
        Subtitle subtitle = getSyncSubtitleText(l);
        if (subtitle != null) { //if subtitle == null, there is not subtitle in the time selected
            if (this.userTaskList != null && this.userTaskList.length > 0)
                for (UserTask userTask : this.userTaskList) {
                    if (userTask.subtitle_position == subtitle.position) {
                        return userTask;
                    }
                }
        }

        return null;
    }

    public Subtitle getLastSubtitlePositionTime() {
        if (this.userTaskList != null && this.userTaskList.length > 0) {
            UserTask lastUserTask = this.userTaskList[userTaskList.length - 1];
            List<Subtitle> subtitleList = this.subtitle_json.subtitles;
            for (Subtitle subtitle : subtitleList)
                if (subtitle.position == lastUserTask.subtitle_position)
                    return subtitle;

        }
        return null;
    }


    protected UserData(Parcel in) {
        mSelectedVideo = in.readParcelable(Video.class.getClassLoader());
        userTaskList = in.createTypedArray(UserTask.CREATOR);
        subtitle_json = in.readParcelable(SubtitleResponse.class.getClassLoader());
        category = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSelectedVideo, flags);
        dest.writeTypedArray(userTaskList, flags);
        dest.writeParcelable(subtitle_json, flags);
        dest.writeInt(category);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
}
