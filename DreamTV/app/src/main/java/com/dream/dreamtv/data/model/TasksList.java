package com.dream.dreamtv.data.model;


/**
 * Created by manuel on 6/12/17.
 */

public class TasksList {
    private Task[] tasks;
    private Category category;

    public TasksList(Task[] tasks, Category category) {

        this.tasks = tasks;
        this.category = category;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public void setTasks(Task[] tasks) {
        this.tasks = tasks;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
