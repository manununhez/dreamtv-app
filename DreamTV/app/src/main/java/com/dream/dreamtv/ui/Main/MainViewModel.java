package com.dream.dreamtv.ui.Main;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class MainViewModel extends ViewModel {

    private final AppRepository mRepository;

    MainViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    void login(String email, String password) {
        mRepository.login(email, password);
    }

    void initializeSyncData() {
        mRepository.initializeSyncData();
    }

    LiveData<Resource<User>> responseFromUserUpdate() {
        return mRepository.responseFromUserUpdate();
    }

    LiveData<TaskEntity[]> requestTasksByCategory(String category) {//TODO add Resource to taskEntity
        return mRepository.requestTaskByCategory(category);
    }

}

