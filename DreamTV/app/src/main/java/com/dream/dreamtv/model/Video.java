package com.dream.dreamtv.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {

    private static final String YOUTUBE_COM = "youtube.com";
    public static final String QUERY_PARAMETER = "v";


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

    public String video_id;
    public String primary_audio_language_code;
    public String title;
    public String description;
    public int duration;
    public String thumbnail;
    public String team;
    public String project;
    public String video_url;
    public String created_at;
    public String updated_at;



    @Ignore
    public Video() {

    }

    public Video(String video_id, String primary_audio_language_code,
                 String title, String description, int duration, String thumbnail,
                 String team, String project, String video_url) {
        this.video_id = video_id;
        this.primary_audio_language_code = primary_audio_language_code;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.thumbnail = thumbnail;
        this.team = team;
        this.project = project;
        this.video_url = video_url;
    }

    protected Video(Parcel in) {
        video_id = in.readString();
        primary_audio_language_code = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readInt();
        thumbnail = in.readString();
        team = in.readString();
        project = in.readString();
        video_url = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(video_id);
        dest.writeString(primary_audio_language_code);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(duration);
        dest.writeString(thumbnail);
        dest.writeString(team);
        dest.writeString(project);
        dest.writeString(video_url);
        dest.writeString(created_at);
        dest.writeString(updated_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public boolean isUrlFromYoutube() {
        return this.video_url.contains(YOUTUBE_COM);
    }

    public String getVideoYoutubeId() {
        Uri newUrl = Uri.parse(this.video_url);

        return newUrl.getQueryParameter(QUERY_PARAMETER);
    }


    public long getVideoDurationInMs(){
        return this.duration * 1000;
    }


}
