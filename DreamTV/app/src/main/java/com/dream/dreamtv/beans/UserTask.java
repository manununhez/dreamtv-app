package com.dream.dreamtv.beans;

import java.util.List;

/**
 * Created by manuel on 7/8/17.
 */

public class UserTask extends JsonRequestBaseBean {
    public int id;
    public int task_id;
    public String video_id;
    public int priority;
    public String type;
    public String language;
    public String created_at;
    public String updated_at;
    public String primary_audio_language_code;
    public String original_language;
    public String video_url;
    public String title;
    public String description;
    public int duration;
    public String thumbnail;
    public String team;
    public String project;


    public Video getVideo(){
       Video video = new Video();
        video.id = this.video_id;
        video.video_type = this.type;
        video.primary_audio_language_code = this.primary_audio_language_code;
        video.original_language = this.original_language;
        video.title = this.title;
        video.description = this.description;
        video.duration = this.duration;
        video.thumbnail = this.thumbnail;
        video.team = this.team;
        video.project = this.project;
        video.video_url = this.video_url;
        return video;
    }
}
