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
    public String subFormat;
    @SerializedName("title")
    public String videoTitleTranslated;
    @SerializedName("description")
    public String videoDescriptionTranslated;
    @SerializedName("video_title")
    public String videoTitleOriginal;
    @SerializedName("video_description")
    public String videoDescriptionOriginal;


    protected SubtitleResponse(Parcel in) {
        versionNumber = in.readInt();
        subtitles = in.createTypedArrayList(Subtitle.CREATOR);
        subFormat = in.readString();
        videoTitleTranslated = in.readString();
        videoDescriptionTranslated = in.readString();
        videoTitleOriginal = in.readString();
        videoDescriptionOriginal = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(versionNumber);
        dest.writeTypedList(subtitles);
        dest.writeString(subFormat);
        dest.writeString(videoTitleTranslated);
        dest.writeString(videoDescriptionTranslated);
        dest.writeString(videoTitleOriginal);
        dest.writeString(videoDescriptionOriginal);
    }

    @Override
    public int describeContents() {
        return 0;
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
    public String toString() {
        return "SubtitleResponse{" +
                "versionNumber=" + versionNumber +
                ", subtitles=" + subtitles +
                ", subFormat='" + subFormat + '\'' +
                ", videoTitleTranslated='" + videoTitleTranslated + '\'' +
                ", videoDescriptionTranslated='" + videoDescriptionTranslated + '\'' +
                ", videoTitleOriginal='" + videoTitleOriginal + '\'' +
                ", videoDescriptionOriginal='" + videoDescriptionOriginal + '\'' +
                '}';
    }
}
