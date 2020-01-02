package com.dreamproject.dreamtv.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by manuel on 7/8/17.
 */

public class User implements Parcelable {
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
    private String email;
    private String password;
    private String subLanguage;
    private String audioLanguage;
    private String interfaceMode;

    public User(String email, String password, String subLanguage, String audioLanguage, String interfaceMode) {
        this.email = email;
        this.password = password;
        this.subLanguage = subLanguage;
        this.audioLanguage = audioLanguage;
        this.interfaceMode = interfaceMode;
    }

    protected User(Parcel in) {
        email = in.readString();
        password = in.readString();
        subLanguage = in.readString();
        audioLanguage = in.readString();
        interfaceMode = in.readString();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubLanguage() {
        return subLanguage;
    }

    public void setSubLanguage(String subLanguage) {
        this.subLanguage = subLanguage;
    }

    public String getAudioLanguage() {
        return audioLanguage;
    }

    public void setAudioLanguage(String audioLanguage) {
        this.audioLanguage = audioLanguage;
    }

    public String getInterfaceMode() {
        return interfaceMode;
    }

    public void setInterfaceMode(String interfaceMode) {
        this.interfaceMode = interfaceMode;
    }

    public boolean isEmpty() {
        return email == null || email.length() == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(subLanguage);
        dest.writeString(audioLanguage);
        dest.writeString(interfaceMode);
    }

    @Override
    public String toString() {
        return "User{" +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", subLanguage='" + subLanguage + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                ", interfaceMode='" + interfaceMode + '\'' +
                '}';
    }
}
