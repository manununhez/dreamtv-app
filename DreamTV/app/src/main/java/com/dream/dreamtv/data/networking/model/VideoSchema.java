package com.dream.dreamtv.data.networking.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 6/12/17.
 */

public class VideoSchema {

    @SerializedName("video_id")
    public String videoId;
    @SerializedName("primary_audio_language_code")
    public String primaryAudioLanguageCode;
    @SerializedName("speaker_name")
    public String speakerName;
    @SerializedName("title")
    public String title;
    @SerializedName("description")
    public String description;
    @SerializedName("duration")
    public int duration; //secs
    @SerializedName("thumbnail")
    public String thumbnail;
    @SerializedName("team")
    public String team;
    @SerializedName("project")
    public String project;
    @SerializedName("video_url")
    public String videoUrl;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;


}
