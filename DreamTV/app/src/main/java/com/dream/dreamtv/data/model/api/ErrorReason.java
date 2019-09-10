package com.dream.dreamtv.data.model.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class ErrorReason implements Parcelable {
    @SerializedName("reason_code")
    public String reasonCode;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("language")
    public String language;

    protected ErrorReason(Parcel in) {
        reasonCode = in.readString();
        name = in.readString();
        description = in.readString();
        language = in.readString();
    }

    public static final Creator<ErrorReason> CREATOR = new Creator<ErrorReason>() {
        @Override
        public ErrorReason createFromParcel(Parcel in) {
            return new ErrorReason(in);
        }

        @Override
        public ErrorReason[] newArray(int size) {
            return new ErrorReason[size];
        }
    };

    @Override
    public String toString() {
        return "ErrorReason{" +
                "reasonCode='" + reasonCode + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", language='" + language + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reasonCode);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(language);
    }
}
