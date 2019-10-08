package com.manuelnunhez.dreamtv.ui.preferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.repository.AppRepository;

public class VideoPreferencesViewModel extends ViewModel {
    private final MutableLiveData<Boolean> _restart = new MutableLiveData<>();
    final LiveData<Boolean> restart = new LiveData<Boolean>() {
        @Nullable
        @Override
        public Boolean getValue() {
            return _restart.getValue();
        }
    };
    private final MutableLiveData<Boolean> _updateUser = new MutableLiveData<>();
    final LiveData<Boolean> updateUser = new LiveData<Boolean>() {
        @Nullable
        @Override
        public Boolean getValue() {
            return _updateUser.getValue();
        }
    };
    private final MutableLiveData<Boolean> _callAllTaskAgain = new MutableLiveData<>();
    final LiveData<Boolean> callAllTaskAgain = new LiveData<Boolean>() {
        @Nullable
        @Override
        public Boolean getValue() {
            return _callAllTaskAgain.getValue();
        }
    };

    private final AppRepository mRepository;


    public VideoPreferencesViewModel(AppRepository appRepository) {
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

    /**
     * This function obtains the user cached data and update its audio language.
     * In case the audio language has been changed, the app will restart in order to apply the new settings,
     * and save the new user data (local and remote).
     */
    void saveUserDataVideoPreferences() {
        //Save user data
        User user = getUser();

        String previousAudioLanguage = user.getAudioLanguage();

        String currentAudioLanguage = getAudioLanguagePref();

        _restart.setValue(!previousAudioLanguage.equals(currentAudioLanguage));
        _updateUser.setValue(_restart.getValue());

        //update audiolanguage
        user.setAudioLanguage(currentAudioLanguage);

        if (_updateUser.getValue() == null ? false : _updateUser.getValue())
            //caching the updated user
            setUser(user);

    }

    void listVideoDurationModified() {
        _callAllTaskAgain.setValue(true);
    }

    void saveUserDataVideoPreferencesCompleted() {
        _callAllTaskAgain.setValue(false);
        _updateUser.setValue(false);
        _restart.setValue(false);
    }
}
