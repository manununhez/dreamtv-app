package com.dream.dreamtv.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.fragment.app.FragmentActivity;

import com.dream.dreamtv.R;
import com.dream.dreamtv.utils.LocaleHelper;

public class SearchActivity extends FragmentActivity {
    private SearchFragment mFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        mFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onSearchRequested() {
        if (mFragment.hasResults()) {
            startActivity(new Intent(this, SearchActivity.class));
        } else {
            mFragment.startRecognition();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // If there are no results found, press the left key to reselect the microphone
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && !mFragment.hasResults()) {
            mFragment.focusOnSearch();
        }
        return super.onKeyDown(keyCode, event);
    }
}

