package com.dream.dreamtv.ui.VideoDetails;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;


    public VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    public void requestFromLogin(String email, String password) {
        mRepository.requestFromLogin(email, password);
    }

    public LiveData<Resource<User>> responseFromUserUpdate() {
        return mRepository.responseFromUserUpdate();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromTasks() {
        return mRepository.responseFromTasks();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromContinueTasks() {
        return mRepository.responseFromContinueTasks();
    }

    public LiveData<Resource<String>> responseFromSyncData() {
        return mRepository.responseFromSyncData();
    }


    public void requestSyncData() {
        mRepository.requestSyncData();
    }


    public LiveData<TaskEntity[]> requestAllTasks() {
        return mRepository.requestAllTasks();
    }

    public LiveData<TaskEntity[]> requestContinueTasks() {
        return mRepository.requestContinueTasks();
    }
}

