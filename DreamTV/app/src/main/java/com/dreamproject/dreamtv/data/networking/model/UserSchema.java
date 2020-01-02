package com.dreamproject.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/8/17.
 */

public class UserSchema {
    @SerializedName("id")
    public int id;
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

}
