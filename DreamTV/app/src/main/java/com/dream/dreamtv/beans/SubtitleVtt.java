package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleVtt implements Parcelable {
    public int version_number;
    public String sub_format;
    public String subtitles;

    protected SubtitleVtt(Parcel in) {
        version_number = in.readInt();
        sub_format = in.readString();
        subtitles = in.readString();
    }

    public static final Creator<SubtitleVtt> CREATOR = new Creator<SubtitleVtt>() {
        @Override
        public SubtitleVtt createFromParcel(Parcel in) {
            return new SubtitleVtt(in);
        }

        @Override
        public SubtitleVtt[] newArray(int size) {
            return new SubtitleVtt[size];
        }
    };

    @Override
    public String toString() {
        return "Subtitle{" +
                "version_number=" + version_number +
                ", sub_format='" + sub_format + '\'' +
                ", subtitles='" + subtitles + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(version_number);
        parcel.writeString(sub_format);
        parcel.writeString(subtitles);
    }
}
