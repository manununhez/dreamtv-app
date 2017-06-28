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

package com.dream.dreamtv.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.activity.DetailsActivity;
import com.dream.dreamtv.beans.Video;

/**
 * TODO: Javadoc
 */
public class DialogChooseLanguageActivity extends Activity {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#21272A")));

        Video mSelectedVideo = (Video) getIntent()
                .getParcelableExtra(DetailsActivity.VIDEO);

        if (savedInstanceState == null) {
            GuidedStepFragment fragment = DialogChooseLanguageFragment.newInstance(mSelectedVideo);
            GuidedStepFragment.addAsRoot(this, fragment, android.R.id.content);
        }
    }

}
