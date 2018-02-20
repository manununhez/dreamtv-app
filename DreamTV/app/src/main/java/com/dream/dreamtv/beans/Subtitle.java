package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 6/27/17.
 */

public class Subtitle implements Parcelable {
    public int start;
    public String text;
    public int end;
    public int position;

    protected Subtitle(Parcel in) {
        start = in.readInt();
        text = in.readString();
        end = in.readInt();
        position = in.readInt();
    }

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in);
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(start);
        parcel.writeString(text);
        parcel.writeInt(end);
        parcel.writeInt(position);
    }

    @Override
    public String toString() {
        return "Subtitle{" +
                "start=" + start +
                ", text='" + text + '\'' +
                ", end=" + end +
                ", position=" + position +
                '}';
    }
}
