package com.dream.dreamtv.ui.Settings;

import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

import androidx.lifecycle.ViewModel;

class SettingsViewModel extends ViewModel {

    private final AppRepository mRepository;


    SettingsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    void updateUser(User user) {
        mRepository.updateUser(user);
    }

}

