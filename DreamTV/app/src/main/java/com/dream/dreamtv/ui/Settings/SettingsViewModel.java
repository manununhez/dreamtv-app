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
    private final MutableLiveData<User> liveUser;
    private final LiveData<Resource<User>> user;

    SettingsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        this.liveUser = new MutableLiveData<>();

        user = Transformations.switchMap(liveUser, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return mRepository.updateUser(input);
        });
    }


    void updateUser(User user) {
        liveUser.setValue(user);
    }

    LiveData<Resource<User>> getUserUpdated() {
        return user;
    }

}

