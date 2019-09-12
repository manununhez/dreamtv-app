package com.dream.dreamtv.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.networking.model.User;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.utils.LocaleHelper;

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;

public class VideoPreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean restart = false;
    private boolean callAllTaskAgain = false;
    private PreferencesViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_video);

        if (savedInstanceState != null) {
            restart = savedInstanceState.getBoolean(INTENT_EXTRA_RESTART);
        }

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PreferencesViewModel.class);

        PreferenceManager.setDefaultValues(this, R.xml.video_preferences, false);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INTENT_EXTRA_RESTART, restart);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restart = savedInstanceState.getBoolean(INTENT_EXTRA_RESTART);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_video_duration))) {
            callAllTaskAgain = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        User userUpdated = saveUserData();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_RESTART, restart);
        returnIntent.putExtra(INTENT_EXTRA_CALL_TASKS, callAllTaskAgain);
        returnIntent.putExtra(INTENT_EXTRA_USER_UPDATED, userUpdated);
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();

    }


    private User saveUserData() {
        //   pref_key_video_duration
        //   pref_key_list_audio_languages

        //Save user data
        User userCached = mViewModel.getUser();

        String audioLanguage = mViewModel.getAudioLanguagePref();


        User userUpdated = new User();
        userUpdated.email = userCached.email;
        userUpdated.password = userCached.password;
        userUpdated.subLanguage = userCached.subLanguage;
        userUpdated.interfaceMode = userCached.interfaceMode;
//        userUpdated.interfaceLanguage = userCached.interfaceLanguage;
        userUpdated.audioLanguage = audioLanguage; //audio updated


        restart = !userCached.audioLanguage.equals(audioLanguage);

        return userUpdated;
    }
}
