package com.dream.dreamtv.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.dream.dreamtv.DreamTVApp;

import java.util.List;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {
    public String id;
    public String video_type;
    public String primary_audio_language_code;
    public String original_language;
    public String title;
    public String description;
    public int duration;
    public String thumbnail;
    public String created;
    public String team;
    public String project;
    public List<String> all_urls;
    public List<Language> languages;
    public String activity_uri;
    public String urls_uri;
    public String subtitle_languages_uri;
    public String resource_uri;

    protected Video(Parcel in) {
        id = in.readString();
        video_type = in.readString();
        primary_audio_language_code = in.readString();
        original_language = in.readString();
        title = in.readString();
        description = in.readString();
        duration = in.readInt();
        thumbnail = in.readString();
        created = in.readString();
        team = in.readString();
        project = in.readString();
        all_urls = in.createStringArrayList();
        activity_uri = in.readString();
        urls_uri = in.readString();
        subtitle_languages_uri = in.readString();
        resource_uri = in.readString();
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

    public String getVideoUrl() {
        String url;
        if (this.all_urls.get(0).contains("youtube"))
            url = this.all_urls.get(0).replace("http", "https");
        else
            url = this.all_urls.get(0);

        DreamTVApp.Logger.d("VideoUrl -> "+url);
        return url;
//        return "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4";
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", video_type='" + video_type + '\'' +
                ", primary_audio_language_code='" + primary_audio_language_code + '\'' +
                ", original_language='" + original_language + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", thumbnail='" + thumbnail + '\'' +
                ", created='" + created + '\'' +
                ", team='" + team + '\'' +
                ", project='" + project + '\'' +
                ", all_urls=" + all_urls +
                ", languages=" + languages +
                ", activity_uri='" + activity_uri + '\'' +
                ", urls_uri='" + urls_uri + '\'' +
                ", subtitle_languages_uri='" + subtitle_languages_uri + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(video_type);
        parcel.writeString(primary_audio_language_code);
        parcel.writeString(original_language);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeInt(duration);
        parcel.writeString(thumbnail);
        parcel.writeString(created);
        parcel.writeString(team);
        parcel.writeString(project);
        parcel.writeStringList(all_urls);
        parcel.writeString(activity_uri);
        parcel.writeString(urls_uri);
        parcel.writeString(subtitle_languages_uri);
        parcel.writeString(resource_uri);
    }
}
