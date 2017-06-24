package com.dream.dreamtv.beans;

/**
 * Created by manuel on 6/15/17.
 */

public class Project {
    public String name;
    public String slug;
    public String description;
    public String modified;
    public String created;
    public boolean workflow_enabled;
    public String resource_uri;


    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", modified='" + modified + '\'' +
                ", created='" + created + '\'' +
                ", workflow_enabled=" + workflow_enabled +
                ", resource_uri='" + resource_uri + '\'' +
                '}';
    }
}
