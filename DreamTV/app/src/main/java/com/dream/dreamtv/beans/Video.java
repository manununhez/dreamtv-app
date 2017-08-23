package com.dream.dreamtv.beans;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.dream.dreamtv.DreamTVApp;

import java.util.ArrayList;
import java.util.Arrays;
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
    public String video_url;
    public List<String> all_urls;
    public List<Language> languages;
    public String activity_uri;
    public String urls_uri;
    public String subtitle_languages_uri;
    public String resource_uri;

    //Para almacenar datos de la tarea
    public String subtitle_language;
    public int task_id;
    public int task_state; //indicates if the task have or have not done yet. If it the task comes from the category "See Again", task_state == 1, else task_state == 0
    public UserTask[] userTaskList; //all user tasks saved

    //Para almacenar los subtitlos e ir propagando entre pantallas
    public SubtitleJson subtitle_json; //para ir propagando el subtitulo entre pantallas

    public Video() {
    }


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
        video_url = in.readString();
        all_urls = in.createStringArrayList();
        languages = in.createTypedArrayList(Language.CREATOR);
        activity_uri = in.readString();
        urls_uri = in.readString();
        subtitle_languages_uri = in.readString();
        resource_uri = in.readString();
        subtitle_language = in.readString();
        task_id = in.readInt();
        task_state = in.readInt();
        userTaskList = in.createTypedArray(UserTask.CREATOR);
        subtitle_json = in.readParcelable(SubtitleJson.class.getClassLoader());
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
        else if (this.all_urls.get(0).contains("youtube"))
            url = this.all_urls.get(0).replace("http", "https");
        else
            url = this.all_urls.get(0);

        DreamTVApp.Logger.d("VideoUrl -> " + url);
        return url;
    }

    public boolean isFromYoutube() {
        if (this.all_urls != null)
            return this.all_urls.get(0).contains("youtube.com");
        else
            return this.video_url.contains("youtube.com");
    }

    public String getVideoYoutubeId() {
        Uri newUrl;
        if (this.all_urls != null)
            newUrl = Uri.parse(this.all_urls.get(0));
        else
            newUrl = Uri.parse(this.video_url);

        return newUrl.getQueryParameter("v");
    }


    public Subtitle getSyncSubtitleText(long l) {
        List<Subtitle> subtitleList = this.subtitle_json.subtitles;
        Subtitle subtitle = null;
        for (Subtitle subtitleTemp : subtitleList) {
            if (l >= subtitleTemp.start && l <= subtitleTemp.end) { //esta adentro del ciclo
                subtitle = subtitleTemp;
                break;
            } else if (l < subtitleTemp.start)
                break;

        }

        return subtitle;
    }

    public UserTask getUserTask(long l) {
        Subtitle subtitle = getSyncSubtitleText(l);
        if (subtitle != null) { //if subtitle == null, there is not subtitle in the time selected
            if (this.userTaskList != null && this.userTaskList.length > 0)
                for (UserTask userTask : this.userTaskList) {
                    if (userTask.subtitle_position == subtitle.position) {
                        //after we find the position, we delete that option from the list. This allow us to show only once the respective reason as a popup
                        List<UserTask> arrayList = new ArrayList<>(Arrays.asList(this.userTaskList));
                        arrayList.remove(userTask);
                        this.userTaskList = arrayList.toArray(new UserTask[arrayList.size()]);
                        return userTask;
                    }
                }
        }

        return null;
    }

    public Subtitle getLastSubtitlePositionTime() {
        if (this.userTaskList != null && this.userTaskList.length > 0) {
            UserTask lastUserTask = this.userTaskList[userTaskList.length - 1];
            List<Subtitle> subtitleList = this.subtitle_json.subtitles;
            for (Subtitle subtitle : subtitleList)
                if (subtitle.position == lastUserTask.subtitle_position)
                    return subtitle;

        }
        return null;
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
        parcel.writeInt(task_state);
        parcel.writeTypedArray(userTaskList, i);
        parcel.writeParcelable(subtitle_json, i);
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
                ", task_state=" + task_state +
                ", userTaskList=" + Arrays.toString(userTaskList) +
                ", subtitle_json=" + subtitle_json +
                '}';
    }
}
