package com.dream.dreamtv.ui.Settings;

import android.content.Context;
import android.os.Bundle;

import com.dream.dreamtv.R;
import com.dream.dreamtv.utils.LocaleHelper;

import androidx.fragment.app.FragmentActivity;

public class SettingsActivity extends FragmentActivity {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    }

}
