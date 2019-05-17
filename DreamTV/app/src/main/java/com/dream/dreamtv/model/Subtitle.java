package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 6/27/17.
 */

public class Subtitle implements Parcelable {
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
    private static final int ONE_SEC_IN_MS = 1000;
    public final String text;
    public final int position;
    private int start; //msecs
    private int end; //msecs

    private Subtitle(Parcel in) {
        start = in.readInt();
        text = in.readString();
        end = in.readInt();
        position = in.readInt();
    }

    public int getStartInSecs() {
            return start / ONE_SEC_IN_MS;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEndInSecs() {
            return end / ONE_SEC_IN_MS;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

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
