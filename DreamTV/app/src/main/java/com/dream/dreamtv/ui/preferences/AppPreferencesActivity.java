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

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_UPDATE_USER;
import static com.dream.dreamtv.utils.Constants.LANGUAGE_POLISH;

public class AppPreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean restart = false;
    private boolean updateUser = false;
    private boolean testingMode = false;
    private PreferencesViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_app);

        if (savedInstanceState != null) {
            restart = savedInstanceState.getBoolean(INTENT_EXTRA_RESTART);
        }

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PreferencesViewModel.class);

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);

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
        if (key.equals(getString(R.string.pref_key_list_app_languages))) {
            LocaleHelper.setLocale(this, sharedPreferences.getString(key, LANGUAGE_POLISH));
        }

        if (key.equals(getString(R.string.pref_key_testing_mode))) {
            testingMode = true;
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
        returnIntent.putExtra(INTENT_EXTRA_TESTING_MODE, testingMode);
        setResult(RESULT_OK, returnIntent);
        finish();

        super.onBackPressed();

    }

    /**
     * This function obtains the user cached data, update its interface language and interface mode,
     * and save the new user data (local and remote).
     * In case the audio language has been changed, the app will restart in order to apply the new settings.
     */
    private void saveUserData() {
        //Save user data
        User user = mViewModel.getUser();

        String previousInterfaceLanguage = user.getSubLanguage();
        String previousInterfaceMode = user.getInterfaceMode();

        String currentInterfaceLanguage = mViewModel.getInterfaceAppLanguage();
        String currentInterfaceMode = mViewModel.getInterfaceMode();

        restart = !previousInterfaceLanguage.equals(currentInterfaceLanguage);
        updateUser = restart || !previousInterfaceMode.equals(currentInterfaceMode);

        //update app interface language - subtitle language
        user.setSubLanguage(currentInterfaceLanguage);

        //update interface mode
        user.setInterfaceMode(currentInterfaceMode);

        if (updateUser)
            //caching the updated user
            mViewModel.setUser(user);


    }
}
