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

package com.dream.dreamtv.presenter;

import android.app.Activity;
import android.content.Context;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.utils.Utils;


public class DetailsDescriptionPresenter extends CustomAbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Task task = ((Task) item);
        Video video = task.video;

        Context context = viewHolder.view.getContext();

        User user = ((DreamTVApp) ((Activity) context).getApplication()).getUser();

        if (video != null) {
            String timeFormatted = Utils.getTimeFormatMinSecs(video.duration * 1000);

            viewHolder.getTitle().setText(video.title);
            viewHolder.getSubtitle().setText(context.getString(R.string.title_video_details, video.project,
                    video.primaryAudioLanguageCode, task.language, timeFormatted, user.interfaceMode));
            viewHolder.getBody().setText(video.description);
        }
    }
}
