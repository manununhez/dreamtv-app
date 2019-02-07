package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleResponse implements Parcelable {
    public int version_number;
    private String sub_format;
    public List<Subtitle> subtitles;


    protected SubtitleResponse(Parcel in) {
        version_number = in.readInt();
        sub_format = in.readString();
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
        parcel.writeInt(version_number);
        parcel.writeString(sub_format);
        parcel.writeTypedList(subtitles);
    }


    @Override
    public String toString() {
        return "SubtitleResponse{" +
                "version_number=" + version_number +
                ", sub_format='" + sub_format + '\'' +
                ", subtitles=" + subtitles +
                '}';
    }
}
