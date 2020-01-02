package com.dreamproject.dreamtv.ui.preferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dreamproject.dreamtv.data.model.User;
import com.dreamproject.dreamtv.repository.AppRepository;

public class AppPreferencesViewModel extends ViewModel {
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

    private final MutableLiveData<Boolean> _testingMode = new MutableLiveData<>();
    final LiveData<Boolean> testingMode = new LiveData<Boolean>() {
        @Nullable
        @Override
        public Boolean getValue() {
            return _testingMode.getValue();
        }
    };

    private final AppRepository mRepository;


    public AppPreferencesViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        _restart.setValue(false);
        _updateUser.setValue(false);
        _testingMode.setValue(false);
    }

    User getUser() {
        return mRepository.getUser();
    }

    void setUser(User user) {
        mRepository.setUser(user);
    }

    String getInterfaceAppLanguage() {
        return mRepository.getInterfaceAppLanguage();
    }

    String getInterfaceMode() {
        return mRepository.getInterfaceMode();
    }

    /**
     * This function obtains the user cached data, update its interface language and interface mode,
     * and save the new user data (local and remote).
     * In case the audio language has been changed, the app will restart in order to apply the new settings.
     */
    void saveUserDataAppPreferences() {
        //Save user data
        User user = getUser();

        String previousInterfaceLanguage = user.getSubLanguage();
        String previousInterfaceMode = user.getInterfaceMode();

        String currentInterfaceLanguage = getInterfaceAppLanguage();
        String currentInterfaceMode = getInterfaceMode();

        _restart.setValue(!previousInterfaceLanguage.equals(currentInterfaceLanguage));
        _updateUser.setValue((_restart.getValue() == null ? false : _restart.getValue())
                || !previousInterfaceMode.equals(currentInterfaceMode));

        //update app interface language - subtitle language
        user.setSubLanguage(currentInterfaceLanguage);

        //update interface mode
        user.setInterfaceMode(currentInterfaceMode);

        if (_updateUser.getValue() == null ? false : _updateUser.getValue()) {
            //caching the updated user
            setUser(user);
        }
    }

    void testingModeModified() {
        _testingMode.setValue(true);
    }

    void saveUserDataAppPreferencesCompleted() {
        _testingMode.setValue(false);
        _updateUser.setValue(false);
        _restart.setValue(false);
    }
}
