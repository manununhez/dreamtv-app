package com.dream.dreamtv.ui.Settings;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.AbsentLiveData;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

class SettingsViewModel extends ViewModel {

    private final AppRepository mRepository;
    private final MutableLiveData<User> userMutableLiveData;
    private final LiveData<Resource<User>> user;

    SettingsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        userMutableLiveData = new MutableLiveData<>();

        user = Transformations.switchMap(userMutableLiveData, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return mRepository.updateUser(input);
        });
    }


    LiveData<Resource<User>> updateUser(User userData) {
        userMutableLiveData.setValue(userData);

        return user;
    }

}

