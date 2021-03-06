package com.dreamproject.dreamtv.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.dreamproject.dreamtv.R;
import com.dreamproject.dreamtv.ViewModelFactory;
import com.dreamproject.dreamtv.di.InjectorUtils;
import com.dreamproject.dreamtv.utils.LocaleHelper;

import static com.dreamproject.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dreamproject.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dreamproject.dreamtv.utils.Constants.INTENT_EXTRA_UPDATE_USER;

public class VideoPreferencesActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private VideoPreferencesViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_video);


        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, factory).get(VideoPreferencesViewModel.class);

        PreferenceManager.setDefaultValues(this, R.xml.video_preferences, false);

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_video_duration))) {
            mViewModel.listVideoDurationModified();
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
        mViewModel.saveUserDataVideoPreferences();

        goBackToHomeAndReturnValues();

        mViewModel.saveUserDataVideoPreferencesCompleted();

        super.onBackPressed();

    }

    private void goBackToHomeAndReturnValues() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_RESTART, mViewModel.restart.getValue());
        returnIntent.putExtra(INTENT_EXTRA_UPDATE_USER, mViewModel.updateUser.getValue());
        returnIntent.putExtra(INTENT_EXTRA_CALL_TASKS, mViewModel.callAllTaskAgain.getValue());
        setResult(RESULT_OK, returnIntent);
        finish();
    }


}
