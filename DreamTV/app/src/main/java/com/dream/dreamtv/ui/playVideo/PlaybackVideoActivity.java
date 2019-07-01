package com.dream.dreamtv.ui.playVideo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import static com.dream.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends FragmentActivity implements ErrorSelectionDialogFragment.OnListener,
        IPlayBackVideoListener, IReasonsDialogListener, ISubtitlePlayBackListener, RatingDialogFragment.OnListener {

    private static final String TAG = PlaybackVideoActivity.class.getSimpleName();

    private static final int SUBTITLE_DELAY_IN_MS = 100;
    private static final int DELAY_IN_MS = 1000;
    private static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    private static final int ONE_SEC_IN_MS = 1000;
    private static final int DIFFERENCE_TIME_IN_MS = 1000;
    private static final int POSITION_OFFSET_IN_MS = 7000;//7 secs in ms
    private static final int BUFFER_VALUE_PB = 1;
    private static final int PLAYER_PROGRESS_SHOW_DELAY = 5000;

    private boolean handlerRunning = true; //we have to manually stop the handler execution, because apparently it is running in a different thread, and removeCallbacks does not work.
    private boolean mPlayFromBeginning;
    private long mLastClickTime = 0;
    private long mLastProgressPlayerTime = 0;
    private long mLastDownPressedTime = 0;
    private int counterClicks = 1;
    private int counterDownPressed = 0;

    private String mSelectedCategory;

    private RelativeLayout rlVideoPlayerInfo;
    private RelativeLayout rlVideoPlayerProgress;
    private TextView tvSubtitle;
    private TextView tvSubtitleError;
    private TextView tvTime;
    private TextView tvVideoTitle;
    private TextView tvTotalTime;
    private TextView tvCurrentTime;
    private VideoView mVideoView;
    private LoadingDialog loadingDialog;
    private PlaybackViewModel mViewModel;
    private SubtitleResponse mSubtitleResponse;
    private Task mSelectedTask;
    private UserTask mUserTask;
    private Handler handler;
    private Runnable myRunnable;
    private ProgressBar pbProgress;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos = new ArrayList<>();


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);

        Log.d(TAG, "$$ onCreate");

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
        subtitleHandlerSyncConfig();
        setupVideoPlayer();
        setupInfoPlayer();
        playVideoMode();

    }

    private void setupInfoPlayer() {
        showPlayerProgress();

        tvVideoTitle.setText(mSelectedTask.video.title);
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
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "KEYCODE_DPAD_RIGHT - ACTION_DOWN");

                    counterDownPressed++;
                    if (mLastDownPressedTime == 0) {
                        mLastDownPressedTime = SystemClock.elapsedRealtime();

                        pauseVideo();

                        showPlayerProgress();

                    }

                    runOnUiThread(() -> {
                        long tmp = mVideoView.getCurrentPosition() + (counterDownPressed * 1000);
                        long progress = tmp < mSelectedTask.video.getVideoDurationInMs() ? tmp : mSelectedTask.video.getVideoDurationInMs(); //time finish video control
                        updatePlayerProgress((int) progress, mSelectedTask.video.getVideoDurationInMs());

                    });


                } else if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "KEYCODE_DPAD_RIGHT - ACTION_UP - counterDownPressed release: " + counterDownPressed + " - release time:" + ((SystemClock.elapsedRealtime() - mLastDownPressedTime) / ONE_SEC_IN_MS));


                    playVideo();

                    int moveForward;
                    if (counterDownPressed == 1) //only one click was pressed
                        moveForward = POSITION_OFFSET_IN_MS;
                    else
                        moveForward = counterDownPressed * 1000;

                    if (mVideoView.canSeekForward()) {
                        mVideoView.seekTo(mVideoView.getCurrentPosition() + moveForward);
                        Toast.makeText(this, getString(R.string.title_video_forward, (moveForward / ONE_SEC_IN_MS)), Toast.LENGTH_SHORT).show();

                        //Analytics Report Event
                        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);
                    }

                    //reset values
                    counterDownPressed = 0;
                    mLastDownPressedTime = 0;

                }

                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "KEYCODE_DPAD_LEFT - ACTION_DOWN");
                    counterDownPressed++;
                    if (mLastDownPressedTime == 0) {
                        mLastDownPressedTime = SystemClock.elapsedRealtime();
                        stopSyncSubtitle();
                        mVideoView.pause();

                        showPlayerProgress();

                    }

                    runOnUiThread(() -> {
                        long tmp = mVideoView.getCurrentPosition() - (counterDownPressed * 1000);
                        long progress = tmp > 0 ? tmp : 0; //time start video control
                        updatePlayerProgress((int) progress, mSelectedTask.video.getVideoDurationInMs());

                    });


                } else if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "KEYCODE_DPAD_LEFT - ACTION_UP - counterDownPressed release: " + counterDownPressed + " - release time:" + ((SystemClock.elapsedRealtime() - mLastDownPressedTime) / ONE_SEC_IN_MS));


                    startSyncSubtitle(null);
                    mVideoView.start();

                    int moveBackward;
                    if (counterDownPressed == 1) //only one click was pressed
                        moveBackward = POSITION_OFFSET_IN_MS;
                    else
                        moveBackward = counterDownPressed * 1000;

                    if (mVideoView.canSeekBackward()) {
                        mVideoView.seekTo(mVideoView.getCurrentPosition() - moveBackward);
                        Toast.makeText(this, getString(R.string.title_video_backward, (moveBackward / ONE_SEC_IN_MS)), Toast.LENGTH_SHORT).show();

                        //Analytics Report Event
                        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);
                    }


                    //reset values
                    counterDownPressed = 0;
                    mLastDownPressedTime = 0;

                }

                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (action == KeyEvent.ACTION_UP) {
                    showPlayerProgress();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    dismissPlayerProgress();
                }
                return true;


            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (action == KeyEvent.ACTION_UP) {
                    if (mVideoView.isPlaying()) {
                        pauseVideo((long) mVideoView.getCurrentPosition());
                        controlReasonDialogPopUp();
                    } else { //Play
                        playVideo();

                    }
                }
                return true;

            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_UP) {
                    Log.d(TAG, "$$ dispatchKeyEvent() - KeyEvent.KEYCODE_BACK");
                    stopVideo();
                    mVideoView.suspend();
                    finish();
                }
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }


    @Override
    public void setupVideoPlayer() {
        mVideoView = findViewById(R.id.videoView);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);

        mVideoView.setVideoPath(mSelectedTask.video.videoUrl);

        mVideoView.setOnErrorListener((mp, what, extra) -> {
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
        });

        mVideoView.setOnPreparedListener(mp -> {
            mp.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Log.d(TAG, "OnPreparedListener - MEDIA_INFO_BUFFERING_START");
                    showLoading();
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Log.d(TAG, "OnPreparedListener - MEDIA_INFO_BUFFERING_END");
                    dismissLoading();
                }

                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    Log.d(TAG, "OnPreparedListener - MEDIA_INFO_VIDEO_RENDERING_START");
                    dismissLoading();
                }

                return false;
            });


            mp.setOnCompletionListener(mediaPlayer -> {
                stopVideo();
                showRatingDialog();
            });

        });

        showLoading();


    }

    private void instantiateLoading() {
        loadingDialog = new LoadingDialog(PlaybackVideoActivity.this, getString(R.string.title_loading_buffering));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void showLoading() {
        if (!isFinishing())
            loadingDialog.show();
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }

    private void showRatingDialog() {

        RatingDialogFragment ratingDialogFragment = new RatingDialogFragment();
        if (!isFinishing()) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            ratingDialogFragment.show(transaction, "Sample Fragment");
        }

    }

    @Override
    public void playVideoMode() {
        if (mPlayFromBeginning)
            playVideo();
        else
            playVideo(mUserTask.getTimeWatched());
    }

    @Override
    public void playVideo(Integer seekToMs) {
        mVideoView.start();
        startSyncSubtitle(null);
        dismissPlayerInfoOnPause();


        if (seekToMs != null)
            mVideoView.seekTo(seekToMs);

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);
    }

    void playVideo(){
        mVideoView.start();
        startSyncSubtitle(null);

        dismissPlayerInfoOnPause();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);
    }


    @Override
    public void pauseVideo(Long position) {
        stopSyncSubtitle();

        mVideoView.pause();

        String currentTime = Utils.getTimeFormat(this, position);
        String videoDuration = Utils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        dismissPlayerProgress();
        showPlayerInfoOnPause();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE);
    }

    void pauseVideo(){
        stopSyncSubtitle();

        mVideoView.pause();
    }

    @Override
    public void stopVideo() {
        stopSyncSubtitle();

        updateUserTimeWatched();

        if (mVideoView != null)
            mVideoView.stopPlayback();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP);
    }

    private void updateUserTimeWatched() {
        if (mVideoView != null) {
            if (mVideoView.getCurrentPosition() > 0) {
                //Update current time of the video
                Log.d(TAG, "stopVideo() => Time" + mVideoView.getCurrentPosition());
                mUserTask.setTimeWatched(mVideoView.getCurrentPosition());

                mViewModel.updateUserTask(mUserTask);

            }
        }
    }


    @Override
    public void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = () -> {
            runOnUiThread(() -> {
                if (!handlerRunning)
                    return;

                if (rlVideoPlayerProgress.getVisibility() == View.VISIBLE) {
                    if (SystemClock.elapsedRealtime() - mLastProgressPlayerTime > PLAYER_PROGRESS_SHOW_DELAY)
                        dismissPlayerProgress();

                    updatePlayerProgress(mVideoView.getCurrentPosition(), mSelectedTask.video.getVideoDurationInMs());
                }

                //Subtitles
                Subtitle selectedSubtitle = mSubtitleResponse.getSyncSubtitleText(mVideoView.getCurrentPosition());

                if (selectedSubtitle != null) //if subtitle == null, there is not subtitle in the time selected
                    userTaskErrorListForSttlPos = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle.position);
                else userTaskErrorListForSttlPos.clear();

                showSubtitle(selectedSubtitle, userTaskErrorListForSttlPos);

            });

            if (handlerRunning)
                handler.postDelayed(myRunnable, SUBTITLE_DELAY_IN_MS);
        };
    }

    private void updatePlayerProgress(int currentVideoPosition, long totalVideoDuration) {
        //Updating progress
        tvCurrentTime.setText(Utils.getTimeFormat(this, currentVideoPosition));
        int videoProgress = (int) (((float) currentVideoPosition / (float) totalVideoDuration) * 100);
        pbProgress.setProgress(videoProgress);
        pbProgress.setSecondaryProgress(videoProgress + BUFFER_VALUE_PB);
    }

    @Override
    public void startSyncSubtitle(Long base) {
        Log.d(TAG, "$$ startSyncSubtitle()");

        handlerRunning = true;
        handler.post(myRunnable);
    }

    @Override
    public void stopSyncSubtitle() {
        Log.d(TAG, "$$ stopSyncSubtitle()");

        handlerRunning = false;

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
        if (subtitle == null)
            tvSubtitle.setVisibility(View.GONE);
        else {
            tvSubtitleError.setVisibility(View.GONE);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public void showSubtitleWithErrors(Subtitle subtitle) {
        if (subtitle == null)
            tvSubtitleError.setVisibility(View.GONE);
        else {
            tvSubtitle.setVisibility(View.GONE);
            tvSubtitleError.setVisibility(View.VISIBLE);
            tvSubtitleError.setText(Html.fromHtml(subtitle.text));
        }
    }

    @Override
    public void showSubtitle(Subtitle subtitle, ArrayList<UserTaskError> userTaskErrorListForSttlPos) {
        if (userTaskErrorListForSttlPos.size() > 0) {
//            Log.d(TAG, "Position = " + subtitle.position + " List: " + userTaskErrorListForSttlPos.toString());
            showSubtitleWithErrors(subtitle);
        } else
            showSubtitle(subtitle);

    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition,
                                      UserTask userTask) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle

            dismissLoading(); //in case the loading is still visible

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
            dismissLoading(); //in case the loading is still visible

            ErrorSelectionDialogFragment errorSelectionDialogFragment =
                    ErrorSelectionDialogFragment.newInstance(mSubtitleResponse,
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
        if (userTaskErrorListForSttlPos != null && userTaskErrorListForSttlPos.size() > 0)
            showReasonDialogPopUp(mVideoView.getCurrentPosition(),
                    mUserTask,
                    userTaskErrorListForSttlPos);
        else
            showReasonDialogPopUp(mVideoView.getCurrentPosition(),
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
                if (selectedSubtitle.getStart() - subtitleOneBeforeNew.getEnd() < DIFFERENCE_TIME_IN_MS)
                    mVideoView.seekTo(subtitleOneBeforeNew.getEnd() - DIFFERENCE_TIME_IN_MS);
                else
                    mVideoView.seekTo(subtitleOneBeforeNew.getEnd());
            } else {
                subtitleOneBeforeNew = mSubtitleResponse.subtitles.get(0); //nos vamos al primer subtitulo
                mVideoView.seekTo(subtitleOneBeforeNew.getStart() - DIFFERENCE_TIME_IN_MS); //inicio del primer sub
            }

            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T);

        } else { // None subtitle from the subtitle navigation was pressed. The video continues as it was.

            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F);

        }

        playVideo();
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

        mUserTask.setCompleted(VIDEO_COMPLETED_WATCHING_TRUE);
        mUserTask.setTimeWatched(0);//restart video watched

        //Update values and exit from the video
        mViewModel.updateUserTask(mUserTask);

        finish();
    }
}
