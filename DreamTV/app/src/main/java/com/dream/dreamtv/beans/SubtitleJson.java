package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 6/26/17.
 */

public class SubtitleJson extends JsonRequestBaseBean implements Parcelable {
    public int version_number;
    public String version;
    public String sub_format;
    public List<Subtitle> subtitles;

    //request
    public String video_id;
    public String language_code;

    public SubtitleJson() {
    }


    protected SubtitleJson(Parcel in) {
        version_number = in.readInt();
        version = in.readString();
        sub_format = in.readString();
        subtitles = in.createTypedArrayList(Subtitle.CREATOR);
        video_id = in.readString();
        language_code = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version_number);
        dest.writeString(version);
        dest.writeString(sub_format);
        dest.writeTypedList(subtitles);
        dest.writeString(video_id);
        dest.writeString(language_code);
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

    @Override
    public String toString() {
        return "SubtitleJson{" +
                "version_number=" + version_number +
                ", version='" + version + '\'' +
                ", sub_format='" + sub_format + '\'' +
                ", subtitles=" + subtitles +
                ", video_id='" + video_id + '\'' +
                ", language_code='" + language_code + '\'' +
                '}';
    }
}
