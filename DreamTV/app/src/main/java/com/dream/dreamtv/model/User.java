package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/8/17.
 */

public class User {
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
    @SerializedName("interface_language")
    public String interfaceLanguage;


    @Override
    public String toString() {
        return "User{" +
                "token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", subLanguage='" + subLanguage + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                ", id='" + id + '\'' +
                ", interfaceMode='" + interfaceMode + '\'' +
                ", interfaceLanguage='" + interfaceLanguage + '\'' +
                '}';
    }

    public boolean isEmpty() {
        return email == null || email.length() == 0 ;
    }
}
