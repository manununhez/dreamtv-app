package com.dream.dreamtv.ui.preferences;

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

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;
import static com.dream.dreamtv.utils.Constants.PREF_ABR_POLISH;

public class SubtitlePreferencesActivity extends FragmentActivity {
    private boolean restart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_subtitle);

        if (savedInstanceState != null) {
            restart = savedInstanceState.getBoolean(INTENT_EXTRA_RESTART);
        }

        PreferenceManager.setDefaultValues(this, R.xml.subtitle_preferences, false);

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
//        super.attachBaseContext(LocaleHelper.onAttach(base, PREF_ABR_POLISH));
    }


    @Override
    public void onBackPressed() {
        User userUpdated = saveUserData();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_RESTART, restart);
        returnIntent.putExtra(INTENT_EXTRA_USER_UPDATED, userUpdated);
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();

    }


    private User saveUserData() {

//            pref_key_list_subtitle_languages
//            pref_key_subtitle_size

        //Save user data
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        User userCached = ((DreamTVApp) getApplication()).getUser();

        String subtitleLanguage = sharedPref.getString(getString(R.string.pref_key_list_subtitle_languages), PREF_ABR_POLISH);

        User userUpdated = new User();
        userUpdated.email = userCached.email;
        userUpdated.password = userCached.password;
        userUpdated.audioLanguage = userCached.audioLanguage;
        userUpdated.interfaceMode = userCached.interfaceMode;
        userUpdated.interfaceLanguage = userCached.interfaceLanguage;
        userUpdated.subLanguage = subtitleLanguage; //subtitle updated


        restart = !userCached.subLanguage.equals(subtitleLanguage);

        return userUpdated;
    }
}
