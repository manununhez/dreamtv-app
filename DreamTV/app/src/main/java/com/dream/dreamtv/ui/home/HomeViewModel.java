package com.dream.dreamtv.ui.home;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.api.VideoTopic;
import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.TasksList;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.repository.AppRepository;

public class HomeViewModel extends ViewModel {

    private final AppRepository mRepository;

    public HomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<TasksList>> requestTasksByCategory(Category.Type category) {
        //TODO should this change with Transformations after login or user update?
        return mRepository.requestTaskByCategory(category);
    }

    LiveData<Resource<VideoTopic[]>> fetchCategories(){
        return mRepository.fetchCategories();
    }

    LiveData<Resource<User>> updateUser(User userData) {
        return mRepository.updateUser(userData);
    }

    void updateTaskByCategory(Category.Type category) {
        mRepository.updateTasksCategory(category);
    }

    void fetchReasons() {
        mRepository.fetchReasons();
    }

    void fetchVideoTestsDetails() {
        mRepository.fetchVideoTestsDetails();
    }


    boolean getTestingMode(){
        return mRepository.getTestingModePref();
    }

    public User getUser() {
        User user = mRepository.getUser();
        Log.d("HomeViewModel", "Update user data - setUser() called: subLanguage = " + user.subLanguage);

        return user;
    }
}

