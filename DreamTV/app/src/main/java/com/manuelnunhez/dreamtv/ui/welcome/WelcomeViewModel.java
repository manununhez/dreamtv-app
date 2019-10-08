package com.manuelnunhez.dreamtv.ui.welcome;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.repository.AppRepository;

public class WelcomeViewModel extends ViewModel {

    private final AppRepository mRepository;

    public WelcomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    void login(String email, String password) {
        //TODO login and transformation according to user data and call initialize syncData
        mRepository.login(email, password);
    }

    String getAccessToken() {
        return mRepository.getAccessToken();
    }

    public User getUser() {
        return mRepository.getUser();
    }

    LiveData<Resource<User>> fetchUserDetails() {
        return mRepository.fetchUserDetails();
    }
}

