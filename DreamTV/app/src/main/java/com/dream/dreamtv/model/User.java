package com.dream.dreamtv.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/8/17.
 */

public class User implements Parcelable {
    @SerializedName("id")
    public int id;
    @SerializedName("token")
    public String token;
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("sub_language")
    public String subLanguage;
    @SerializedName("audio_language")
    public String audioLanguage;
    @SerializedName("interface_mode")
    public String interfaceMode;

    public User() {
    }

    protected User(Parcel in) {
        id = in.readInt();
        token = in.readString();
        email = in.readString();
        password = in.readString();
        subLanguage = in.readString();
        audioLanguage = in.readString();
        interfaceMode = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public boolean isEmpty() {
        return email == null || email.length() == 0 ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(token);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(subLanguage);
        dest.writeString(audioLanguage);
        dest.writeString(interfaceMode);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", subLanguage='" + subLanguage + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                ", interfaceMode='" + interfaceMode + '\'' +
                '}';
    }
}
