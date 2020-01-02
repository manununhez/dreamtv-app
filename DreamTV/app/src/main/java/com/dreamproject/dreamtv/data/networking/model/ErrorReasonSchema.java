package com.dreamproject.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 7/18/17.
 */

public class ErrorReasonSchema {
    @SerializedName("reason_code")
    public String reasonCode;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("language")
    public String language;

    public ErrorReasonSchema(String reasonCode, String name, String description, String language) {
        this.reasonCode = reasonCode;
        this.name = name;
        this.description = description;
        this.language = language;
    }
}
