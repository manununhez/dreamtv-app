package com.dream.dreamtv.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import androidx.room.Ignore;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    private static final String YOUTUBE_COM = "youtube.com";
    private static final String QUERY_PARAMETER = "v";

    @SerializedName("video_id")
    public String videoId;
    @SerializedName("primary_audio_language_code")
    public String primaryAudioLanguageCode;
    @SerializedName("title")
    public String title;
    @SerializedName("description")
    public String description;
    @SerializedName("duration")
    public int duration;
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

    @Ignore
    public Video() {

    }

    public Video(String videoId, String primaryAudioLanguageCode,
                 String title, String description, int duration, String thumbnail,
                 String team, String project, String videoUrl) {
        this.videoId = videoId;
        this.primaryAudioLanguageCode = primaryAudioLanguageCode;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.thumbnail = thumbnail;
        this.team = team;
        this.project = project;
        this.videoUrl = videoUrl;
    }

    protected Video(Parcel in) {
        videoId = in.readString();
        primaryAudioLanguageCode = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readInt();
        thumbnail = in.readString();
        team = in.readString();
        project = in.readString();
        videoUrl = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(primaryAudioLanguageCode);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(duration);
        dest.writeString(thumbnail);
        dest.writeString(team);
        dest.writeString(project);
        dest.writeString(videoUrl);
        dest.writeString(created_at);
        dest.writeString(updated_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public boolean isUrlFromYoutube() {
        return this.videoUrl.contains(YOUTUBE_COM);
    }

    public String getVideoYoutubeId() {
        Uri newUrl = Uri.parse(this.videoUrl);

        return newUrl.getQueryParameter(QUERY_PARAMETER);
    }


    public long getVideoDurationInMs() {
        return this.duration * 1000;
    }


}
