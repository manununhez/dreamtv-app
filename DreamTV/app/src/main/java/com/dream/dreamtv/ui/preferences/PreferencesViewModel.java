package com.dream.dreamtv.ui.preferences;

import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.repository.AppRepository;

public class PreferencesViewModel extends ViewModel {
    private final AppRepository mRepository;

    public PreferencesViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    public User getUser() {
        return mRepository.getUser();
    }

    public void setUser(User user) {
        mRepository.setUser(user);
    }

    String getAudioLanguagePref() {
        return mRepository.getAudioLanguagePref();
    }

    String getInterfaceAppLanguage() {
        return mRepository.getInterfaceAppLanguage();
    }

    String getInterfaceMode() {
        return mRepository.getInterfaceMode();
    }
}
