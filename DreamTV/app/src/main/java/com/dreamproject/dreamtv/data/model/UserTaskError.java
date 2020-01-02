package com.dreamproject.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class UserTaskError implements Parcelable {
    public static final Creator<UserTaskError> CREATOR = new Creator<UserTaskError>() {
        @Override
        public UserTaskError createFromParcel(Parcel in) {
            return new UserTaskError(in);
        }

        @Override
        public UserTaskError[] newArray(int size) {
            return new UserTaskError[size];
        }
    };
    private String reasonCode;
    private List<ErrorReason> errorReasonList;
    private int subtitlePosition;
    private String comment;

    public UserTaskError(String reasonCode, int subtitlePosition, String comment) {
        this.reasonCode = reasonCode;
        this.subtitlePosition = subtitlePosition;
        this.comment = comment;
    }

    public UserTaskError(List<ErrorReason> errorReasonList, int subtitlePosition, String comment) {
        this.errorReasonList = errorReasonList;
        this.subtitlePosition = subtitlePosition;
        this.comment = comment;
    }

    public UserTaskError(Parcel in) {
        reasonCode = in.readString();
        subtitlePosition = in.readInt();
        comment = in.readString();
    }

    public List<ErrorReason> getErrorReasonList() {
        return errorReasonList;
    }

    public void setErrorReasonList(List<ErrorReason> errorReasonList) {
        this.errorReasonList = errorReasonList;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public int getSubtitlePosition() {
        return subtitlePosition;
    }

    public void setSubtitlePosition(int subtitlePosition) {
        this.subtitlePosition = subtitlePosition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reasonCode);
        dest.writeInt(subtitlePosition);
        dest.writeString(comment);
    }

    @Override
    public String toString() {
        return "UserTaskError{" +
                ", reasonCode='" + reasonCode + '\'' +
                ", subtitlePosition=" + subtitlePosition +
                ", comment='" + comment + '\'' +
                '}';
    }
}
