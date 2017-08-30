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
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.fragment.ReasonsDialogFragment;
import com.dream.dreamtv.utils.Constants;

import java.util.concurrent.TimeUnit;

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
        ReasonsDialogFragment.OnDialogClosedListener {


    //    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
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
    private LinearLayout llTest;
    private int isPlayPauseAction = PAUSE;
    private final static int POSITION_OFFSET = 5;
    private static int PLAY = 0;
    private static int PAUSE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_videos_youtube);

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvSubtitle = (TextView) findViewById(R.id.tvSubtitle); // initiate a chronometer
        llTest = (LinearLayout) findViewById(R.id.llTest); // initiate a chronometer
        rlVideoPlayerInfo = (RelativeLayout) findViewById(R.id.rlVideoPlayerInfo);

        mYoutubeView = (YoutubeTvView) findViewById(R.id.video_1);
        mYoutubeView.updateView(getArgumentos());
        mYoutubeView.playVideo(getArgumentos().getString("videoId", ""));


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

                if (state.toString().equals(Constants.STATE_PLAY)) {
                    playVideo(position);
                } else if (state.toString().equals(Constants.STATE_PAUSED)) {
                    DreamTVApp.Logger.d("Position Paused: " + position);
                    pauseVideo(position);

                    if (selectedUserTask != null)
                        controlReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp, selectedUserTask);
                    else
                        controlReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp);

                } else {
                    stopPlayback();
                    stopSyncSubtitle();
//                    stopProgressAutomation();
                }

                if (position == mSelectedVideo.duration) {//at this moment we are in the end of the video
                    stopPlayback();
                    stopSyncSubtitle();
//                    stopProgressAutomation();
                }

//                DreamTVApp.Logger.d("VIDEO STATE -> " + state.toString());
            }
        });


        subtitleHandlerSyncConfig();

    }


    private Bundle getArgumentos() {
        mSelectedVideo = getIntent().getParcelableExtra(Constants.VIDEO);
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
        mYoutubeView.stopVideo();
        mYoutubeView.closePlayer();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mYoutubeView.stopVideo();
        mYoutubeView.closePlayer();
        finish();
    }



    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_CENTER");
                // Do something...

                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_DOWN");
                // Do something...


                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_LEFT");
                // Do something...

                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(this.getClass().getName(), "KEYCODE_DPAD_RIGHT");
                // Do something...

                return true;
            case KeyEvent.KEYCODE_DPAD_UP:

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(this.getClass().getName(), "KEYCODE_VOLUME_DOWN");
                llTest.setVisibility(View.GONE);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d(this.getClass().getName(), "KEYCODE_VOLUME_UP");
                // Do something...
                llTest.setVisibility(View.VISIBLE);

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
                        isPlayPauseAction = PLAY;

                        mYoutubeView.start();
                    }
                    Log.d(this.getClass().getName(), "KEYCODE_DPAD_CENTER - dispatchKeyEvent");
                    return true;
                }
            default:
                return super.dispatchKeyEvent(event);
        }
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
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }


    private String videoCurrentTimeFormat(long millis) {
        String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));

        return hms;


    }

    private void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeStoppedTemp = chronometer.getBase();
                        elapsedRealtimeTemp = SystemClock.elapsedRealtime();

                        DreamTVApp.Logger.d("CurrentTime: " + (elapsedRealtimeTemp - timeStoppedTemp));
                        selectedUserTask = mSelectedVideo.getUserTask(elapsedRealtimeTemp - timeStoppedTemp);
                        if (selectedUserTask != null)  //pause the video and show the popup
                            mYoutubeView.pause();

                        controlShowSubtitle(elapsedRealtimeTemp - timeStoppedTemp);

                    }
                });
                handler.postDelayed(myRunnable, 100);
            }
        };
    }


    private void playVideo(long position) {
        rlVideoPlayerInfo.setVisibility(View.GONE);
        DreamTVApp.Logger.d("Position: " + (SystemClock.elapsedRealtime() - position));
        startSyncSubtitle(SystemClock.elapsedRealtime() - position);
    }

    private void pauseVideo(long position) {
        tvTime.setText(videoCurrentTimeFormat(position) + "/" + videoCurrentTimeFormat(mSelectedVideo.duration * 1000));

        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
        stopSyncSubtitle();
    }

    private void startSyncSubtitle(long base) {
        chronometer.setBase(base);
        chronometer.start();
        handler.post(myRunnable);
    }

    private void stopSyncSubtitle() {
        chronometer.stop();
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
        if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                    subtitle.position, mSelectedVideo.task_id);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }

    private void showReasonsScreen(Subtitle subtitle, UserTask userTask) {
        if (mSelectedVideo.task_state != Constants.MY_LIST_CATEGORY) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ReasonsDialogFragment reasonsDialogFragment = ReasonsDialogFragment.newInstance(mSelectedVideo.subtitle_json,
                    subtitle.position, mSelectedVideo.task_id, userTask, mSelectedVideo.task_state);
            FragmentManager fm = getFragmentManager();
            reasonsDialogFragment.show(fm, "Sample Fragment");
        }
    }


//    /**
//     * Implementation of OnPlayPauseClickedListener
//     */
//    public void onFragmentPlayPause(final Video video, final int position,
//                                    final Constants.Actions actions,
//                                    final PlaybackControlsRow playbackControlsRow) {
//        mPlaybackControlsRow = playbackControlsRow;
//
//        if (mSelectedVideo.task_state == Constants.CONTINUE_WATCHING_CATEGORY && showContinueDialogOnlyOnce) {
//            final Subtitle subtitle = mSelectedVideo.getLastSubtitlePositionTime();
//            Utils.getAlertDialogWithChoice(this, getString(R.string.title_alert_dialog), getString(R.string.title_continue_from_saved_point, String.valueOf(subtitle.end / 1000 / 60), String.valueOf(mSelectedVideo.duration / 60)),
//                    getString(R.string.btn_continue_watching), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            mPlaybackControlsRow.setCurrentTime(subtitle.end);
//                            setupReproduction(subtitle.end, actions, video);
//                        }
//                    }, getString(R.string.btn_no_from_beggining), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            mPlaybackControlsRow.setCurrentTime(position);
//                            setupReproduction(position, actions, video);
//                            dialog.dismiss();
//                        }
//                    }, new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            dialog.dismiss();
//                        }
//                    }).show();
//
//            showContinueDialogOnlyOnce = false;
//        } else {
//            mPlaybackControlsRow.setCurrentTime(position);
//            setupReproduction(position, actions, video);
//        }
//    }


//    private void setupReproduction(int position, Constants.Actions actions, Video video) {
//        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
//            mPlaybackState = LeanbackPlaybackState.IDLE;
//        }
//
//        if (actions.value.equals(Constants.Actions.PLAY.value) && mPlaybackState != LeanbackPlaybackState.PLAYING) { //PLAYING
////            Toast.makeText(this, "Play - Youtube - Position = " + position, Toast.LENGTH_SHORT).show();
//            mPlaybackState = LeanbackPlaybackState.PLAYING;
////            if (position >= 1000) //after the video begins
//            mYoutubeView.seekTo(position / 1000);
////            else
////                mYoutubeView.seekTo(1);
//
//            mYoutubeView.start();
//            startSyncSubtitle(SystemClock.elapsedRealtime() - position);
//            startProgressAutomation();
//        } else if (actions.value.equals(Constants.Actions.PAUSE.value)) { //PAUSE
////            Toast.makeText(this, "Pause - Youtube", Toast.LENGTH_SHORT).show();
//            mPlaybackState = LeanbackPlaybackState.PAUSED;
//            mYoutubeView.pause();
//        } else if (actions.value.equals(Constants.Actions.FORWARD.value)) {
//            int currentTime = mPlaybackControlsRow.getCurrentTime();
//            int forwardTime = currentTime + POSITION_OFFSET;
//            DreamTVApp.Logger.d("Current Time Forward = " + forwardTime);
//            mPlaybackControlsRow.setCurrentTime(forwardTime);
//            mYoutubeView.moveForward(POSITION_OFFSET);
//            stopProgressAutomation();
//            startProgressAutomation();
////            Toast.makeText(this, "Forward - Youtube", Toast.LENGTH_SHORT).show();
//        } else if (actions.value.equals(Constants.Actions.REWIND.value)) {
//            int currentTime = mPlaybackControlsRow.getCurrentTime();
//            int rewindTime = currentTime - POSITION_OFFSET;
//            DreamTVApp.Logger.d("Current Time Rewind = " + rewindTime);
//
//            mPlaybackControlsRow.setCurrentTime(rewindTime);
//            mYoutubeView.moveBackward(POSITION_OFFSET);
//            stopProgressAutomation();
//            startProgressAutomation();
////            Toast.makeText(this, "Rewind - Youtube", Toast.LENGTH_SHORT).show();
//        }
//        updatePlaybackState(position);
//        updateMetadata(video);
//    }

    private void stopPlayback() {
        if (mYoutubeView != null) {
            mYoutubeView.stopVideo();
        }
    }

    @Override
    public void onDialogClosed() {
        isPlayPauseAction = PLAY;

        mYoutubeView.start();
    }


}
