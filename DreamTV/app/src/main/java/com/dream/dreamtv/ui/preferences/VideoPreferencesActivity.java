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
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.utils.LocaleHelper;

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_UPDATE_USER;

public class VideoPreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean restart = false;
    private boolean callAllTaskAgain = false;
    private boolean updateUser = false;
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
        saveUserData();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_RESTART, restart);
        returnIntent.putExtra(INTENT_EXTRA_UPDATE_USER, updateUser);
        returnIntent.putExtra(INTENT_EXTRA_CALL_TASKS, callAllTaskAgain);
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();

    }

    /**
     * This function obtains the user cached data and update its audio language.
     * In case the audio language has been changed, the app will restart in order to apply the new settings,
     * and save the new user data (local and remote).
     */
    private void saveUserData() {
        //Save user data
        User user = mViewModel.getUser();

        String previousAudioLanguage = user.getAudioLanguage();

        String currentAudioLanguage = mViewModel.getAudioLanguagePref();

        restart = !previousAudioLanguage.equals(currentAudioLanguage);
        updateUser = restart;

        //update audiolanguage
        user.setAudioLanguage(currentAudioLanguage);

        if (updateUser)
            //caching the updated user
            mViewModel.setUser(user);

    }
}
