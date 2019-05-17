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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUBTITLE_NAVEGATION;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_STOP_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_TASK_ERRORS;
import static com.dream.dreamtv.utils.Constants.STATE_ENDED;
import static com.dream.dreamtv.utils.Constants.STATE_PAUSED;
import static com.dream.dreamtv.utils.Constants.STATE_PLAY;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_AUTOPLAY;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_CLOSED_CAPTIONS;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_DEBUG;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_SHOW_RELATED_VIDEOS;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_SHOW_VIDEO_INFO;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_VIDEO_ANNOTATION;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_VIDEO_ID;


public class PlaybackVideoYoutubeActivity extends Activity implements
        ErrorSelectionDialogFragment.OnDialogClosedListener, IPlayerListener, IPlayBackVideoListener,
        IReasonsDialogListener, ISubtitlePlayBackListener {

    private static final String TAG = PlaybackVideoYoutubeActivity.class.getSimpleName();

    private static final int POSITION_OFFSET = 7;//7 secs
    private static final int DELAY_IN_MS = 100;
    private static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    private static final int DIFFERENCE_TIME_IN_SECS_ = 1;
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private int lastSelectedUserTaskShown = -1;
    private int isPlayPauseAction = PAUSE;
    private RelativeLayout rlVideoPlayerInfo;
    private YoutubeTvView mYoutubeView;
    private TextView tvSubtitle;
    private TextView tvTime;
    private Handler handler;
    private Chronometer chronometer;
    private Runnable myRunnable;
    private Long elapsedRealtimeTemp;
    private Long timeStoppedTemp;
    private ArrayList<UserTaskError> userTaskErrorForSpecificSubtitlePosition;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TaskEntity mSelectedTask;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTaskErrorsDetails;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_videos_youtube);

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = findViewById(R.id.tvTime);
        tvSubtitle = findViewById(R.id.tvSubtitle); // initiate a chronometer
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        mSelectedTask = getIntent().getParcelableExtra(INTENT_USER_DATA_TASK);
        mSubtitleResponse = getIntent().getParcelableExtra(INTENT_USER_DATA_SUBTITLE);
        mUserTaskErrorsDetails = getIntent().getParcelableExtra(INTENT_USER_DATA_TASK_ERRORS);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setupVideoPlayer();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopVideo();
        mYoutubeView.closePlayer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stopVideo();
        mYoutubeView.closePlayer();
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
    public void onPlayerStateChange(VideoState state, long position, float speed, float duration, VideoInfo videoInfo) {
        if (state.toString().equals(STATE_PLAY)) {
            Log.d(TAG, "State : " + STATE_PLAY);

            //start sync subtitles
            rlVideoPlayerInfo.setVisibility(View.GONE);
            startSyncSubtitle(SystemClock.elapsedRealtime() - position);

        } else if (state.toString().equals(STATE_PAUSED)) {
            Log.d(TAG, "State : " + STATE_PAUSED);
            pauseVideo(position);
            controlReasonDialogPopUp();
        }


        if (state.toString().equals(STATE_ENDED)) {//at this moment we are in the end of the video. Duration in ms
            Log.d(TAG, "State : " + STATE_ENDED);
            stopVideo();
            Utils.getAlertDialog(PlaybackVideoYoutubeActivity.this, getString(R.string.alert_title_video_terminated),
                    getString(R.string.alert_msg_video_terminated), getString(R.string.btn_ok),
                    dialog -> finish()).show();
        }

    }

    //Called when player is ready.
    @Override
    public void onPlayerReady(VideoInfo videoInfo) {
        subtitleHandlerSyncConfig();
        playVideoMode();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Bundle bundle = new Bundle();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(TAG, "KEYCODE_DPAD_LEFT");
                mYoutubeView.moveBackward(POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_backward, POSITION_OFFSET),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO, bundle);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "KEYCODE_DPAD_RIGHT");
                mYoutubeView.moveForward(POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_forward, POSITION_OFFSET),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO, bundle);
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
                    Log.d(TAG, "KEYCODE_DPAD_CENTER - dispatchKeyEvent");
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
    public void setupVideoPlayer() {
        Bundle bundle = new Bundle();
        bundle.putString(YOUTUBE_VIDEO_ID, mSelectedTask.video.getVideoYoutubeId());
        bundle.putBoolean(YOUTUBE_AUTOPLAY, false);
        bundle.putBoolean(YOUTUBE_SHOW_RELATED_VIDEOS, false);
        bundle.putBoolean(YOUTUBE_SHOW_VIDEO_INFO, false);
        bundle.putBoolean(YOUTUBE_VIDEO_ANNOTATION, false);
        bundle.putBoolean(YOUTUBE_DEBUG, false);
        bundle.putBoolean(YOUTUBE_CLOSED_CAPTIONS, false);

        mYoutubeView = findViewById(R.id.video_1);
        mYoutubeView.updateView(bundle);
        mYoutubeView.playVideo(bundle.getString(YOUTUBE_VIDEO_ID, ""));
        mYoutubeView.addPlayerListener(this);
    }

    @Override
    public void playVideoMode() {
        if (mUserTaskErrorsDetails.getTimeWatchedInSecs() > 0) {
            Utils.getAlertDialogWithChoice(this, getString(R.string.title_alert_dialog), getString(R.string.title_continue_from_saved_point),
                    getString(R.string.btn_continue_watching, String.valueOf(mUserTaskErrorsDetails.getTimeWatchedInMins())), (dialog, which) -> {
                        playVideo(mUserTaskErrorsDetails.getTimeWatchedInSecs());
                        //Analytics Report Event
                        Bundle bundle = new Bundle();
                        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO, bundle);
                    }, getString(R.string.btn_no_from_beggining), (dialog, which) -> {
                        playVideo(null);
                        dialog.dismiss();
                        //Analytics Report Event
                        Bundle bundle = new Bundle();
                        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO, bundle);
                    }, DialogInterface::dismiss).show();
        } else {
            playVideo(null);
        }
    }

    @Override
    public void playVideo(Integer seekToSecs) {
        isPlayPauseAction = PLAY;

        if (seekToSecs != null)
            mYoutubeView.seekTo(seekToSecs);

        mYoutubeView.start();

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY, bundle);
    }


    @Override
    public void pauseVideo(Long position) {

        String currentTime = Utils.getTimeFormat(this, position);
        String videoDuration = Utils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        rlVideoPlayerInfo.setVisibility(View.VISIBLE);


        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSED, bundle);
    }

    @Override
    public void stopVideo() {
        stopSyncSubtitle();

        if (mYoutubeView != null) {
            mYoutubeView.stopVideo();
            //Analytics Report Event
            Bundle bundle = new Bundle();
            bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
            bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
            mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_STOP_VIDEO, bundle);
        }
    }

    @Override
    public void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = () -> {
            runOnUiThread(() -> {
                timeStoppedTemp = chronometer.getBase();
                elapsedRealtimeTemp = SystemClock.elapsedRealtime();

                Subtitle selectedSubtitle = mSubtitleResponse.getSyncSubtitleText(elapsedRealtimeTemp - timeStoppedTemp);

                if (selectedSubtitle != null) { //if subtitle == null, there is not subtitle in the time selected
                    Log.d(TAG, "Selected subtitle: #" + selectedSubtitle.position + " - " + selectedSubtitle.text);


                    userTaskErrorForSpecificSubtitlePosition = mUserTaskErrorsDetails.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle);


                    if (userTaskErrorForSpecificSubtitlePosition != null && userTaskErrorForSpecificSubtitlePosition.size() > 0) {
                        Log.d(TAG, "Position = " + selectedSubtitle.position
                                + " List: " + userTaskErrorForSpecificSubtitlePosition.toString());

                        if (lastSelectedUserTaskShown != selectedSubtitle.position) {  //pause the video and show the popup

                            mYoutubeView.pause(); //we don't use pauseVideo() here because position_time we have only in onPlayerStateChange()
                            lastSelectedUserTaskShown = selectedSubtitle.position;
                        }
                    }

                    showSubtitle(selectedSubtitle);
                }

            });
            handler.postDelayed(myRunnable, DELAY_IN_MS);
        };
    }

    @Override
    public void startSyncSubtitle(Long base) {
        chronometer.setBase(base);
        chronometer.start();
        handler.post(myRunnable);

    }

    @Override
    public void stopSyncSubtitle() {
        chronometer.stop();

        if (handler != null) {
            handler.removeCallbacks(null);
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void showSubtitle(Subtitle subtitle) {
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
                ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                        subtitle.position, mSelectedTask);
                if (!isFinishing()) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    errorSelectionDialogFragment.show(transaction, "Sample Fragment");
                }
            }
        }
    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition, ArrayList<UserTaskError> userTaskError) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
                ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                        subtitle.position, mSelectedTask, userTaskError);
                if (!isFinishing()) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    errorSelectionDialogFragment.show(transaction, "Sample Fragment");
                }
            }
        }
    }


    @Override
    public void controlReasonDialogPopUp() {
        if (userTaskErrorForSpecificSubtitlePosition != null && userTaskErrorForSpecificSubtitlePosition.size() > 0)
            showReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp, userTaskErrorForSpecificSubtitlePosition);
        else
            showReasonDialogPopUp(elapsedRealtimeTemp - timeStoppedTemp);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS, bundle);
    }

    @Override
    public void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition) {

        Subtitle subtitleOld = mSubtitleResponse.subtitles.get(subtitleOriginalPosition);
        Subtitle subtitleOneBeforeNew;

        if (selectedSubtitle != null) { // A subtitle from the subtitle navigation was pressed. The video is moving forward or backward
            //if selectedSubtitle is null means that the onDialogDismiss action comes from the informative user reason dialog (it shows the selected reasons of the user)
            if (selectedSubtitle.position != subtitleOld.position) { //a different subtitle from the original was selected
                if (selectedSubtitle.position - AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION >= 0) { //avoid index out of range
                    subtitleOneBeforeNew = mSubtitleResponse.subtitles.get(
                            selectedSubtitle.position - AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION); //We go to the end of one subtitle before the previous of the selected subtitle
                    if (selectedSubtitle.getStartInSecs() - subtitleOneBeforeNew.getEndInSecs() < DIFFERENCE_TIME_IN_SECS_)
                        mYoutubeView.seekTo(subtitleOneBeforeNew.getEndInSecs() - DIFFERENCE_TIME_IN_SECS_); //damos mas tiempo, para leer subtitulos anterioires
                    else
                        mYoutubeView.seekTo(subtitleOneBeforeNew.getEndInSecs());
                } else {
                    subtitleOneBeforeNew = mSubtitleResponse.subtitles.get(0); //we go to the first subtitle
                    mYoutubeView.seekTo(subtitleOneBeforeNew.getStartInSecs() - DIFFERENCE_TIME_IN_SECS_); //inicio del primer sub
                }

            }
            //Analytics Report Event
            Bundle bundle = new Bundle();
            bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
            bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
            bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, true);
            mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS, bundle);

        } else { // None subtitle from the subtitle navigation was pressed. The video continues as it was.

            //Analytics Report Event
            Bundle bundle = new Bundle();
            bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
            bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
            bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, false);
            mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS, bundle);

        }


        playVideo(null);

    }
}
