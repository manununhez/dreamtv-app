package com.dream.dreamtv.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.utils.LocaleHelper;

import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_UPDATE_USER;
import static com.dream.dreamtv.utils.Constants.LANGUAGE_POLISH;

public class AppPreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppPreferencesViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_app);


        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(AppPreferencesViewModel.class);

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);

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
            mViewModel.testingModeModified();
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
        mViewModel.saveUserDataAppPreferences();

        goBackToHomeAndReturnValues();

        mViewModel.saveUserDataAppPreferencesCompleted();

        super.onBackPressed();

    }

    private void goBackToHomeAndReturnValues() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_RESTART, mViewModel.restart.getValue());
        returnIntent.putExtra(INTENT_EXTRA_UPDATE_USER, mViewModel.updateUser.getValue());
        returnIntent.putExtra(INTENT_EXTRA_TESTING_MODE, mViewModel.testingMode.getValue());
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
