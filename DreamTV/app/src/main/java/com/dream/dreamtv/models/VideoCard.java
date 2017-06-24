/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.dream.dreamtv.models;


import com.google.gson.annotations.SerializedName;


public class VideoCard extends Card {

    @SerializedName("sources") private String mVideoSource = "";
    @SerializedName("background") private String mBackgroundUrl = "";
    @SerializedName("studio") private String mStudio = "";

    public VideoCard() {
        super();
        setType(Type.VIDEO_GRID);
    }

    public String getVideoSource() {
        return mVideoSource;
    }

    public void setVideoSource(String sources) {
        mVideoSource = sources;
    }

    public String getBackground() {
        return mBackgroundUrl;
    }

    public void setBackground(String background) {
        mBackgroundUrl = background;
    }

    public String getStudio() {
        return mStudio;
    }

    public void setStudio(String studio) {
        mStudio = studio;
    }
}
