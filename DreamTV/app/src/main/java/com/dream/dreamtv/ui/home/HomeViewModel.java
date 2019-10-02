package com.dream.dreamtv.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Category.Type;
import com.dream.dreamtv.data.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.VideoTopic;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.repository.AppRepository;

public class HomeViewModel extends ViewModel {

    private final AppRepository mRepository;
    private LiveData<Resource<VideoTopic[]>> categoriesLD;
    private LiveData<Resource<TasksList[]>> tasksLD;

    public HomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    void initSyncData() {
        //initSyncData
        mRepository.fetchReasons();
        mRepository.fetchVideoTestsDetails();
        categoriesLD = mRepository.fetchCategories();

        syncTasks();
    }


    void syncTasks() {
        tasksLD = mRepository.fetchTasks();
    }


    boolean getTestingMode() {
        return mRepository.getTestingModePref();
    }

    void updateUser(User userData) {
        mRepository.updateUser(userData);
    }

    User getUser() {
        return mRepository.getUser();
    }

    String getSubtitleSize() {
        return mRepository.getSubtitleSizePref();
    }

    VideoDuration getVideoDuration() {
        return mRepository.getVideoDurationPref();
    }

    LiveData<Resource<VideoTopic[]>> fetchCategories() {
        return categoriesLD;
    }

    LiveData<Resource<TasksList[]>> fetchTasks() {
        return tasksLD;
    }


}

