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
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.fragment.MainFragment;
import com.dream.dreamtv.fragment.PlaybackVideoFragment;
import com.dream.dreamtv.fragment.ReasonsDialogFragment;
import com.dream.dreamtv.utils.Utils;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;

/**
 * YoutubeActivityApiShowcase
 *
 * @author Bertrand Martel
 */
public class PlaybackVideoYoutubeActivity extends Activity implements
        PlaybackVideoFragment.OnPlayPauseClickedListener {

    private final static String TAG = PlaybackVideoYoutubeActivity.class.getSimpleName();
    private final static String STATE_PLAY = "PLAYING";
    private final static String STATE_PAUSED = "PAUSED";
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;

    //    private LinearLayout llReasons;
    private YoutubeTvView mYoutubeView;
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
    private UserTask selectedUserTask;
    private boolean showContinueDialogOnlyOnce = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_videos_youtube);

        tvTime = new Chronometer(this); // initiate a chronometer
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle); // initiate a chronometer
//        llReasons = (LinearLayout) findViewById(R.id.llReasons); // initiate a chronometer

        mYoutubeView = (YoutubeTvView) findViewById(R.id.video_1);
        mYoutubeView.updateView(getArgumentos());
        mYoutubeView.playVideo(getArgumentos().getString("videoId", ""));


//        Button playBtn = (Button) findViewById(R.id.play_button);
//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.start();
//            }
//        });
//        Button pauseBtn = (Button) findViewById(R.id.pause_button);
//        pauseBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.pause();
//            }
//        });
//        Button nextBtn = (Button) findViewById(R.id.next_button);
//        nextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.nextVideo();
//            }
//        });
//        Button previousBtn = (Button) findViewById(R.id.previous_button);
//        previousBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.previousVideo();
//            }
//        });
//
//        Button backwardBtn = (Button) findViewById(R.id.backward_button);
//        backwardBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.moveBackward(POSITION_OFFSET);
//            }
//        });
//        Button forwardBtn = (Button) findViewById(R.id.forward_button);
//        forwardBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mYoutubeView.moveForward(POSITION_OFFSET);
//            }
//        });


        mYoutubeView.addPlayerListener(new IPlayerListener() {
            @Override
            public void onPlayerReady(final VideoInfo videoInfo) {
            }

            @Override
            public void onPlayerStateChange(final VideoState state,
                                            final long position,
                                            final float speed,
                                            final float duration,
                                            final VideoInfo videoInfo) {


                if (state.toString().equals(STATE_PLAY)) {
                    playVideo(position);
                } else if (state.toString().equals(STATE_PAUSED)) {
                    pauseVideo();

                    if (selectedUserTask != null)
                        controlReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp, selectedUserTask);
                    else
                        controlReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp);

                } else {
                    stopSyncSubtitle();
                }
            }
        });


        subtitleHandlerSyncConfig();


        mSession = new MediaSession(this, "LeanbackSampleApp");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);

    }


    private Bundle getArgumentos() {
        mSelectedVideo = getIntent().getParcelableExtra(VideoDetailsActivity.VIDEO);
        Bundle args = new Bundle();
        args.putString("videoId", mSelectedVideo.getVideoYoutubeId());
        args.putBoolean("autoplay", false);
        args.putBoolean("debug", false);
        args.putBoolean("closedCaptions", false);
        args.putBoolean("showRelatedVideos", false);

        return args;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mYoutubeView.closePlayer();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mYoutubeView.closePlayer();
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        mSession.setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mYoutubeView.isPlaying()) {
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


    private void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeStoppedTemp = tvTime.getBase();
                        elapsedRealtimeTemp = SystemClock.elapsedRealtime();

                        selectedUserTask = mSelectedVideo.getUserTask(elapsedRealtimeTemp - timeStoppedTemp);
                        if (selectedUserTask != null) { //pause the video and show the popup
                            mPlaybackState = LeanbackPlaybackState.PAUSED;
                            mYoutubeView.pause();
                        }


                        controlShowSubtitle(elapsedRealtimeTemp - timeStoppedTemp);

                    }
                });

                handler.postDelayed(myRunnable, 100);
            }
        };
    }


    private void playVideo(long position) {

        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        elapsedRealtimeTemp = elapsedRealtimeTemp == null ? SystemClock.elapsedRealtime() : elapsedRealtimeTemp;

        startSyncSubtitle(elapsedRealtimeTemp - position);

        mPlaybackState = LeanbackPlaybackState.PLAYING;

        if (playbackVideoFragment != null) {
            playbackVideoFragment.togglePlaybackWithoutVideoView(true);
        }

    }

    private void pauseVideo() {
        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        stopSyncSubtitle();
        mPlaybackState = LeanbackPlaybackState.PAUSED;
        if (playbackVideoFragment != null) {
            playbackVideoFragment.togglePlayback(false);
        }
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

    private void controlReasonDialogPopUp(long subtitlePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) //only shows the popup when exist an subtitle
            showReasonsScreen(subtitle);
    }

    private void controlReasonDialogPopUp(long subtitlePosition, UserTask userTask) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) //only shows the popup when exist an subtitle
            showReasonsScreen(subtitle, userTask);
    }

    private void controlShowSubtitle(long subtitleTimePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitleTimePosition);
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }
    }

    private void showReasonsScreen(Subtitle subtitle) {
        if (mSelectedVideo.task_state != MainFragment.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json, subtitle.position, mSelectedVideo.task_id);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }

    private void showReasonsScreen(Subtitle subtitle, UserTask userTask) {
        if (mSelectedVideo.task_state != MainFragment.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json, subtitle.position, mSelectedVideo.task_id, userTask, mSelectedVideo.task_state);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }


//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
//
////        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_MEDIA_PLAY:
//                DreamTVApp.Logger.d("Button PLAY");
////                llReasons.setVisibility(View.VISIBLE);
//
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PAUSE:
////                llReasons.setVisibility(View.VISIBLE);
//
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
////                    llReasons.setVisibility(View.VISIBLE);
//
//                    playbackVideoFragment.togglePlayback(false);
//                } else {
////                    llReasons.setVisibility(View.GONE);
//
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
    public void onFragmentPlayPause(final Video video, final int position, final Boolean playPause, final PlaybackControlsRow mPlaybackControlsRow) {
        if (mSelectedVideo.task_state == MainFragment.SEE_AGAIN_CATEGORY && showContinueDialogOnlyOnce) {
            Utils.getAlertDialogWithChoice(this, "Alert!", "Do you want to continue from the last saved point?",
                    getString(R.string.btn_continue_watching), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Subtitle subtitle = mSelectedVideo.getLastSubtitlePositionTime();
                            mPlaybackControlsRow.setCurrentTime(subtitle.end);
                            setupReproduction(subtitle.end, playPause, video);
                        }
                    }, getString(R.string.btn_no_from_beggining), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPlaybackControlsRow.setCurrentTime(position);
                            setupReproduction(position, playPause, video);
                            dialog.dismiss();
                        }
                    }, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    }).show();

            showContinueDialogOnlyOnce = false;
        } else {
            mPlaybackControlsRow.setCurrentTime(position);
            setupReproduction(position, playPause, video);
        }
    }


    private void setupReproduction(int position, boolean playPause, Video video) {
        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
            mPlaybackState = LeanbackPlaybackState.IDLE;
        }

        if (playPause && mPlaybackState != LeanbackPlaybackState.PLAYING) { //PLAYING
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            mYoutubeView.seekTo(position / 1000);
            mYoutubeView.start();
            startSyncSubtitle(SystemClock.elapsedRealtime() - position);
        } else { //PAUSE
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            mYoutubeView.pause();
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


    private void stopPlayback() {
        if (mYoutubeView != null) {
            mYoutubeView.stopVideo();
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
