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
package com.dream.dreamtv.ui.PlayVideo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.common.IPlayBackVideoListener;
import com.dream.dreamtv.common.IReasonsDialogListener;
import com.dream.dreamtv.common.ISubtitlePlayBackListener;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.ui.PlayVideo.Dialogs.ErrorSelectionDialogFragment;
import com.dream.dreamtv.ui.PlayVideo.Dialogs.RatingDialogFragment;
import com.dream.dreamtv.utils.InjectorUtils;
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
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_STOP_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY;
import static com.dream.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.dream.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_TASK;
import static com.dream.dreamtv.utils.Constants.STATE_ENDED;
import static com.dream.dreamtv.utils.Constants.STATE_PAUSED;
import static com.dream.dreamtv.utils.Constants.STATE_PLAY;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_AUTOPLAY;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_CLOSED_CAPTIONS;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_DEBUG;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_SHOW_RELATED_VIDEOS;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_SHOW_VIDEO_INFO;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_VIDEO_ANNOTATION;
import static com.dream.dreamtv.utils.Constants.YOUTUBE_VIDEO_ID;


public class PlaybackVideoYoutubeActivity extends FragmentActivity implements ErrorSelectionDialogFragment.OnListener,
        IPlayerListener, IPlayBackVideoListener, IReasonsDialogListener, ISubtitlePlayBackListener,
        RatingDialogFragment.OnListener {

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
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TaskEntity mSelectedTask;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTask;
    private PlaybackViewModel mViewModel;
    private int currentTime;
    private boolean mPlayFromBeginning;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback_videos_youtube);

        PlaybackViewModelFactory factory = InjectorUtils.providePlaybackViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PlaybackViewModel.class);

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = findViewById(R.id.tvTime);
        tvSubtitle = findViewById(R.id.tvSubtitle); // initiate a chronometer
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        mSelectedTask = getIntent().getParcelableExtra(INTENT_TASK);
        mSubtitleResponse = getIntent().getParcelableExtra(INTENT_SUBTITLE);
        mUserTask = getIntent().getParcelableExtra(INTENT_USER_TASK);
        mPlayFromBeginning = getIntent().getBooleanExtra(INTENT_PLAY_FROM_BEGINNING, true);

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

        currentTime = (int) position;

        Log.d(TAG, "POSITION : " + position);
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
//            Utils.getAlertDialog(PlaybackVideoYoutubeActivity.this, getString(R.string.alert_title_video_terminated),
//                    getString(R.string.alert_msg_video_terminated), getString(R.string.btn_ok),
//                    dialog -> finish()).show();


            //TODO update completed -> 1 y actualizar lista de finalizados
            mUserTask.setCompleted(1);

            showRatingDialog();

        }

    }

    private void showRatingDialog() {

        RatingDialogFragment ratingDialogFragment = new RatingDialogFragment();
        if (!isFinishing()) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            ratingDialogFragment.show(transaction, "Sample Fragment");
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
        if (mPlayFromBeginning)
            playVideo(null);
        else
            playVideo(mUserTask.getTimeWatchedInSecs());

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
        stopSyncSubtitle();

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

            //IMPORTANT! To update the currentTime, we change the state of the player FIRST
            mYoutubeView.stopVideo();


            //Updated value of the currentTime
            Log.d(TAG, "stopVideo() => Time (Youtube)" + currentTime);

            //TODO update current time of the video
            mUserTask.setTimeWatched(currentTime);


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


                    userTaskErrorListForSttlPos = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle.position);


                    if (userTaskErrorListForSttlPos != null && userTaskErrorListForSttlPos.size() > 0) {
                        Log.d(TAG, "Position = " + selectedSubtitle.position
                                + " List: " + userTaskErrorListForSttlPos.toString());

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
    public void showReasonDialogPopUp(long subtitlePosition,
                                      UserTask userTask) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
//            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                    subtitle.position, mSelectedTask, userTask);
            if (!isFinishing()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                errorSelectionDialogFragment.show(transaction, "Sample Fragment");
            }
//            }
        }
    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask,
                                      ArrayList<UserTaskError> userTaskErrorList) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
//            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
            ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                    subtitle.position, mSelectedTask, userTask, userTaskErrorList);
            if (!isFinishing()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                errorSelectionDialogFragment.show(transaction, "Sample Fragment");
            }
//            }
        }
    }


    @Override
    public void controlReasonDialogPopUp() {
        long subtitlePosition = elapsedRealtimeTemp - timeStoppedTemp;
        if (userTaskErrorListForSttlPos != null && userTaskErrorListForSttlPos.size() > 0)
            showReasonDialogPopUp(subtitlePosition,
                    mUserTask,
                    userTaskErrorListForSttlPos);
        else
            showReasonDialogPopUp(subtitlePosition,
                    mUserTask);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS, bundle);
    }

    @Override
    public void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition) {

//        Subtitle subtitleOld = mSubtitleResponse.subtitles.get(subtitleOriginalPosition - 1);
        Subtitle subtitleOneBeforeNew;

        // A subtitle from the subtitle navigation was pressed. The video is moving forward or backward
//        if (selectedSubtitle != null) {
        //if selectedSubtitle is null means that the onDialogDismiss action comes from the informative user reason dialog (it shows the selected reasons of the user)
        if (selectedSubtitle.position != subtitleOriginalPosition) { //a different subtitle from the original was selected
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stopVideo();
        mYoutubeView.closePlayer();

    }


    @Override
    public void onSaveReasons(UserTaskError userTaskError) {
        Log.d(TAG, "onSaveReasons() =>" + userTaskError.toString());

        mViewModel.saveErrors(userTaskError);

        // TODO UPDATE  mUserTask.userTaskErrorList
//        mUserTask.setUserTaskErrorList();
    }


    @Override
    protected void onStop() {
        super.onStop();


        mViewModel.updateUserTask(mUserTask);

    }

    @Override
    public void setRating(int rating) {
        //TODO complete with dialog
        mUserTask.setRating(rating);

        onBackPressed();
    }
}
