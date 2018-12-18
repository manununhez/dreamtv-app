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

package com.dream.dreamtv.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends Activity implements ReasonsDialogFragment.OnDialogClosedListener,
        IPlayBackVideoListener, IReasonsDialogListener, ISubtitlePlayBackListener {

    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private static final int POSITION_OFFSET = 30000;//30 secs in ms
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
    private int lastSelectedUserTaskShown = -1;
    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvTime = findViewById(R.id.tvTime);
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        mSelectedVideo = getIntent().getParcelableExtra(Constants.VIDEO);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        subtitleHandlerSyncConfig();
        setupVideoPlayer();
        playVideoMode();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
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
                stopVideo();
            }
        } else {
            requestVisibleBehind(false);
        }
    }


    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Bundle bundle = new Bundle();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                DreamTVApp.Logger.d("KEYCODE_DPAD_LEFT");

                mVideoView.seekTo(mVideoView.getCurrentPosition() - POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_backward, (POSITION_OFFSET / 1000)),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
                bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO, bundle);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                DreamTVApp.Logger.d("KEYCODE_DPAD_RIGHT");

                mVideoView.seekTo(mVideoView.getCurrentPosition() + POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_forward, (POSITION_OFFSET / 1000)),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                 bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
                bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO, bundle);

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

                        pauseVideo(null);
                        controlReasonDialogPopUp();

                    } else { //Play
                        playVideoMode();

                    }
                    return true;
                }
            default:
                return super.dispatchKeyEvent(event);
        }
    }




    @Override
    public void setupVideoPlayer() {
        mVideoView = findViewById(R.id.videoView);
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
                stopVideo();
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

                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            DreamTVApp.Logger.d("OnPreparedListener - MEDIA_INFO_VIDEO_RENDERING_START");
                            loadingDialog.dismiss();
                        }

                        return false;
                    }
                });

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Utils.getAlertDialog(PlaybackVideoActivity.this, getString(R.string.alert_title_video_terminated),
                                getString(R.string.alert_msg_video_terminated), getString(R.string.btn_ok),
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        finish();
                                    }
                                }).show();
                    }
                });

            }
        });

        loadingDialog = new LoadingDialog(PlaybackVideoActivity.this, getString(R.string.title_loading_buffering));
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    @Override
    public void playVideoMode() {
        if (mSelectedVideo.task_state == Constants.CONTINUE_WATCHING_CATEGORY && showContinueDialogOnlyOnce) {
            final Subtitle subtitle = mSelectedVideo.getLastSubtitlePositionTime();
            if (subtitle != null) { //Si por alguna razon no se cuenta con subtitulo (algun fallo en el servicio al traer el requerido subt)
                Utils.getAlertDialogWithChoice(this, getString(R.string.title_alert_dialog), getString(R.string.title_continue_from_saved_point, String.valueOf(subtitle.end / 1000 / 60), String.valueOf(mSelectedVideo.duration / 60)),
                        getString(R.string.btn_continue_watching), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playVideo(subtitle.end);
                                //Analytics Report Event
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
                                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
                                bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
                                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO, bundle);
                            }
                        }, getString(R.string.btn_no_from_beggining), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playVideo(null);
                                dialog.dismiss();
                                //Analytics Report Event
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
                                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
                                bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
                                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO, bundle);
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
        } else {
            playVideo(null);
        }
    }

    @Override
    public void playVideo(Integer seekToPosition) {
        isPlayPauseAction = PLAY;

        if (seekToPosition != null)
            mVideoView.seekTo(seekToPosition);

        rlVideoPlayerInfo.setVisibility(View.GONE);
        startSyncSubtitle(null);
        mVideoView.start();

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY, bundle);
    }

    @Override
    public void pauseVideo(Long position) {
        isPlayPauseAction = PAUSE;

        String currentTime = mSelectedVideo.getTimeFormat(this, mVideoView.getCurrentPosition());
        String videoDuration = mSelectedVideo.getTimeFormat(this, mSelectedVideo.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));
        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
        mVideoView.pause();
        stopSyncSubtitle();

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSED, bundle);
    }

    @Override
    public void stopVideo() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            //Analytics Report Event
            Bundle bundle = new Bundle();
            bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
            bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
            bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
            mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_STOP_VIDEO, bundle);

        }
    }

    @Override
    public void subtitleHandlerSyncConfig() {
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

                        if (selectedUserTask != null)
                            if (lastSelectedUserTaskShown != selectedUserTask.subtitle_position) {  //pause the video and show the popup
                                pauseVideo(null);

                                controlReasonDialogPopUp();

                                lastSelectedUserTaskShown = selectedUserTask.subtitle_position;
                            }

                        showSubtitle(mVideoView.getCurrentPosition());
                    }
                });

                if (handlerRunning)
                    handler.postDelayed(myRunnable, 100);
            }
        };
    }

    @Override
    public void startSyncSubtitle(Long base) {
        handlerRunning = true;
        handler.post(myRunnable);
    }

    @Override
    public void stopSyncSubtitle() {
        handlerRunning = false;

        handler.removeCallbacks(null);
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void showSubtitle(long subtitleTimePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitleTimePosition);
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }

    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle

            loadingDialog.dismiss(); //in case the loading is still visible

            if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
                ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                        subtitle.position, mSelectedVideo.task_id);
                if (!isFinishing()) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    reasonsDialogFragment.show(transaction, "Sample Fragment");
                }
            }
        }
    }


    @Override
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
            loadingDialog.dismiss(); //in case the loading is still visible

            if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
                ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                        subtitle.position, mSelectedVideo.task_id, userTask, mSelectedVideo.task_state);
                if (!isFinishing()) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    reasonsDialogFragment.show(transaction, "Sample Fragment");
                }
            }
        }
    }

    @Override
    public void controlReasonDialogPopUp() {
        if (selectedUserTask != null)
            showReasonDialogPopUp(mVideoView.getCurrentPosition(), selectedUserTask);
        else
            showReasonDialogPopUp(mVideoView.getCurrentPosition());


        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_REASONS, bundle);

    }

    @Override
    public void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition) {
        Subtitle subtitle = mSelectedVideo.subtitle_json.subtitles.get(subtitleOriginalPosition);
        Subtitle subtitleOneBeforeNew;
        if (selectedSubtitle != null) { //if selectedSubtitle is null means that the onDialogDismiss action comes from the informative user reason dialog (it shows the selected reasons of the user)

            if (selectedSubtitle.position != subtitle.position) { //a different subtitle from the original was selected
                if (selectedSubtitle.position - 2 >= 0) { //avoid index out of range
                    subtitleOneBeforeNew = mSelectedVideo.subtitle_json.subtitles.get(selectedSubtitle.position - 2); //We go to the end of one subtitle before the previous of the selected subtitle
                    if (selectedSubtitle.start - subtitleOneBeforeNew.end < 1000)
                        mVideoView.seekTo(subtitleOneBeforeNew.end - 1000);
                    else
                        mVideoView.seekTo(subtitleOneBeforeNew.end);
                } else {
                    subtitleOneBeforeNew = mSelectedVideo.subtitle_json.subtitles.get(0); //nos vamos al primer subtitulo
                    mVideoView.seekTo(subtitleOneBeforeNew.start - 1000); //inicio del primer sub
                }

            }
        }

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.video_id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_REASONS, bundle);

        playVideo(null);
    }

}