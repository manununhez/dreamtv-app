package com.dream.dreamtv.beans;

/**
 * Created by manuel on 7/8/17.
 */

public class Task extends JsonRequestBaseBean {
    public int id;
    public String video_id;
    public Video video;
    public String language;
    public String type;
    public User assignee;
    public int priority;
    public String created;
    public String modified;
    public String completed;
    public String approved;
    public String resource_uri;
    //    request
    public String[] type_task;
    public String team;
    public int limit;
    public int offset;
}
