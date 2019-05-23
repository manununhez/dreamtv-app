package com.dream.dreamtv.ui.Main;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.AbsentLiveData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

class MainViewModel extends ViewModel {

    private final AppRepository mRepository;

    MainViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }


    void login(String email, String password) {
        //TODO login and transformation according to user data and call initialize syncData
        mRepository.login(email, password);
    }

    void initializeSyncData() {
        mRepository.initializeSyncData();
    }

    LiveData<Resource<TaskEntity[]>> requestTasksByCategory(String category) {
        //TODO should this change with Transformations after login or user update?
        return mRepository.requestTaskByCategory(category);
    }


}

