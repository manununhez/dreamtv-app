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
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.fragment.ReasonsDialogFragment;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends Activity implements ReasonsDialogFragment.OnDialogClosedListener {

    private VideoView mVideoView;
    private Video mSelectedVideo;
    private TextView tvSubtitle;
    private TextView tvTime;
    private Handler handler;
    private Runnable myRunnable;
    private UserTask selectedUserTask;
    private RelativeLayout rlVideoPlayerInfo;
    private LoadingDialog loadingDialog;
    private boolean handlerRunning = true; //we have to manually stop the handler execution, because apparently it is running in a different thread, and removeCallbacks does not work.
    private boolean showContinueDialogOnlyOnce = true;
    private int isPlayPauseAction = PAUSE;
    private static int PLAY = 0;
    private static int PAUSE = 1;
    private final static int POSITION_OFFSET = 30000;//30 secs

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle);
        tvTime = (TextView) findViewById(R.id.tvTime);
        rlVideoPlayerInfo = (RelativeLayout) findViewById(R.id.rlVideoPlayerInfo);

        mSelectedVideo = (Video) getIntent().getParcelableExtra(Constants.VIDEO);

        subtitleHandlerSyncConfig();
        loadViews();

    }


    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);

        mVideoView.setVideoPath(mSelectedVideo.getVideoUrl());

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
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {

                            DreamTVApp.Logger.d("OnPreparedListener - MEDIA_INFO_BUFFERING_START");
                        }
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            DreamTVApp.Logger.d("OnPreparedListener - MEDIA_INFO_BUFFERING_END");
                            loadingDialog.dismiss();
                        }

                        return false;
                    }
                });

            }
        });

        loadingDialog = new LoadingDialog(PlaybackVideoActivity.this, getString(R.string.title_loading_buffering));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        playVideoMode();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoView.suspend();
    }

    @Override
    public void onBackPressed() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onBackPressed();
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
                        if (!handlerRunning)
                            return;

                        selectedUserTask = mSelectedVideo.getUserTask(mVideoView.getCurrentPosition());
                        if (selectedUserTask != null) { //pause the video and show the popup
                            pauseAction();
                        }

                        controlShowSubtitle(mVideoView.getCurrentPosition());
                    }
                });

                if (handlerRunning)
                    handler.postDelayed(myRunnable, 100);
            }
        };
    }

    private void pauseAction() {
        pauseVideo();

        if (selectedUserTask != null)
            controlReasonDialogPopUp(mVideoView.getCurrentPosition(), selectedUserTask);
        else
            controlReasonDialogPopUp(mVideoView.getCurrentPosition());
    }

    private void startSyncSubtitle() {
        handlerRunning = true;
        handler.post(myRunnable);
    }

    private void stopSyncSubtitle() {
        handlerRunning = false;

        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_LEFT");
                // Do something...
                mVideoView.seekTo(mVideoView.getCurrentPosition() - POSITION_OFFSET);
                Toast.makeText(this, "- " + (POSITION_OFFSET / 1000) + " secs", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_RIGHT");
                // Do something...
                mVideoView.seekTo(mVideoView.getCurrentPosition() + POSITION_OFFSET);

                Toast.makeText(this, "+ " + (POSITION_OFFSET / 1000) + " secs", Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (action == KeyEvent.ACTION_UP) {
                    if (isPlayPauseAction == PLAY) { //Pause

                        pauseVideo();

                        if (selectedUserTask != null)
                            controlReasonDialogPopUp(mVideoView.getCurrentPosition(), selectedUserTask);
                        else
                            controlReasonDialogPopUp(mVideoView.getCurrentPosition());

                    } else { //Play
                        playVideoMode();

                    }
                    return true;
                }
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    private void playVideoMode() {
        if (mSelectedVideo.task_state == Constants.CONTINUE_WATCHING_CATEGORY && showContinueDialogOnlyOnce) {
            final Subtitle subtitle = mSelectedVideo.getLastSubtitlePositionTime();
            Utils.getAlertDialogWithChoice(this, getString(R.string.title_alert_dialog), getString(R.string.title_continue_from_saved_point, String.valueOf(subtitle.end / 1000 / 60), String.valueOf(mSelectedVideo.duration / 60)),
                    getString(R.string.btn_continue_watching), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playVideo(subtitle.end);
                        }
                    }, getString(R.string.btn_no_from_beggining), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playVideo(null);
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
            playVideo(null);
        }
    }

    private void playVideo(Integer seekToPosition) {
        isPlayPauseAction = PLAY;

        if (seekToPosition != null)
            mVideoView.seekTo(seekToPosition);

        rlVideoPlayerInfo.setVisibility(View.GONE);
        startSyncSubtitle();
        mVideoView.start();
    }

    private void pauseVideo() {
        isPlayPauseAction = PAUSE;


        tvTime.setText(videoCurrentTimeFormat(mVideoView.getCurrentPosition()) + "/" + videoCurrentTimeFormat(mSelectedVideo.duration * 1000));
        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
        mVideoView.pause();
        stopSyncSubtitle();
    }

    private String videoCurrentTimeFormat(long millis) {
        String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));

        return hms;


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
        loadingDialog.dismiss(); //in case the loading is still visible

        if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                    subtitle.position, mSelectedVideo.task_id);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }

    private void showReasonsScreen(Subtitle subtitle, UserTask userTask) {
        loadingDialog.dismiss(); //in case the loading is still visible

        if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                    subtitle.position, mSelectedVideo.task_id, userTask, mSelectedVideo.task_state);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }


    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    public void onDialogClosed() {
        playVideo(null);
    }
}
