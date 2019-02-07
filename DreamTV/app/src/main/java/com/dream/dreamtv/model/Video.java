package com.dream.dreamtv.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.dream.dreamtv.R;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

/**
 * Created by manuel on 6/12/17.
 */

public class Video implements Parcelable {
    private static final String YOUTUBE_COM = "youtube.com";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String YOUTUBE = "youtube";
    public static final String QUERY_PARAMETER = "v";

    public String id;
    public String video_type;
    public String primary_audio_language_code;
    public String original_language;
    public String title;
    public String description;
    public int duration;
    public String thumbnail;
    private String created;
    public String team;
    public String project;
    public String video_url;
    private List<String> all_urls;
    private List<Language> languages;
    private String activity_uri;
    private String urls_uri;
    private String subtitle_languages_uri;
    private String resource_uri;

    //To use video as a parameter in UserVideos
    public String video_id;

    //To keep tracking of the task
    public String subtitle_language;
    public int task_id;



    public Video() {
    }

    public Video(Task task){
        this.id = task.video_id;
        this.video_type = task.type;
        this.primary_audio_language_code = task.primary_audio_language_code;
        this.original_language = task.original_language;
        this.title = task.title;
        this.description = task.description;
        this.duration = task.duration;
        this.thumbnail = task.thumbnail;
        this.team = task.team;
        this.project = task.project;
        this.video_url = task.video_url;
        this.subtitle_language =  task.language;
        this.task_id =  task.task_id;
    }


    private Video(Parcel in) {
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
        video_url = in.readString();
        all_urls = in.createStringArrayList();
        languages = in.createTypedArrayList(Language.CREATOR);
        activity_uri = in.readString();
        urls_uri = in.readString();
        subtitle_languages_uri = in.readString();
        resource_uri = in.readString();
        subtitle_language = in.readString();
        task_id = in.readInt();
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
        if (this.all_urls == null)
            url = video_url;
        else if (this.all_urls.get(0).contains(YOUTUBE))
            url = this.all_urls.get(0).replace(HTTP, HTTPS);
        else
            url = this.all_urls.get(0);

        return url;
    }

    public boolean isUrlFromYoutube() {
        if (this.all_urls != null)
            return this.all_urls.get(0).contains(YOUTUBE_COM);
        else
            return this.video_url.contains(YOUTUBE_COM);
    }

    public String getVideoYoutubeId() {
        Uri newUrl;
        if (this.all_urls != null)
            newUrl = Uri.parse(this.all_urls.get(0));
        else
            newUrl = Uri.parse(this.video_url);

        return newUrl.getQueryParameter(QUERY_PARAMETER);
    }




    public long getVideoDurationInMs(){
        return this.duration * 1000;
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
        parcel.writeString(video_url);
        parcel.writeStringList(all_urls);
        parcel.writeTypedList(languages);
        parcel.writeString(activity_uri);
        parcel.writeString(urls_uri);
        parcel.writeString(subtitle_languages_uri);
        parcel.writeString(resource_uri);
        parcel.writeString(subtitle_language);
        parcel.writeInt(task_id);
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
                ", video_url='" + video_url + '\'' +
                ", all_urls=" + all_urls +
                ", languages=" + languages +
                ", activity_uri='" + activity_uri + '\'' +
                ", urls_uri='" + urls_uri + '\'' +
                ", subtitle_languages_uri='" + subtitle_languages_uri + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                ", subtitle_language='" + subtitle_language + '\'' +
                ", task_id=" + task_id +
                '}';
    }
}
