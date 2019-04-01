package com.dream.dreamtv.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {
    private static final String YOUTUBE_COM = "youtube.com";
    public static final String QUERY_PARAMETER = "v";

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

    //To keep tracking of the task
    public String subtitle_language;
    public int task_id;



    public Video() {
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
        subtitle_language = in.readString();
        task_id = in.readInt();
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
        dest.writeString(subtitle_language);
        dest.writeInt(task_id);
    }

    @Override
    public int describeContents() {
        return 0;
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
