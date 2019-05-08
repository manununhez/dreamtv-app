package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleResponse implements Parcelable {
    @SerializedName("version_number")
    public int versionNumber;
    @SerializedName("sub_format")
    private String subFormat;
    @SerializedName("subtitles")
    public List<Subtitle> subtitles;

//    public Subtitle getLastSubtitlePositionTime(int position) {
//        if (this.userTaskList != null && this.userTaskList.size() > 0) {
//            UserTask lastUserTask = this.userTaskList.get(userTaskList.size() - 1);
//            List<Subtitle> subtitleList = this.subtitle_json.subtitles;
//            for (Subtitle subtitle : subtitleList)
//                if (subtitle.position == lastUserTask.subtitlePosition)
//                    return subtitle;
//
//        }
//        return null;
//    }

    public Subtitle getSyncSubtitleText(long l) {
        Subtitle subtitle = null;
        for (Subtitle subtitleTemp : this.subtitles) {
            if (l >= subtitleTemp.start && l <= subtitleTemp.end) { //esta adentro del ciclo
                subtitle = subtitleTemp;
                break;
            } else if (l < subtitleTemp.start)
                break;

        }

        return subtitle;
    }

    protected SubtitleResponse(Parcel in) {
        versionNumber = in.readInt();
        subFormat = in.readString();
        subtitles = in.createTypedArrayList(Subtitle.CREATOR);
    }

    public static final Creator<SubtitleResponse> CREATOR = new Creator<SubtitleResponse>() {
        @Override
        public SubtitleResponse createFromParcel(Parcel in) {
            return new SubtitleResponse(in);
        }

        @Override
        public SubtitleResponse[] newArray(int size) {
            return new SubtitleResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(versionNumber);
        parcel.writeString(subFormat);
        parcel.writeTypedList(subtitles);
    }


    @Override
    public String toString() {
        return "SubtitleResponse{" +
                "versionNumber=" + versionNumber +
                ", subFormat='" + subFormat + '\'' +
                ", subtitles=" + subtitles +
                '}';
    }
}
