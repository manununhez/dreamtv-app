package com.dream.dreamtv.beans;

/**
 * Created by manuel on 6/12/17.
 */

public class Language {
    public String code;
    public String name;
    public boolean published;
    public String dir;
    public String subtitles_uri;
    public String resource_uri;

    @Override
    public String toString() {
        return "Language{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", published=" + published +
                ", dir='" + dir + '\'' +
                ", subtitles_uri='" + subtitles_uri + '\'' +
                ", resource_uri='" + resource_uri + '\'' +
                '}';
    }
}
