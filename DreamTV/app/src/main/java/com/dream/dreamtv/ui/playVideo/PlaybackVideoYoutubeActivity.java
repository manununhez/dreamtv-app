package com.dream.dreamtv.ui.playVideo;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.dream.dreamtv.ui.playVideo.dialogs.ErrorSelectionDialogFragment;
import com.dream.dreamtv.ui.playVideo.dialogs.RatingDialogFragment;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
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
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_RATING;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUBTITLE_NAVEGATION;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TASK_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_USER_TASK_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_RATING_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_UPDATE_USER_TASK;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_VIDEO_COMPLETED;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.dream.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_TASK;
import static com.dream.dreamtv.utils.Constants.PREF_SUBTITLE_SMALL_SIZE;
import static com.dream.dreamtv.utils.Constants.STATE_BUFFERING;
import static com.dream.dreamtv.utils.Constants.STATE_ENDED;
import static com.dream.dreamtv.utils.Constants.STATE_PAUSED;
import static com.dream.dreamtv.utils.Constants.STATE_PLAY;
import static com.dream.dreamtv.utils.Constants.STATE_UNSTARTED;
import static com.dream.dreamtv.utils.Constants.STATE_VIDEO_CUED;
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
    private static final int SUBTITLE_DELAY_IN_MS = 100;
    private static final int DELAY_IN_MS = 1000;
    private static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    private static final int DIFFERENCE_TIME_IN_SECS_ = 1;
    private static final int VIDEO_COMPLETED_WATCHING_TRUE = 1;//7 secs in ms
    private static final int BUFFER_VALUE_PB = 2;
    private static final int PLAYER_PROGRESS_SHOW_DELAY = 5000;

    private boolean mPlayFromBeginning;
    private boolean hasAlreadyPlayFromBeginning = false;
    private long mLastClickTime = 0;
    private long mLastProgressPlayerTime = 0;
    private int counterClicks = 1;

    private RelativeLayout rlVideoPlayerInfo;
    private RelativeLayout rlVideoPlayerProgress;
    private YoutubeTvView mYoutubeView;
    private TextView tvSubtitle;
    private TextView tvTime;
    private TextView tvSubtitleError;
    private TextView tvVideoTitle;
    private TextView tvTotalTime;
    private TextView tvCurrentTime;
    private Handler handler;
    private Chronometer chronometer;
    private Runnable myRunnable;
    private Long elapsedRealtimeTemp;
    private Long timeStoppedTemp;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTask;
    private PlaybackViewModel mViewModel;
    private Task mSelectedTask;
    private String mSelectedCategory;
    private LoadingDialog loadingDialog;
    private ProgressBar pbProgress;
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos = new ArrayList<>();


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

        tvSubtitleError = findViewById(R.id.tvSubtitleError);
        tvSubtitleError.setTextSize(Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(getString(R.string.pref_key_subtitle_size), PREF_SUBTITLE_SMALL_SIZE))));

        chronometer = new Chronometer(this); // initiate a chronometer
        tvTime = findViewById(R.id.tvTime);
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        rlVideoPlayerProgress = findViewById(R.id.rlVideoPlayerProgress);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        pbProgress = findViewById(R.id.pbProgress);


        mPlayFromBeginning = getIntent().getBooleanExtra(INTENT_PLAY_FROM_BEGINNING, true);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        instantiateLoading();
        setupInfoPlayer();
        setupVideoPlayer();
    }

    private void setupInfoPlayer() {
        tvVideoTitle.setText(mSubtitleResponse.videoTitleTranslated);
        tvTotalTime.setText(Utils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs()));
        if (mPlayFromBeginning)
            tvCurrentTime.setText(Utils.getTimeFormat(this, 0));
        else
            tvCurrentTime.setText(Utils.getTimeFormat(this, mUserTask.getTimeWatched()));

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
            case FIREBASE_LOG_EVENT_UPDATE_USER_TASK:
                bundle.putInt(FIREBASE_KEY_USER_TASK_ID, mUserTask.id);
                break;
            case FIREBASE_LOG_EVENT_RATING_VIDEO:
                bundle.putInt(FIREBASE_KEY_USER_TASK_ID, mUserTask.id);
                bundle.putInt(FIREBASE_KEY_RATING, mUserTask.getRating());
                break;
            case FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS:
            case FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS:
                bundle.putInt(FIREBASE_KEY_USER_TASK_ID, mUserTask.id);
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.taskId);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY:
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE:
            case FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP:
            case FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS:
            case FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO:
            case FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO:
            case FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER:
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER:
            case FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN:
            case FIREBASE_LOG_EVENT_VIDEO_COMPLETED:
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.taskId);
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.taskId);
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, true);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.taskId);
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


    private void instantiateLoading() {
        loadingDialog = new LoadingDialog(PlaybackVideoYoutubeActivity.this, getString(R.string.title_loading_buffering));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void showLoading() {
        if (!isFinishing()) {
            if (!loadingDialog.isShowing())
                loadingDialog.show();
        }
    }

    private void dismissLoading() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();
    }


    @Override
    public void onPlayerStateChange(VideoState state, long position, float speed, float duration, VideoInfo videoInfo) {

        if (state.toString().equals(STATE_VIDEO_CUED)) {
            Log.d(TAG, "State : " + state.toString());
            dismissLoading();
        } else if (state.toString().equals(STATE_UNSTARTED) || state.toString().equals(STATE_BUFFERING)) {
            Log.d(TAG, "State : " + state.toString());
            showLoading();
        } else if (state.toString().equals(STATE_PLAY)) {
            Log.d(TAG, "State : " + STATE_PLAY);
            dismissLoading();

            //start sync subtitles
            startSyncSubtitle(SystemClock.elapsedRealtime() - position);


            if (!mPlayFromBeginning && !hasAlreadyPlayFromBeginning) { //To continue playing a video, we first play and then seek to an specific time
                Log.d(TAG, "$$ Seeking video()");
                hasAlreadyPlayFromBeginning = true;
                mYoutubeView.seekTo(mUserTask.getTimeWatchedInSecs());
            }

        } else if (state.toString().equals(STATE_PAUSED)) {
            Log.d(TAG, "State : " + STATE_PAUSED);

            dismissLoading();

            pauseVideo(position);
            controlReasonDialogPopUp();

        }


        if (state.toString().equals(STATE_ENDED)) {//at this moment we are in the end of the video. Duration in ms
            Log.d(TAG, "State : " + STATE_ENDED);

            stopVideo();
            showRatingDialog();

            firebaseLoginEvents(FIREBASE_LOG_EVENT_VIDEO_COMPLETED);
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
        Log.d(TAG, "$$ onPlayerReady() -> playVideoMode()");
        subtitleHandlerSyncConfig();
        playVideoMode();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "KEYCODE_DPAD_RIGHT");

                    showPlayerProgress();

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < DELAY_IN_MS)
                        counterClicks++;
                    else
                        counterClicks = 1;


                    Log.d(TAG, "Consecutive clicks =" + counterClicks);

                    mLastClickTime = SystemClock.elapsedRealtime();

                    int moveForward = counterClicks * POSITION_OFFSET;

                    mYoutubeView.moveForward(moveForward);
//                    Toast.makeText(this, getString(R.string.title_video_forward, POSITION_OFFSET),
//                            Toast.LENGTH_SHORT).show();

                    //Analytics Report Event
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "KEYCODE_DPAD_LEFT");

                    showPlayerProgress();

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < DELAY_IN_MS)
                        counterClicks++;
                    else
                        counterClicks = 1;


                    Log.d(TAG, "Consecutive clicks =" + counterClicks);

                    mLastClickTime = SystemClock.elapsedRealtime();

                    int moveBackward = counterClicks * POSITION_OFFSET;
                    mYoutubeView.moveBackward(moveBackward);
//                    Toast.makeText(this, getString(R.string.title_video_backward, POSITION_OFFSET),
//                            Toast.LENGTH_SHORT).show();

                    //Analytics Report Event
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_UP:
                if (action == KeyEvent.ACTION_UP) {
                    showPlayerProgress();
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER);

                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    dismissPlayerProgress();
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER);

                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "KEYCODE_DPAD_CENTER - dispatchKeyEvent");

                    if (mYoutubeView.isPlaying()) {
                        timeStoppedTemp = chronometer.getBase();

                        mYoutubeView.pause();
                    }

                    return true;

                }
            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "$$ dispatchKeyEvent() - KeyEvent.KEYCODE_BACK");
                    stopVideo();
                    mYoutubeView.closePlayer();
                    finish();
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN);
                    return true;
                }
            default:
                return super.dispatchKeyEvent(event);
        }
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
        playVideo(null);

    }

    @Override
    public void playVideo(Integer seekToSecs) {
        Log.d(TAG, "$$ playVideo()");

        mYoutubeView.start();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);
    }

    @Override
    public void pauseVideo(Long position) {
        Log.d(TAG, "$$ pauseVideo()");

        stopSyncSubtitle();

        String currentTime = Utils.getTimeFormat(this, position);
        String videoDuration = Utils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));
        showPlayerInfoOnPause();
        dismissPlayerProgress();


        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE);
    }

    @Override
    public void stopVideo() {
        Log.d(TAG, "$$ stopVideo()");
        stopSyncSubtitle();

        updateUserTimeWatched();

        if (mYoutubeView != null)
            mYoutubeView.stopVideo();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP);
    }

    private void updateUserTimeWatched() {
        long time = elapsedRealtimeTemp - timeStoppedTemp;
        //Update current time of the video
        if (time > 0) { //For some reason, youtube player sometimes restart with the current time in 0, after the correct current time was saved
            Log.d(TAG, "stopVideo() => Time (mYoutubeView)" + time);

            mUserTask.setTimeWatched((int) time);

            updateUserTask(mUserTask);
        }
    }

    @Override
    public void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = () -> {
            runOnUiThread(() -> {
                timeStoppedTemp = chronometer.getBase();
                elapsedRealtimeTemp = SystemClock.elapsedRealtime();

                if (rlVideoPlayerProgress.getVisibility() == View.VISIBLE) {
                    if (SystemClock.elapsedRealtime() - mLastProgressPlayerTime > PLAYER_PROGRESS_SHOW_DELAY)
                        dismissPlayerProgress();

                    //Updating progress
                    tvCurrentTime.setText(Utils.getTimeFormat(this, elapsedRealtimeTemp - timeStoppedTemp));
                    int videoProgress = (int) ((((float) elapsedRealtimeTemp - timeStoppedTemp) / (float) mSelectedTask.video.getVideoDurationInMs()) * 100);
                    pbProgress.setProgress(videoProgress);
                    pbProgress.setSecondaryProgress(videoProgress + BUFFER_VALUE_PB);
                }


                //Subtitles
                Subtitle selectedSubtitle = mSubtitleResponse.getSyncSubtitleText(elapsedRealtimeTemp - timeStoppedTemp);

                if (selectedSubtitle != null) //if subtitle == null, there is not subtitle in the time selected
                    userTaskErrorListForSttlPos = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle.position);
                else userTaskErrorListForSttlPos.clear();

                showSubtitle(selectedSubtitle, userTaskErrorListForSttlPos);

            });
            handler.postDelayed(myRunnable, SUBTITLE_DELAY_IN_MS);
        };
    }

    @Override
    public void startSyncSubtitle(Long base) {
        dismissPlayerInfoOnPause();

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

    void showPlayerInfoOnPause() {
        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
    }

    void dismissPlayerInfoOnPause() {
        rlVideoPlayerInfo.setVisibility(View.GONE);
    }

    void showPlayerProgress() {
        if (rlVideoPlayerProgress.getVisibility() == View.GONE) {
            mLastProgressPlayerTime = SystemClock.elapsedRealtime();
            rlVideoPlayerProgress.setVisibility(View.VISIBLE);
        }
    }

    void dismissPlayerProgress() {
        if (rlVideoPlayerProgress.getVisibility() == View.VISIBLE) {
            mLastProgressPlayerTime = 0;
            rlVideoPlayerProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void showSubtitle(Subtitle subtitle) {
        if (subtitle == null) {
            tvSubtitle.setVisibility(View.GONE);
            tvSubtitleError.setVisibility(View.GONE);
        } else {
            tvSubtitleError.setVisibility(View.GONE);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public void showSubtitleWithErrors(Subtitle subtitle) {
        if (subtitle == null) {
            tvSubtitle.setVisibility(View.GONE);
            tvSubtitleError.setVisibility(View.GONE);
        } else {
            tvSubtitle.setVisibility(View.GONE);
            tvSubtitleError.setVisibility(View.VISIBLE);
            tvSubtitleError.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public void showSubtitle(Subtitle subtitle, ArrayList<UserTaskError> userTaskErrorListForSttlPos) {
        if (userTaskErrorListForSttlPos.size() > 0)
            showSubtitleWithErrors(subtitle);
        else
            showSubtitle(subtitle);

    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
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
        if (userTaskErrorListForSttlPos.size() > 0)
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
        Subtitle subtitleOneBeforeNew;

        // A subtitle from the subtitle navigation was pressed. The video is moving forward or backward
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

        Log.d(TAG, "$$ onDialogClosePlay()");

        playVideo(null);

    }

    @Override
    public void onSaveReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        Log.d(TAG, "onSaveReasons() =>" + userTaskError.toString());

        LiveData<Resource<UserTaskError[]>> saveErrorsLiveData = mViewModel.errorsUpdate(taskId, subtitleVersion, userTaskError, true);

        saveErrorsLiveData.removeObservers(this);

        saveErrorsLiveData.observe(this, errorsResource -> {
            if (errorsResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "errorsUpdate response");

                mUserTask.setUserTaskErrorList(errorsResource.data);

                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS);

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
    public void onUpdateReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        Log.d(TAG, "onUpdateReasons() =>" + userTaskError.toString());

        LiveData<Resource<UserTaskError[]>> updateErrorsLiveData = mViewModel.errorsUpdate(taskId, subtitleVersion, userTaskError, false);

        updateErrorsLiveData.removeObservers(this);

        updateErrorsLiveData.observe(this, errorsResource -> {
            if (errorsResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "errorsUpdate response");

                mUserTask.setUserTaskErrorList(errorsResource.data);

                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS);

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

    private void updateUserTask(UserTask userTask) {
        //Update values and exit from the video
        mViewModel.updateUserTask(userTask);
        firebaseLoginEvents(FIREBASE_LOG_EVENT_UPDATE_USER_TASK);
    }


    @Override
    public void setRating(int rating) {
        mUserTask.setRating(rating);

        mUserTask.setCompleted(VIDEO_COMPLETED_WATCHING_TRUE);
        mUserTask.setTimeWatched(0);//restart video watched

        //Update values and exit from the video
        updateUserTask(mUserTask);

        firebaseLoginEvents(FIREBASE_LOG_EVENT_RATING_VIDEO);

        finish();
    }
}
