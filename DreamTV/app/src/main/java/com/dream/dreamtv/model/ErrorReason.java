package com.dream.dreamtv.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class ErrorReason {
    @SerializedName("reason_code")
    public String reasonCode;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("language")
    public String language;
}
