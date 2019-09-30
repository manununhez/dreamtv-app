package com.dream.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 7/18/17.
 */

public class ErrorReason implements Parcelable {
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
    private String reasonCode;
    private String name;
    private String description;
    private String language;

    public ErrorReason(String reasonCode, String name, String description, String language) {
        this.reasonCode = reasonCode;
        this.name = name;
        this.description = description;
        this.language = language;
    }

    protected ErrorReason(Parcel in) {
        reasonCode = in.readString();
        name = in.readString();
        description = in.readString();
        language = in.readString();
    }

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

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
