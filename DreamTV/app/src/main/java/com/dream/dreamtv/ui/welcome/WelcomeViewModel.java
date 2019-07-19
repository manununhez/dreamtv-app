package com.dream.dreamtv.ui.welcome;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

class WelcomeViewModel extends ViewModel {

    private final AppRepository mRepository;

    WelcomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    void login(String email, String password) {
        //TODO login and transformation according to user data and call initialize syncData
        mRepository.login(email, password);
    }

    LiveData<Resource<User>> fetchUserDetails() {
        return mRepository.fetchUserDetails();
    }

}

