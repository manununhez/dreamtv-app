package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleJson implements Parcelable {
    public int version_number;
    public String sub_format;
    public List<Subtitle> subtitles;


    protected SubtitleJson(Parcel in) {
        version_number = in.readInt();
        sub_format = in.readString();
        subtitles = in.createTypedArrayList(Subtitle.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version_number);
        dest.writeString(sub_format);
        dest.writeTypedList(subtitles);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubtitleJson> CREATOR = new Creator<SubtitleJson>() {
        @Override
        public SubtitleJson createFromParcel(Parcel in) {
            return new SubtitleJson(in);
        }

        @Override
        public SubtitleJson[] newArray(int size) {
            return new SubtitleJson[size];
        }
    };
}
