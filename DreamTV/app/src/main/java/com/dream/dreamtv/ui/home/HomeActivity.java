/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dream.dreamtv.ui.home;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ui.home.dialogs.ExitDialogFragment;
import com.dream.dreamtv.utils.LocaleHelper;

import timber.log.Timber;


/*
 * HomeActivity class that loads HomeFragment
 */
public class HomeActivity extends FragmentActivity implements ExitDialogFragment.OnListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed()");
        showExitDialog();
//        super.onBackPressed();
    }


    private void showExitDialog() {
        ExitDialogFragment ratingDialogFragment = new ExitDialogFragment();
        if (!isFinishing()) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            ratingDialogFragment.show(transaction, "Sample Fragment");
        }

    }

    @Override
    public void exit() {
        super.onBackPressed();
    }
}
