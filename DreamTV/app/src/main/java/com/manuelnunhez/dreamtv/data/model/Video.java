package com.manuelnunhez.dreamtv.data.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

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
    public String videoId;
    public String audioLanguage;
    public String speakerName;
    public String title;
    public String description;
    public int duration; //secs
    public String thumbnail;
    public String team;
    public String project;
    public String videoUrl;

    public Video(String videoId, String audioLanguage, String speakerName, String title, String description,
                 int duration, String thumbnail, String team, String project, String videoUrl) {
        this.videoId = videoId;
        this.audioLanguage = audioLanguage;
        this.speakerName = speakerName;
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
        audioLanguage = in.readString();
        speakerName = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readInt();
        thumbnail = in.readString();
        team = in.readString();
        project = in.readString();
        videoUrl = in.readString();
    }

    public boolean isUrlFromYoutube() {
        return this.videoUrl.contains(YOUTUBE_COM);
    }

    public String getVideoYoutubeId() {
        Uri newUrl = Uri.parse(this.videoUrl);

        return newUrl.getQueryParameter(QUERY_PARAMETER);
    }

    public long getVideoDuration() {
        return this.duration;
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
        dest.writeString(audioLanguage);
        dest.writeString(speakerName);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(duration);
        dest.writeString(thumbnail);
        dest.writeString(team);
        dest.writeString(project);
        dest.writeString(videoUrl);
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoId='" + videoId + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                ", speakerName='" + speakerName + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", thumbnail='" + thumbnail + '\'' +
                ", team='" + team + '\'' +
                ", project='" + project + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
