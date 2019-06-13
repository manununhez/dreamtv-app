package com.dream.dreamtv.ui.PlayVideo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.common.IPlayBackVideoListener;
import com.dream.dreamtv.common.IReasonsDialogListener;
import com.dream.dreamtv.common.ISubtitlePlayBackListener;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.ui.PlayVideo.Dialogs.ErrorSelectionDialogFragment;
import com.dream.dreamtv.ui.PlayVideo.Dialogs.RatingDialogFragment;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Objects;

import fr.bmartel.youtubetv.YoutubeTvView;
import fr.bmartel.youtubetv.listener.IPlayerListener;
import fr.bmartel.youtubetv.model.VideoInfo;
import fr.bmartel.youtubetv.model.VideoState;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUBTITLE_NAVEGATION;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.dream.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_TASK;
import static com.dream.dreamtv.utils.Constants.PREF_SUBTITLE_SMALL_SIZE;
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
    private int currentTime;
    private boolean mPlayFromBeginning;
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
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTask;
    private PlaybackViewModel mViewModel;
    private Task mSelectedTask;
    private String mSelectedCategory;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "$$ onCreate()");

        setContentView(R.layout.activity_playback_videos_youtube);


        if (savedInstanceState != null) {
            mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
            mSelectedCategory = savedInstanceState.getString(INTENT_CATEGORY);
            mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
            mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
        } else {
            mSelectedTask = getIntent().getParcelableExtra(INTENT_TASK);
            mSelectedCategory = getIntent().getStringExtra(INTENT_CATEGORY);
            mSubtitleResponse = getIntent().getParcelableExtra(INTENT_SUBTITLE);
            mUserTask = getIntent().getParcelableExtra(INTENT_USER_TASK);
        }

        PlaybackViewModelFactory factory = InjectorUtils.providePlaybackViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PlaybackViewModel.class);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSubtitle.setTextSize(Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(getString(R.string.pref_key_subtitle_size), PREF_SUBTITLE_SMALL_SIZE))));

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = findViewById(R.id.tvTime);
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        mPlayFromBeginning = getIntent().getBooleanExtra(INTENT_PLAY_FROM_BEGINNING, true);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setupVideoPlayer();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(INTENT_TASK, mSelectedTask);
        outState.putString(INTENT_CATEGORY, mSelectedCategory);
        outState.putParcelable(INTENT_SUBTITLE, mSubtitleResponse);
        outState.putParcelable(INTENT_USER_TASK, mUserTask);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
        mSelectedCategory = savedInstanceState.getString(INTENT_CATEGORY);
        mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
        mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
    }

    private void firebaseLoginEvents(String logEventName) {
        Bundle bundle = new Bundle();

        switch (logEventName) {
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY:
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE:
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP:
            case FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS:
            case FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO:
            case FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO:
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, true);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, false);
                break;
            default:
                break;

        }

        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "$$ onPause()");

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

//        Log.d(TAG, "POSITION : " + position);
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(TAG, "KEYCODE_DPAD_LEFT");
                mYoutubeView.moveBackward(POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_backward, POSITION_OFFSET),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "KEYCODE_DPAD_RIGHT");
                mYoutubeView.moveForward(POSITION_OFFSET);
                Toast.makeText(this, getString(R.string.title_video_forward, POSITION_OFFSET),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (isPlayPauseAction == PLAY) { //Pause
                    isPlayPauseAction = PAUSE;
                    timeStoppedTemp = chronometer.getBase();

                    mYoutubeView.pause();

                } else { //Play
                    playVideoMode();
                }
                Log.d(TAG, "KEYCODE_DPAD_CENTER - dispatchKeyEvent");
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        int action = event.getAction();
//        int keyCode = event.getKeyCode();
//
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                if (action == KeyEvent.ACTION_UP) {
//                    if (isPlayPauseAction == PLAY) { //Pause
//                        isPlayPauseAction = PAUSE;
//                        timeStoppedTemp = chronometer.getBase();
//
//                        mYoutubeView.pause();
//
//                    } else { //Play
//                        playVideoMode();
//                    }
//                    Log.d(TAG, "KEYCODE_DPAD_CENTER - dispatchKeyEvent");
//                    return true;
//                }
//            default:
//                return super.dispatchKeyEvent(event);
//        }
//    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    @Override
    public void setupVideoPlayer() {
        Bundle youtubeOptions = new Bundle();
        youtubeOptions.putString(YOUTUBE_VIDEO_ID, mSelectedTask.video.getVideoYoutubeId());
        youtubeOptions.putBoolean(YOUTUBE_AUTOPLAY, false);
        youtubeOptions.putBoolean(YOUTUBE_SHOW_RELATED_VIDEOS, false);
        youtubeOptions.putBoolean(YOUTUBE_SHOW_VIDEO_INFO, false);
        youtubeOptions.putBoolean(YOUTUBE_VIDEO_ANNOTATION, false);
        youtubeOptions.putBoolean(YOUTUBE_DEBUG, false);
        youtubeOptions.putBoolean(YOUTUBE_CLOSED_CAPTIONS, false);

        mYoutubeView = findViewById(R.id.video_1);
        mYoutubeView.updateView(youtubeOptions);
        mYoutubeView.playVideo(youtubeOptions.getString(YOUTUBE_VIDEO_ID, ""));
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
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);
    }


    @Override
    public void pauseVideo(Long position) {
        stopSyncSubtitle();

        String currentTime = Utils.getTimeFormat(this, position);
        String videoDuration = Utils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        rlVideoPlayerInfo.setVisibility(View.VISIBLE);


        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE);
    }

    @Override
    public void stopVideo() {
        stopSyncSubtitle();

        if (mYoutubeView != null) {

            //IMPORTANT! To update the currentTime, we change the state of the player FIRST
            mYoutubeView.stopVideo();


            int time = (int) (mYoutubeView.getCurrentPosition() * 1000);
            //Update current time of the video
            if (time > 0) { //For some reason, youtube player sometimes restart with the current time in 0, after the correct current time was saved
                Log.d(TAG, "stopVideo() => Time (mYoutubeView)" + time);
                mUserTask.setTimeWatched(time);

                mViewModel.updateUserTask(mUserTask);
            }


            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP);
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
//                    Log.d(TAG, "Selected subtitle: #" + selectedSubtitle.position + " - " + selectedSubtitle.text);


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
//            if (!mSelectedCategory.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved video
            ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                    subtitle.position, mSelectedTask, userTask);
            if (!isFinishing()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                errorSelectionDialogFragment.show(transaction, "Sample Fragment");
            }
        }
    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask,
                                      ArrayList<UserTaskError> userTaskErrorList) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
//            if (!mSelectedCategory.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved video
            ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
                    subtitle.position, mSelectedTask, userTask, userTaskErrorList);
            if (!isFinishing()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                errorSelectionDialogFragment.show(transaction, "Sample Fragment");
            }
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
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS);
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
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T);

        } else { // None subtitle from the subtitle navigation was pressed. The video continues as it was.
            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F);

        }

        playVideo(null);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d(TAG, "$$ onBackPressed()");

        stopVideo();
        mYoutubeView.closePlayer();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "$$ onDestroy()");

        stopVideo();
        mYoutubeView.closePlayer();

    }


    @Override
    public void onSaveReasons(UserTaskError userTaskError) {
        Log.d(TAG, "onSaveReasons() =>" + userTaskError.toString());

        LiveData<Resource<UserTaskError[]>> saveErrorsLiveData = mViewModel.errorsUpdate(userTaskError, true);

        saveErrorsLiveData.removeObservers(this);

        saveErrorsLiveData.observe(this, errorsResource -> {
            if (errorsResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "errorsUpdate response");

//                mUserTask.addUserTaskErrorToList(errorsResource.data);
                mUserTask.setUserTaskErrorList(errorsResource.data);

            } else if (errorsResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (errorsResource.message != null)
                    Log.d(TAG, errorsResource.message);
                else
                    Log.d(TAG, "Status ERROR");


//                dismissLoading();
            }
        });

    }

    @Override
    public void onUpdateReasons(UserTaskError userTaskError) {
        Log.d(TAG, "onUpdateReasons() =>" + userTaskError.toString());

        LiveData<Resource<UserTaskError[]>> updateErrorsLiveData = mViewModel.errorsUpdate(userTaskError, false);

        updateErrorsLiveData.removeObservers(this);

        updateErrorsLiveData.observe(this, errorsResource -> {
            if (errorsResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "errorsUpdate response");

//                mUserTask.addUserTaskErrorToList(errorsResource.data);
                mUserTask.setUserTaskErrorList(errorsResource.data);

            } else if (errorsResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (errorsResource.message != null)
                    Log.d(TAG, errorsResource.message);
                else
                    Log.d(TAG, "Status ERROR");


//                dismissLoading();
            }
        });
    }


    @Override
    public void setRating(int rating) {
        mUserTask.setRating(rating);

        //Update values and exit from the video
        mViewModel.updateUserTask(mUserTask);
    }
}
