package com.dream.dreamtv.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Category.Type;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.networking.model.VideoTopicSchema;
import com.dream.dreamtv.repository.AppRepository;

public class HomeViewModel extends ViewModel {

    private final AppRepository mRepository;

    public HomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    void initSyncData() {
        mRepository.fetchReasons();
        mRepository.fetchVideoTestsDetails();

        syncAllCategories();
    }

    void syncAllCategories() {
        mRepository.updateTasksCategory(Type.ALL);
        mRepository.updateTasksCategory(Type.FINISHED);
        mRepository.updateTasksCategory(Type.CONTINUE);
        mRepository.updateTasksCategory(Type.MY_LIST);

        if (getTestingMode())
            mRepository.updateTasksCategory(Type.TEST);
    }

    void updateTaskByCategory(Type category) {
        mRepository.updateTasksCategory(category);
    }


    boolean getTestingMode() {
        return mRepository.getTestingModePref();
    }

    public User getUser() {
        return mRepository.getUser();
    }

    LiveData<Resource<TasksList>> requestTasksByCategory(Type category) {
        //TODO should this change with Transformations after login or user update?
        return mRepository.requestTaskByCategory(category);
    }

    LiveData<Resource<VideoTopicSchema[]>> fetchCategories() {
        return mRepository.fetchCategories();
    }

    LiveData<Resource<User>> updateUser(User userData) {
        return mRepository.updateUser(userData);
    }


    public String getSubtitleSize() {
       return mRepository.getSubtitleSizePref();
    }

    public VideoDuration getVideoDuration() {
        return mRepository.getVideoDurationPref();
    }
}

