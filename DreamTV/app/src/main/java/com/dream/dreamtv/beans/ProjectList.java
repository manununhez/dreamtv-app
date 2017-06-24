package com.dream.dreamtv.beans;

import java.util.List;

/**
 * Created by manuel on 6/15/17.
 */

public class ProjectList {
    public Metadata meta;
    public List<Project> objects;

    @Override
    public String toString() {
        return "ProjectList{" +
                "meta=" + meta +
                ", objects=" + objects +
                '}';
    }
}
