package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 6/12/17.
 */

public class Language implements Parcelable{
    public String code;
    public String name;
    public boolean published;
    public String dir;
    public String subtitles_uri;
    public String resource_uri;

    protected Language(Parcel in) {
        code = in.readString();
        name = in.readString();
        published = in.readByte() != 0;
        dir = in.readString();
        subtitles_uri = in.readString();
        resource_uri = in.readString();
    }

    public static final Creator<Language> CREATOR = new Creator<Language>() {
        @Override
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        @Override
        public Language[] newArray(int size) {
            return new Language[size];
        }
    };

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", published=" + published +
                ", dir='" + dir + '\'' +
                ", subtitles_uri='" + subtitles_uri + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeByte((byte) (published ? 1 : 0));
        parcel.writeString(dir);
        parcel.writeString(subtitles_uri);
        parcel.writeString(resource_uri);
    }
}
