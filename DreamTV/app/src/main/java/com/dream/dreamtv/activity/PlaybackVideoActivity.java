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

package com.dream.dreamtv.activity;

import android.app.Activity;
import android.app.FragmentManager;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.fragment.MainFragment;
import com.dream.dreamtv.fragment.PlaybackVideoFragment;
import com.dream.dreamtv.fragment.ReasonsDialogFragment;
import com.dream.dreamtv.utils.LoadingDialog;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends Activity implements
        PlaybackVideoFragment.OnPlayPauseClickedListener {
    private static final String TAG = "PlaybackOverlayActivity";

    private VideoView mVideoView;
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
    private MediaSession mSession;
    private TextView tvSubtitle;
    private Handler handler;
    private Chronometer tvTime;
    private Runnable myRunnable;
    private Video mSelectedVideo;
    private int currentPositionPlayPause = 0;
    private Long timeStoppedTemp;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle);
        tvTime = new Chronometer(this); // initiate a chronometer

        mSelectedVideo = (Video) getIntent().getParcelableExtra(VideoDetailsActivity.VIDEO);


        loadViews();
        setupCallbacks();
        subtitleHandlerSyncConfig();

        mSession = new MediaSession(this, "LeanbackSampleApp");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setActive(true);
    }

    private void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controlShowSubtitle(SystemClock.elapsedRealtime() - tvTime.getBase());

                    }
                });

                handler.postDelayed(myRunnable, 100);
            }
        };
    }


    private void startSyncSubtitle(long base) {
        tvTime.setBase(base);
        tvTime.start();
        handler.post(myRunnable);
    }

    private void stopSyncSubtitle() {
        tvTime.stop();
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoView.suspend();
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_MEDIA_PLAY:
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
//                    playbackVideoFragment.togglePlayback(false);
//                } else {
//                    playbackVideoFragment.togglePlayback(true);
//                }
//                return true;
//            default:
//                return super.onKeyUp(keyCode, event);
//        }
//    }

    /**
     * Implementation of OnPlayPauseClickedListener
     */
    public void onFragmentPlayPause(Video video, int position, Boolean playPause) {
        mVideoView.setVideoPath(video.getVideoUrl());

//        String subtitle = video.subtitle_vtt.subtitles;
//        if (subtitle != null && subtitle.length() > 0)
//            mVideoView.addSubtitleSource(new ByteArrayInputStream(subtitle.getBytes()),
//                    MediaFormat.createSubtitleFormat("text/vtt", Locale.ENGLISH.getLanguage()));

        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
            setupCallbacks();
            mPlaybackState = LeanbackPlaybackState.IDLE;
        }

        currentPositionPlayPause = position;

        if (playPause && mPlaybackState != LeanbackPlaybackState.PLAYING) { //PLAYING
            mPlaybackState = LeanbackPlaybackState.PLAYING;

        } else { //PAUSE
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            mVideoView.pause();

            timeStoppedTemp = tvTime.getBase();

            stopSyncSubtitle();

            controlReasonDialogPopUp(SystemClock.elapsedRealtime() - timeStoppedTemp);

        }

        updatePlaybackState(position);
        updateMetadata(video);
    }

    private void controlReasonDialogPopUp(long subtitlePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) //only shows the popup when exist an subtitle
            showReasonsScreen(subtitle);
    }

    private void controlShowSubtitle(long subtitlePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }

    }


    private void showReasonsScreen(Subtitle subtitle) {
        if (mSelectedVideo.task_state != MainFragment.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(subtitle.text, subtitle.position, mSelectedVideo.task_id, mSelectedVideo.subtitle_json.version_number);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
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
                video.project);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION,
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

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
    }

    private void setupCallbacks() {

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_inaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                mVideoView.stopPlayback();
                mPlaybackState = LeanbackPlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                DreamTVApp.Logger.d("OnPreparedListener - function");
                DreamTVApp.Logger.d("OnPreparedListener - VideView isPlaying?  " + mVideoView.isPlaying());
                final LoadingDialog loadingDialog = new LoadingDialog(PlaybackVideoActivity.this, "Buffering");
                loadingDialog.setCanceledOnTouchOutside(false);

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            loadingDialog.show();
                            DreamTVApp.Logger.d("OnPreparedListener - MEDIA_INFO_BUFFERING_START");
                        }
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            DreamTVApp.Logger.d("OnPreparedListener - MEDIA_INFO_BUFFERING_END");
                            loadingDialog.dismiss();

                            startSyncSubtitle(SystemClock.elapsedRealtime() - currentPositionPlayPause);
                        }

                        return false;
                    }
                });

                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
//                    DreamTVApp.Logger.d("OnPreparedListener - PLAYING -- CurrentPosition:0 isPLaying? " + mVideoView.isPlaying());
//                    mp.seekTo(currentPositionPlayPause);
                    mVideoView.seekTo(currentPositionPlayPause);
                    mVideoView.start();
                }/*else if(mPlaybackState == LeanbackPlaybackState.PAUSED){
                    DreamTVApp.Logger.d("PAUSED 1 - OnPreparedListener");

                }*/
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlaybackState = LeanbackPlaybackState.IDLE;
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
        if (mVideoView.isPlaying()) {
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
        if (mVideoView != null) {
            mVideoView.stopPlayback();
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
