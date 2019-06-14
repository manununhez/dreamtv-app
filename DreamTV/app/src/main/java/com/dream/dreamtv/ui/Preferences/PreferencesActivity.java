package com.dream.dreamtv.ui.Preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.utils.LocaleHelper;

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;
import static com.dream.dreamtv.utils.Constants.PREF_ABR_POLISH;
import static com.dream.dreamtv.utils.Constants.PREF_BEGINNER_INTERFACE_MODE;

public class PreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean restart = false;
    private boolean callAllTaskAgain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        if (savedInstanceState != null) {
            restart = savedInstanceState.getBoolean(INTENT_EXTRA_RESTART);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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
        super.attachBaseContext(LocaleHelper.onAttach(base, PREF_ABR_POLISH));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_list_app_languages))) {
            LocaleHelper.setLocale(this, sharedPreferences.getString(key, PREF_ABR_POLISH));
//            recreate();

        } else if (key.equals(getString(R.string.pref_key_video_duration))) {

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
        //Save user data
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        User userCached = ((DreamTVApp) getApplication()).getUser();

        String interfaceLanguage = sharedPref.getString(getString(R.string.pref_key_list_app_languages), PREF_ABR_POLISH);
        String audioLanguage = sharedPref.getString(getString(R.string.pref_key_list_audio_languages), PREF_ABR_POLISH);
        String subtitleLanguage = sharedPref.getString(getString(R.string.pref_key_list_subtitle_languages), PREF_ABR_POLISH);
        String interfaceMode = sharedPref.getString(getString(R.string.pref_key_list_interface_mode), PREF_BEGINNER_INTERFACE_MODE);


        User userUpdated = new User();
        userUpdated.email = userCached.email;
        userUpdated.password = userCached.password;
        userUpdated.subLanguage = subtitleLanguage;
        userUpdated.audioLanguage = audioLanguage;
        userUpdated.interfaceMode = interfaceMode; //interface mode updated
        userUpdated.interfaceLanguage = interfaceLanguage; //interface language updated


        restart = !userCached.interfaceLanguage.equals(interfaceLanguage) ||
                !userCached.audioLanguage.equals(audioLanguage) ||
                !userCached.subLanguage.equals(subtitleLanguage);

        return userUpdated;
    }
}
