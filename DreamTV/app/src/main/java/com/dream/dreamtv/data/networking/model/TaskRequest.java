package com.dream.dreamtv.data.networking.model;

import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.VideoDuration;

public class TaskRequest {

    private Category.Type mCategory;
    private VideoDuration mVideoDuration;

    public TaskRequest(Category.Type category, VideoDuration videoDuration) {
        mCategory = category;
        mVideoDuration = videoDuration;
    }

    public Category.Type getCategory() {
        return mCategory;
    }

    public void setCategory(Category.Type mCategory) {
        this.mCategory = mCategory;
    }

    public VideoDuration getVideoDuration() {
        return mVideoDuration;
    }

    public void setVideoDuration(VideoDuration mVideoDuration) {
        this.mVideoDuration = mVideoDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskRequest that = (TaskRequest) o;

        if (mCategory != that.mCategory) return false;
        return mVideoDuration.equals(that.mVideoDuration);
    }

    @Override
    public int hashCode() {
        int result = mCategory.hashCode();
        result = 31 * result + mVideoDuration.hashCode();
        return result;
    }
}
