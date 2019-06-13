package com.dream.dreamtv.ui.Main;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

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

    LiveData<Resource<TasksList>> requestTasksByCategory(String category) {
        //TODO should this change with Transformations after login or user update?
        return mRepository.requestTaskByCategory(category);
    }


    void updateTaskByCategory(String category) {
        mRepository.updateTasksCategory(category);
    }

    LiveData<Resource<User>> updateUser(User userData) {
        return mRepository.updateUser(userData);
    }

}

