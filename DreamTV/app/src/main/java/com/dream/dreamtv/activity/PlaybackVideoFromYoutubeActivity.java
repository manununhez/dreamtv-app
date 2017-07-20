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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.fragment.PlaybackVideoFragment;
import com.dream.dreamtv.fragment.ReasonsDialogFragment;
import com.dream.dreamtv.fragment.VideoDetailsFragment;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;

/**
 * YoutubeActivityApiShowcase
 *
 * @author Bertrand Martel
 */
public class PlaybackVideoFromYoutubeActivity extends Activity implements
        PlaybackVideoFragment.OnPlayPauseClickedListener {

    private final static String TAG = PlaybackVideoFromYoutubeActivity.class.getSimpleName();
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;

    //    private LinearLayout llReasons;
    private YoutubeTvView mYoutubeView1;
    private TextView tvSubtitle;
    private Handler handler;
    private Chronometer tvTime;
    private Runnable myRunnable;
    private Video mSelectedVideo;
    //    private final static int POSITION_OFFSET = 5;
    private MediaSession mSession;
    //    private PlaybackVideoFragment playbackVideoFragment;
    private Long elapsedRealtimeTemp;
    private Long timeStoppedTemp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_youtube);

        tvTime = new Chronometer(this); // initiate a chronometer
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle); // initiate a chronometer
//        llReasons = (LinearLayout) findViewById(R.id.llReasons); // initiate a chronometer

        mYoutubeView1 = (YoutubeTvView) findViewById(R.id.video_1);
        mYoutubeView1.updateView(getArgumentos());

        mYoutubeView1.playVideo(getArgumentos().getString("videoId", ""));


//        Button playBtn = (Button) findViewById(R.id.play_button);
//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.start();
//            }
//        });
//        Button pauseBtn = (Button) findViewById(R.id.pause_button);
//        pauseBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.pause();
//            }
//        });
//        Button nextBtn = (Button) findViewById(R.id.next_button);
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.nextVideo();
//            }
//        });
//        Button previousBtn = (Button) findViewById(R.id.previous_button);
//        previousBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.previousVideo();
//            }
//        });
//
//        Button backwardBtn = (Button) findViewById(R.id.backward_button);
//        backwardBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.moveBackward(POSITION_OFFSET);
//            }
//        });
//        Button forwardBtn = (Button) findViewById(R.id.forward_button);
//        forwardBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView1.moveForward(POSITION_OFFSET);
//            }
//        });


        mYoutubeView1.addPlayerListener(new IPlayerListener() {
            @Override
            public void onPlayerReady(final VideoInfo videoInfo) {
//                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
////                    mYoutubeView1.start();
//                }

//                Toast.makeText(PlaybackVideoFromYoutubeActivity.this, "onPlayerReady", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPlayerStateChange(final VideoState state,
                                            final long position,
                                            final float speed,
                                            final float duration,
                                            final VideoInfo videoInfo) {

                PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

                if (state.toString().equals("PLAYING")) {
                    tvTime.setBase(SystemClock.elapsedRealtime() - position);
                    elapsedRealtimeTemp = elapsedRealtimeTemp == null ? SystemClock.elapsedRealtime() : elapsedRealtimeTemp;
                    tvTime.setBase(elapsedRealtimeTemp - position);
                    tvTime.start();
                    handler.post(myRunnable);

                    mPlaybackState = LeanbackPlaybackState.PLAYING;

                    if (playbackVideoFragment != null) {
                        playbackVideoFragment.togglePlaybackWithoutVideoView(true);
                    }

//                    Toast.makeText(PlaybackVideoFromYoutubeActivity.this, "Playing", Toast.LENGTH_SHORT).show();

                } else {
//                    Long elapsedRealtimeTemp = SystemClock.elapsedRealtime();
//                    Long timeStoppedTemp = tvTime.getBase();
                    tvTime.stop();
                    handler.removeCallbacksAndMessages(null);


                    if (state.toString().equals("PAUSED")) {
                        if (playbackVideoFragment != null) {
                            playbackVideoFragment.togglePlayback(false);
                        }
                        mPlaybackState = LeanbackPlaybackState.PAUSED;
                        String text = mSelectedVideo.getSyncSubtitleText(elapsedRealtimeTemp - timeStoppedTemp);
                        if (!text.isEmpty()) //only shows the popup when exist an subtitle
                            showReasonsScreen(text);

                    }
//                    Toast.makeText(PlaybackVideoFromYoutubeActivity.this, "NOT Playing", Toast.LENGTH_SHORT).show();

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
                        timeStoppedTemp = tvTime.getBase();
                        elapsedRealtimeTemp = SystemClock.elapsedRealtime();
                        String text = mSelectedVideo.getSyncSubtitleText(elapsedRealtimeTemp - timeStoppedTemp);
                        if (text.isEmpty())
                            tvSubtitle.setVisibility(View.GONE);
                        else {
                            tvSubtitle.setVisibility(View.VISIBLE);
                            tvSubtitle.setText(Html.fromHtml(text));
                        }
                    }
                });

                handler.postDelayed(myRunnable, 100);
            }
        };


        mSession = new MediaSession(this, "LeanbackSampleApp");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setActive(true);


    }


    private void showReasonsScreen(String text) {
        ReasonsDialogFragment videoDetailsFragment = ReasonsDialogFragment.newInstance(text);
        FragmentManager fm = getFragmentManager();
        videoDetailsFragment.show(fm, "Sample Fragment");

//        FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
//
//        fTransaction.replace(R.id.framelayout_reasons, videoDetailsFragment);
//        fTransaction.commit();
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
    public void onBackPressed() {
        super.onBackPressed();
        mYoutubeView1.closePlayer();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mYoutubeView1.closePlayer();
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

//        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                DreamTVApp.Logger.d("Button PLAY");
//                llReasons.setVisibility(View.VISIBLE);

                playbackVideoFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                llReasons.setVisibility(View.VISIBLE);

                playbackVideoFragment.togglePlayback(false);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
//                    llReasons.setVisibility(View.VISIBLE);

                    playbackVideoFragment.togglePlayback(false);
                } else {
//                    llReasons.setVisibility(View.GONE);

                    playbackVideoFragment.togglePlayback(true);
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }


    /**
     * Implementation of OnPlayPauseClickedListener
     */
    public void onFragmentPlayPause(Video video, int position, Boolean playPause) {
//        mVideoView.setVideoPath(video.getVideoUrl());

//        String subtitle = video.subtitle_vtt.subtitles;
//        if (subtitle != null && subtitle.length() > 0)
//            mVideoView.addSubtitleSource(new ByteArrayInputStream(subtitle.getBytes()),
//                    MediaFormat.createSubtitleFormat("text/vtt", Locale.ENGLISH.getLanguage()));

        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
//            setupCallbacks();
            mPlaybackState = LeanbackPlaybackState.IDLE;
        }

        if (playPause && mPlaybackState != LeanbackPlaybackState.PLAYING) { //PLAYING
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (position > 0) {
//                llReasons.setVisibility(View.GONE);
//                mYoutubeView1.seekTo(position);
                mYoutubeView1.start();
            }

            tvTime.setBase(SystemClock.elapsedRealtime() - position);
            tvTime.start();
            handler.post(myRunnable);
        } else { //PAUSE
            mPlaybackState = LeanbackPlaybackState.PAUSED;
//            llReasons.setVisibility(View.VISIBLE);
            mYoutubeView1.pause();

            tvTime.setBase(SystemClock.elapsedRealtime() - position);
            tvTime.stop();
            handler.removeCallbacksAndMessages(null);
        }
        updatePlaybackState(position);
        updateMetadata(video);
    }

    private void updatePlaybackState(int position) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, position, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;

        if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
            actions |= PlaybackState.ACTION_PAUSE;
        }

        return actions;
    }

    private void updateMetadata(final Video video) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        String title = video.title.replace("_", " -");

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                video.description);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
                video.thumbnail);

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, video.project);

        Glide.with(this)
                .load(Uri.parse(video.thumbnail))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mSession.setMetadata(metadataBuilder.build());
                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        mSession.setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mYoutubeView1.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                stopPlayback();
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSession.release();
    }


    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    private void stopPlayback() {
        if (mYoutubeView1 != null) {
            mYoutubeView1.stopVideo();
        }
    }

    /*
     * List of various states that we can be in
     */
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    private class MediaSessionCallback extends MediaSession.Callback {
    }

}
