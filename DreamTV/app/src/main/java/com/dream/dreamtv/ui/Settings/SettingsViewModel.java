package com.dream.dreamtv.ui.Settings;


import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class SettingsViewModel extends ViewModel {

    private final AppRepository mRepository;


    public SettingsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    public void requestUserUpdate(User user) {
        mRepository.requestFromUserUpdate(user);
    }

    public LiveData<Resource<User>> responseFromUserUpdate() {
        return mRepository.responseFromUserUpdate();
    }

}

