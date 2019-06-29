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
    @SerializedName("subtitles")
    public List<Subtitle> subtitles;
    @SerializedName("sub_format")
    private String subFormat;

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


    public Subtitle getSyncSubtitleText(long l) {
        Subtitle subtitle = null;
        for (Subtitle subtitleTemp : this.subtitles) {
            if (l >= subtitleTemp.getStart() && l <= subtitleTemp.getEnd()) { //esta adentro del ciclo
                subtitle = subtitleTemp;
                break;
            } else if (l < subtitleTemp.getStart())
                break;

        }

        return subtitle;
    }

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
                ", subtitles=" + subtitles +
                ", subFormat='" + subFormat + '\'' +
                '}';
    }
}
