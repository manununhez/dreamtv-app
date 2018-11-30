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
package com.dream.dreamtv.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Utils;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;


public class PlaybackVideoYoutubeActivity extends Activity implements
        ReasonsDialogFragment.OnDialogClosedListener, IPlayerListener, IPlayBackVideoListener,
        IReasonsDialogListener, ISubtitlePlayBackListener {

    private static final String YOUTUBE_VIDEO_ID = "videoId";
    private static final String YOUTUBE_AUTOPLAY = "autoplay";
    private static final String YOUTUBE_SHOW_RELATED_VIDEOS = "showRelatedVideos";
    private static final String YOUTUBE_SHOW_VIDEO_INFO = "showVideoInfo";
    private static final String YOUTUBE_VIDEO_ANNOTATION = "videoAnnotation";
    private static final String YOUTUBE_DEBUG = "debug";
    private static final String YOUTUBE_CLOSED_CAPTIONS = "closedCaptions";
    private static final String STATE_PLAY = "PLAYING";
    private static final String STATE_BUFFERING = "BUFFERING";
    private static final String STATE_ENDED = "ENDED";
    private static final String STATE_PAUSED = "PAUSED";
    private static final int POSITION_OFFSET = 30;//30 secs
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private RelativeLayout rlVideoPlayerInfo;
    private YoutubeTvView mYoutubeView;
    private TextView tvSubtitle;
    private TextView tvTime;
    private Handler handler;
    private Chronometer chronometer;
    private Runnable myRunnable;
    private Video mSelectedVideo;
    private Long elapsedRealtimeTemp;
    private Long timeStoppedTemp;
    private UserTask selectedUserTask;
    private boolean showContinueDialogOnlyOnce = true;
    private int lastSelectedUserTaskShown = -1;
    private int isPlayPauseAction = PAUSE;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_videos_youtube);

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = findViewById(R.id.tvTime);
        tvSubtitle = findViewById(R.id.tvSubtitle); // initiate a chronometer
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        setupVideoPlayer();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onPlayerStateChange(VideoState state, long position, float speed, float duration, VideoInfo videoInfo) {
        if (state.toString().equals(STATE_PLAY)) {
            DreamTVApp.Logger.d("State : " + STATE_PLAY);

            //start sync subtitles
            rlVideoPlayerInfo.setVisibility(View.GONE);
            startSyncSubtitle(SystemClock.elapsedRealtime() - position);

        } else if (state.toString().equals(STATE_PAUSED)) {
            DreamTVApp.Logger.d("State : " + STATE_PAUSED);
            pauseVideo(position);

            controlReasonDialogPopUp();
        }


        if (state.toString().equals(STATE_ENDED)) {//at this moment we are in the end of the video. Duration in ms
            DreamTVApp.Logger.d("State : ENDED");
            stopVideo();
            stopSyncSubtitle();
            Utils.getAlertDialog(PlaybackVideoYoutubeActivity.this, getString(R.string.alert_title_video_terminated),
                    getString(R.string.alert_msg_video_terminated), getString(R.string.btn_ok),
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
        }

    }


    //Called when player is ready.
    @Override
    public void onPlayerReady(VideoInfo videoInfo) {
        subtitleHandlerSyncConfig();
        playVideoMode();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopVideo();
        mYoutubeView.closePlayer();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVideo();
        mYoutubeView.closePlayer();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mYoutubeView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                stopVideo();
            }
        } else {
            requestVisibleBehind(false);
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                DreamTVApp.Logger.d("KEYCODE_DPAD_LEFT");
                mYoutubeView.moveBackward(POSITION_OFFSET);
                Toast.makeText(this, "- " + POSITION_OFFSET + " secs", Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                DreamTVApp.Logger.d("KEYCODE_DPAD_RIGHT");
                mYoutubeView.moveForward(POSITION_OFFSET);
                Toast.makeText(this, "+ " + POSITION_OFFSET + " secs", Toast.LENGTH_SHORT).show();

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
                        isPlayPauseAction = PAUSE;
                        timeStoppedTemp = chronometer.getBase();

                        mYoutubeView.pause();

                    } else { //Play
                        playVideoMode();
                    }
                    DreamTVApp.Logger.d("KEYCODE_DPAD_CENTER - dispatchKeyEvent");
                    return true;
                }
            default:
                return super.dispatchKeyEvent(event);
        }
    }



    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    @Override
    public void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition) {

        Subtitle subtitleOld = mSelectedVideo.subtitle_json.subtitles.get(subtitleOriginalPosition);
        Subtitle subtitleOneBeforeNew;

        if (selectedSubtitle != null) //if selectedSubtitle is null means that the onDialogDismiss action comes from the informative user reason dialog (it shows the selected reasons of the user)
        {
            if (selectedSubtitle.position != subtitleOld.position) { //a different subtitle from the original was selected
                if (selectedSubtitle.position - 2 >= 0) { //avoid index out of range
                    subtitleOneBeforeNew = mSelectedVideo.subtitle_json.subtitles.get(selectedSubtitle.position - 2); //We go to the end of one subtitle before the previous of the selected subtitle
                    if (selectedSubtitle.start - subtitleOneBeforeNew.end < 1000) //1000ms de diff
                        mYoutubeView.seekTo((subtitleOneBeforeNew.end - 1000) / 1000); //damos mas tiempo, para leer subtitulos anterioires
                    else
                        mYoutubeView.seekTo(subtitleOneBeforeNew.end / 1000);
                } else {
                    subtitleOneBeforeNew = mSelectedVideo.subtitle_json.subtitles.get(0); //we go to the first subtitle
                    mYoutubeView.seekTo((subtitleOneBeforeNew.start - 1000) / 1000); //inicio del primer sub
                }

            }
        }

        playVideo(null);


    }


    @Override
    public  void setupVideoPlayer(){
        Bundle bundle = getArgumentos();
        mYoutubeView = findViewById(R.id.video_1);
        mYoutubeView.updateView(bundle);
        mYoutubeView.playVideo(bundle.getString(YOUTUBE_VIDEO_ID, ""));
        mYoutubeView.addPlayerListener(this);
    }

    private Bundle getArgumentos() {
        mSelectedVideo = getIntent().getParcelableExtra(Constants.VIDEO);
        Bundle args = new Bundle();
        args.putString(YOUTUBE_VIDEO_ID, mSelectedVideo.getVideoYoutubeId());
        args.putBoolean(YOUTUBE_AUTOPLAY, false);
        args.putBoolean(YOUTUBE_SHOW_RELATED_VIDEOS, false);
        args.putBoolean(YOUTUBE_SHOW_VIDEO_INFO, false);
        args.putBoolean(YOUTUBE_VIDEO_ANNOTATION, false);
        args.putBoolean(YOUTUBE_DEBUG, false);
        args.putBoolean(YOUTUBE_CLOSED_CAPTIONS, false);
        return args;
    }


    @Override
    public  void playVideoMode() {
        if (mSelectedVideo.task_state == Constants.CONTINUE_WATCHING_CATEGORY && showContinueDialogOnlyOnce) {
            final Subtitle subtitle = mSelectedVideo.getLastSubtitlePositionTime();
            if (subtitle != null) { //Si por alguna razon no se cuenta con subtitulo (algun fallo en el servicio al traer el requerido subt)
                Utils.getAlertDialogWithChoice(this, getString(R.string.title_alert_dialog), getString(R.string.title_continue_from_saved_point, String.valueOf(subtitle.end / 1000 / 60), String.valueOf(mSelectedVideo.duration / 60)),
                        getString(R.string.btn_continue_watching), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playVideo(subtitle.end / 1000);
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
        } else {
            playVideo(null);
        }
    }



    @Override
    public  void playVideo(Integer seekToSecs) {
        isPlayPauseAction = PLAY;

        if (seekToSecs != null)
            mYoutubeView.seekTo(seekToSecs);

        mYoutubeView.start();
    }


    @Override
    public  void pauseVideo(Long position) {
        String currentTime = mSelectedVideo.getTimeFormat(this, position);
        String videoDuration = mSelectedVideo.getTimeFormat(this, mSelectedVideo.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
        stopSyncSubtitle();
    }

    @Override
    public void stopVideo() {
        if (mYoutubeView != null) {
            mYoutubeView.stopVideo();
        }
    }

    @Override
    public  void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeStoppedTemp = chronometer.getBase();
                        elapsedRealtimeTemp = SystemClock.elapsedRealtime();

                        selectedUserTask = mSelectedVideo.getUserTask(elapsedRealtimeTemp - timeStoppedTemp);

                        if (selectedUserTask != null)
                            if (lastSelectedUserTaskShown != selectedUserTask.subtitle_position) {  //pause the video and show the popup
                                mYoutubeView.pause();
                                lastSelectedUserTaskShown = selectedUserTask.subtitle_position;
                            }

                        showSubtitle(elapsedRealtimeTemp - timeStoppedTemp);

                    }
                });
                handler.postDelayed(myRunnable, 100);
            }
        };
    }

    @Override
    public void startSyncSubtitle(Long base) {
        chronometer.setBase(base);
        chronometer.start();
        handler.post(myRunnable);

    }

    @Override
    public  void stopSyncSubtitle() {
        chronometer.stop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public  void showSubtitle(long subtitleTimePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitleTimePosition);
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public  void showReasonDialogPopUp(long subtitlePosition) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
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
    public  void showReasonDialogPopUp(long subtitlePosition, UserTask userTask) {
        Subtitle subtitle = mSelectedVideo.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
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
    public  void controlReasonDialogPopUp() {
        if (selectedUserTask != null)
            showReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp, selectedUserTask);
        else
            showReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp);
    }

}
