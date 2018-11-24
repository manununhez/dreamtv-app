package com.dream.dreamtv.model;

/**
 * Created by manuel on 7/8/17.
 */

public class Task extends JsonRequestBaseBean {
    public int id;
    private int task_id;
    public String video_id;
    public int priority;
    private String type;
    private String language;
    public String created_at;
    public String updated_at;
    private String primary_audio_language_code;
    private String original_language;
    private String video_url;
    private String title;
    private String description;
    private int duration;
    private String thumbnail;
    private String team;
    private String project;


    public Video getVideo(int taskState){
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
        video.subtitle_language =  this.language;
        video.task_id =  this.task_id;
        video.task_state = taskState;

        return video;
    }
}
