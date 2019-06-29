package com.dream.dreamtv.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {


    private static final String YOUTUBE_COM = "youtube.com";
    private static final String QUERY_PARAMETER = "v";

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


    protected Video(Parcel in) {
        videoId = in.readString();
        primaryAudioLanguageCode = in.readString();
        speakerName = in.readString();
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(primaryAudioLanguageCode);
        dest.writeString(speakerName);
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
    public String toString() {
        return "Video{" +
                "videoId='" + videoId + '\'' +
                ", primaryAudioLanguageCode='" + primaryAudioLanguageCode + '\'' +
                ", speakerName='" + speakerName + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", thumbnail='" + thumbnail + '\'' +
                ", team='" + team + '\'' +
                ", project='" + project + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }
}
