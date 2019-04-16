package com.dream.dreamtv.model;

/**
 * Created by manuel on 7/8/17.
 */

public class User {
    public String token;
    public String email;
    public String password;
    public String sub_language;
    public String audio_language;
    public int id;

    public String interface_mode;
    public String interface_language;


    @Override
    public String toString() {
        return "User{" +
                "token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", sub_language='" + sub_language + '\'' +
                ", audio_language='" + audio_language + '\'' +
                ", id='" + id + '\'' +
                ", interface_mode='" + interface_mode + '\'' +
                ", interface_language='" + interface_language + '\'' +
                '}';
    }

}
