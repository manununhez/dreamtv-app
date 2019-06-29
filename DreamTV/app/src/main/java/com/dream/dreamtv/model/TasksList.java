package com.dream.dreamtv.model;

import java.util.Arrays;

/**
 * Created by manuel on 6/12/17.
 */

public class TasksList {
    public int current_page;
    public Task[] data;
    public int from;
    public int last_page;
    public int total;
    public int to;
    public String category;

    @Override
    public String toString() {
        return "TasksList{" +
                "current_page=" + current_page +
                ", data=" + Arrays.toString(data) +
                ", from=" + from +
                ", last_page=" + last_page +
                ", total=" + total +
                ", to=" + to +
                ", category='" + category + '\'' +
                '}';
    }
}
