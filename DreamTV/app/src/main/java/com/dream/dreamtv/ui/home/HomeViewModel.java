package com.dream.dreamtv.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Category;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

class HomeViewModel extends ViewModel {

    private final AppRepository mRepository;

    HomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<TasksList>> requestTasksByCategory(String category) {
        //TODO should this change with Transformations after login or user update?
        return mRepository.requestTaskByCategory(category);
    }

    LiveData<Resource<Category[]>> fetchCategories(){
        return mRepository.fetchCategories();
    }

    LiveData<Resource<User>> updateUser(User userData) {
        return mRepository.updateUser(userData);
    }

    void updateTaskByCategory(String category) {
        mRepository.updateTasksCategory(category);
    }

    void fetchReasons() {
        mRepository.fetchReasons();
    }

    void fetchVideoTestsDetails() {
        mRepository.fetchVideoTestsDetails();
    }

}

