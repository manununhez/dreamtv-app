/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dream.dreamtv.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Video;

import fr.bmartel.youtubetv.YoutubeTvFragment;

/**
 * Created by bertrandmartel on 03/11/16.
 */
public class YoutubeActivityFragment extends Activity {

    private YoutubeTvFragment mYtFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        FragmentTransaction fTransaction = getFragmentManager().beginTransaction();

        Video video = getIntent().getParcelableExtra(DetailsActivity.VIDEO);
        Bundle args = new Bundle();
        args.putString("videoId", video.getVideoYoutubeId());
//        args.putString("closedCaptionLangPref", video.languages.get(0).code);
//        args.putString("videoQuality", "hd1080");
        args.putBoolean("debug", true);
        args.putBoolean("closedCaptions", true);
        args.putBoolean("showRelatedVideos", true);

        mYtFragment = YoutubeTvFragment.newInstance(args);
        fTransaction.replace(R.id.youtube_fragment, mYtFragment);
        fTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mYtFragment.closePlayer();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
