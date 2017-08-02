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

package com.dream.dreamtv.adapter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.dream.dreamtv.beans.Video;


public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        if (video != null) {
            viewHolder.getTitle().setText(video.title);
            viewHolder.getSubtitle().setText(video.project + " | AUDIO: " +
                    video.primary_audio_language_code + " | SUBT: " +
                    video.subtitle_language + " | Duration:" +
                    (video.duration / 60) + " min.");
            viewHolder.getBody().setText(video.description);
        }
    }
}
