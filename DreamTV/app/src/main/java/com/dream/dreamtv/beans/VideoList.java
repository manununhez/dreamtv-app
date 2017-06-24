package com.dream.dreamtv.beans;

import java.util.List;

/**
 * Created by manuel on 6/12/17.
 */

public class VideoList {
    public Metadata meta;
    public List<Video> objects;

    @Override
    public String toString() {
        return "VideoList{" +
                "meta=" + meta +
                ", objects=" + objects +
                '}';
    }
}
