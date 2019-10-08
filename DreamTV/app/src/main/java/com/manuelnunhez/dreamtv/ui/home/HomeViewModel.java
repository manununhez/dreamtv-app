package com.manuelnunhez.dreamtv.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.manuelnunhez.dreamtv.data.model.TasksList;
import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.data.model.VideoDuration;
import com.manuelnunhez.dreamtv.data.model.VideoTopic;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.repository.AppRepository;

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

