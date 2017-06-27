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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.Video;

import java.util.List;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;

/**
 * YoutubeActivityApiShowcase
 *
 * @author Bertrand Martel
 */
public class YoutubeActivityApiShowcase extends Activity {

    private final static String TAG = YoutubeActivityApiShowcase.class.getSimpleName();

    private YoutubeTvView mYoutubeView1;
    private Chronometer tvTime;
    private TextView tvSubtitle;
    private Handler handler;
    private Runnable myRunnable;
    private Video mSelectedVideo;
    private final static int POSITION_OFFSET = 5;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_showcase);

        tvTime = new Chronometer(this); // initiate a chronometer
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle); // initiate a chronometer

        mYoutubeView1 = (YoutubeTvView) findViewById(R.id.video_1);
        mYoutubeView1.updateView(getArgumentos());

        mYoutubeView1.playVideo(getArgumentos().getString("videoId", ""));

        Button playBtn = (Button) findViewById(R.id.play_button);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.start();
            }
        });
        Button pauseBtn = (Button) findViewById(R.id.pause_button);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.pause();
            }
        });
        Button nextBtn = (Button) findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.nextVideo();
            }
        });
        Button previousBtn = (Button) findViewById(R.id.previous_button);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.previousVideo();
            }
        });

        Button backwardBtn = (Button) findViewById(R.id.backward_button);
        backwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.moveBackward(POSITION_OFFSET);
            }
        });
        Button forwardBtn = (Button) findViewById(R.id.forward_button);
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYoutubeView1.moveForward(POSITION_OFFSET);
            }
        });


        mYoutubeView1.addPlayerListener(new IPlayerListener() {
            @Override
            public void onPlayerReady(final VideoInfo videoInfo) {
                Log.i(TAG, "onPlayerReady");
            }

            @Override
            public void onPlayerStateChange(final VideoState state,
                                            final long position,
                                            final float speed,
                                            final float duration,
                                            final VideoInfo videoInfo) {

                if (state.toString().equals("PLAYING")) {
                    tvTime.setBase(SystemClock.elapsedRealtime() - position);
                    tvTime.start();
                    handler.post(myRunnable);
                } else {
                    tvTime.stop();
                    handler.removeCallbacksAndMessages(null);
                }

                Log.i(TAG, "onPlayerStateChange : " + state.toString() + " | position : " + position + " | speed : " + speed);
            }
        });

        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = getSyncSubtitleText(SystemClock.elapsedRealtime() - tvTime.getBase());
                        if (text.isEmpty())
                            tvSubtitle.setVisibility(View.GONE);
                        else {
                            tvSubtitle.setVisibility(View.VISIBLE);
                            tvSubtitle.setText(Html.fromHtml(text));
                        }
                    }
                });

                handler.postDelayed(myRunnable, 500);
            }
        };


    }

    private String getSyncSubtitleText(long l) {
        List<Subtitle> subtitleList = mSelectedVideo.subtitle_json.subtitles;
        String text = "";
        for (Subtitle subtitle : subtitleList) {
            if (l >= subtitle.start && l < subtitle.end) { //esta adentro del ciclo
                text = subtitle.text;
                break;
            } else if (l < subtitle.start)
                break;

        }

        return text;
    }

    private Bundle getArgumentos() {
        mSelectedVideo = getIntent().getParcelableExtra(DetailsActivity.VIDEO);
        Bundle args = new Bundle();
        args.putString("videoId", mSelectedVideo.getVideoYoutubeId());
//        args.putString("closedCaptionLangPref", video.languages.get(0).code);
//        args.putString("videoQuality", "hd1080");
        args.putBoolean("debug", false);
        args.putBoolean("closedCaptions", false);
        args.putBoolean("showRelatedVideos", false);

        return args;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mYoutubeView1.closePlayer();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
