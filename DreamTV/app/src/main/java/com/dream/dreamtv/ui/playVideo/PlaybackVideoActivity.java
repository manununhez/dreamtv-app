package com.dream.dreamtv.ui.playVideo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.Resource.Status;
import com.dream.dreamtv.data.networking.model.Subtitle;
import com.dream.dreamtv.data.networking.model.SubtitleResponse;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.data.networking.model.UserTask;
import com.dream.dreamtv.data.networking.model.UserTaskError;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.ui.playVideo.dialogs.ErrorSelectionDialogFragment;
import com.dream.dreamtv.ui.playVideo.dialogs.RatingDialogFragment;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.TimeUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import timber.log.Timber;

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
import static com.dream.dreamtv.utils.Constants.STATUS_ERROR;
import static com.dream.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends FragmentActivity implements ErrorSelectionDialogFragment.OnListener,
        IPlayBackVideoListener, IReasonsDialogListener, ISubtitlePlayBackListener, RatingDialogFragment.OnListener {

    private static final int SUBTITLE_DELAY_IN_MS = 100;
    private static final int DELAY_IN_MS = 1000;
    private static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    private static final int ONE_SEC_IN_MS = 1000;
    private static final int DIFFERENCE_TIME_IN_MS = 1000;
    private static final int POSITION_OFFSET_IN_MS = 7000;//7 secs in ms
    private static final int BUFFER_VALUE_PB = 2;
    private static final int PLAYER_PROGRESS_SHOW_DELAY = 5000;
    private boolean handlerRunning = true; //we have to manually stop the handler execution, because apparently it is running in a different thread, and removeCallbacks does not work.
    private boolean mPlayFromBeginning;
    private long mLastClickTime = 0;
    private long mLastProgressPlayerTime = 0;
    private int counterClicks = 1;

    private Category.Type mSelectedCategory;
    private VideoView mVideoView;
    private LoadingDialog loadingDialog;
    private PlaybackViewModel mViewModel;
    private SubtitleResponse mSubtitleResponse;
    private RelativeLayout rlVideoPlayerInfo;
    private RelativeLayout rlVideoPlayerProgress;
    private TextView tvSubtitle;
    private TextView tvSubtitleError;
    private TextView tvTime;
    private TextView tvVideoTitle;
    private TextView tvTotalTime;
    private TextView tvCurrentTime;
    private Task mSelectedTask;
    private UserTask mUserTask;
    private Handler handler;
    private Runnable myRunnable;
    private ProgressBar pbProgress;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos = new ArrayList<>();
    private LiveData<Resource<UserTaskError[]>> saveErrorsLiveData;
    private LiveData<Resource<UserTaskError[]>> updateErrorsLiveData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);

        Timber.d("$$ onCreate");

        if (savedInstanceState != null) {
            mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
            mSelectedCategory = (Category.Type) savedInstanceState.getSerializable(INTENT_CATEGORY);
            mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
            mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
        } else {
            mSelectedTask = getIntent().getParcelableExtra(INTENT_TASK);
            mSelectedCategory = (Category.Type) getIntent().getSerializableExtra(INTENT_CATEGORY);
            mSubtitleResponse = getIntent().getParcelableExtra(INTENT_SUBTITLE);
            mUserTask = getIntent().getParcelableExtra(INTENT_USER_TASK);
        }

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PlaybackViewModel.class);

        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSubtitle.setTextSize(Integer.parseInt(mViewModel.getListSubtitleSize()));

        tvSubtitleError = findViewById(R.id.tvSubtitleError);
        tvSubtitleError.setTextSize(Integer.parseInt(mViewModel.getListSubtitleSize()));


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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(INTENT_TASK, mSelectedTask);
        outState.putSerializable(INTENT_CATEGORY, mSelectedCategory);
        outState.putParcelable(INTENT_SUBTITLE, mSubtitleResponse);
        outState.putParcelable(INTENT_USER_TASK, mUserTask);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
        mSelectedCategory = (Category.Type) savedInstanceState.getSerializable(INTENT_CATEGORY);
        mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
        mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
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
                if (action == KeyEvent.ACTION_UP) {
                    Timber.d("KEYCODE_DPAD_RIGHT");

                    showPlayerProgress();

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < DELAY_IN_MS)
                        counterClicks++;
                    else
                        counterClicks = 1;


                    Timber.d("Consecutive clicks =%s", counterClicks);

                    mLastClickTime = SystemClock.elapsedRealtime();

                    int moveForward = counterClicks * POSITION_OFFSET_IN_MS;

                    if (mVideoView.canSeekForward()) {
                        mVideoView.seekTo(mVideoView.getCurrentPosition() + moveForward);
//                    Toast.makeText(this, getString(R.string.title_video_forward, (moveForward / ONE_SEC_IN_MS)), Toast.LENGTH_SHORT).show();

                        //Analytics Report Event
                        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);
                    }
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (action == KeyEvent.ACTION_UP) {

                    showPlayerProgress();

                    Timber.d("KEYCODE_DPAD_LEFT");

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTime < DELAY_IN_MS)
                        counterClicks++;
                    else
                        counterClicks = 1;


                    Timber.d("Consecutive clicks =%s", counterClicks);

                    mLastClickTime = SystemClock.elapsedRealtime();


                    int moveBackward = counterClicks * POSITION_OFFSET_IN_MS;
                    if (mVideoView.canSeekBackward()) {
                        mVideoView.seekTo(mVideoView.getCurrentPosition() - moveBackward);

//                    Toast.makeText(this, getString(R.string.title_video_backward, (POSITION_OFFSET_IN_MS / ONE_SEC_IN_MS)), Toast.LENGTH_SHORT).show();

                        //Analytics Report Event
                        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);
                    }
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_UP:
                if (action == KeyEvent.ACTION_UP) {
                    showPlayerProgress();
                    //Analytics Report Event
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
                    if (mVideoView.isPlaying()) {
                        pauseVideo((long) mVideoView.getCurrentPosition());
                        controlReasonDialogPopUp();
                    } else { //Play
                        playVideo(null);

                    }
                    return true;
                }

            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_UP) {
                    Timber.d("$$ dispatchKeyEvent() - KeyEvent.KEYCODE_BACK");
                    stopVideo();
                    mVideoView.suspend();
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
                    Timber.d("OnPreparedListener - MEDIA_INFO_BUFFERING_START");
                    showLoading();
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Timber.d("OnPreparedListener - MEDIA_INFO_BUFFERING_END");
                    dismissLoading();
                }

                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    Timber.d("OnPreparedListener - MEDIA_INFO_VIDEO_RENDERING_START");
                    dismissLoading();
                }

                return false;
            });


            mp.setOnCompletionListener(mediaPlayer -> {
                stopVideo();
                showRatingDialog();
                firebaseLoginEvents(FIREBASE_LOG_EVENT_VIDEO_COMPLETED);
            });

        });

        showLoading();


    }

    @Override
    public void playVideoMode() {
        if (mPlayFromBeginning)
            playVideo(null);
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

    @Override
    public void pauseVideo(Long position) {
        stopSyncSubtitle();

        mVideoView.pause();

        String currentTime = TimeUtils.getTimeFormat(this, position);
        String videoDuration = TimeUtils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs());

        tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        dismissPlayerProgress();
        showPlayerInfoOnPause();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE);
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

                    //Updating progress
                    tvCurrentTime.setText(TimeUtils.getTimeFormat(this, mVideoView.getCurrentPosition()));
                    int videoProgress = (int) (((float) mVideoView.getCurrentPosition() / (float) mSelectedTask.video.getVideoDurationInMs()) * 100);
                    pbProgress.setProgress(videoProgress);
                    pbProgress.setSecondaryProgress(videoProgress + BUFFER_VALUE_PB);
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

    @Override
    public void startSyncSubtitle(Long base) {
        Timber.d("$$ startSyncSubtitle()");

        handlerRunning = true;
        handler.post(myRunnable);
    }

    @Override
    public void stopSyncSubtitle() {
        Timber.d("$$ stopSyncSubtitle()");

        handlerRunning = false;

        if (handler != null) {
            handler.removeCallbacks(null);
            handler.removeCallbacksAndMessages(null);
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
        if (userTaskErrorListForSttlPos.size() > 0) {
//            Timber.d("Position = " + subtitle.position + " List: " + userTaskErrorListForSttlPos.toString());
            showSubtitleWithErrors(subtitle);
        } else
            showSubtitle(subtitle);

    }

    @Override
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask) {
        Subtitle subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle

            dismissLoading(); //in case the loading is still visible

            ErrorSelectionDialogFragment errorSelectionDialogFragment = ErrorSelectionDialogFragment.newInstance(mViewModel.getReasons(), mViewModel.getUser(), mSubtitleResponse,
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
                    ErrorSelectionDialogFragment.newInstance(mViewModel.getReasons(), mViewModel.getUser(), mSubtitleResponse,
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

        playVideo(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (saveErrorsLiveData != null)
            saveErrorsLiveData.removeObservers(this);

        if (updateErrorsLiveData != null)
            updateErrorsLiveData.removeObservers(this);
    }

    @Override
    public void onSaveReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        Timber.d("onSaveReasons() =>%s", userTaskError.toString());

        saveErrorsLiveData = mViewModel.saveErrorReasons(taskId, subtitleVersion, userTaskError);

        saveErrorsLiveData.removeObservers(this);

        saveErrorsLiveData.observe(this, errorsResource -> {
            Status status = errorsResource.status;
            UserTaskError[] data = errorsResource.data;
            String message = errorsResource.message;

            if (status.equals(Status.SUCCESS)) {
                Timber.d("errorsUpdate response");

                mUserTask.setUserTaskErrorList(data);

                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS);


            } else if (status.equals(Status.ERROR)) {
                Timber.d(message != null ? message : STATUS_ERROR);
            }
        });


    }

    @Override
    public void onUpdateReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        Timber.d("onUpdateReasons() =>%s", userTaskError.toString());

        updateErrorsLiveData = mViewModel.updateErrorReasons(taskId, subtitleVersion, userTaskError);

        updateErrorsLiveData.removeObservers(this);

        updateErrorsLiveData.observe(this, errorsResource -> {
            Status status = errorsResource.status;
            UserTaskError[] data = errorsResource.data;
            String message = errorsResource.message;

            if (status.equals(Status.SUCCESS)) {
                Timber.d("errorsUpdate response");

                mUserTask.setUserTaskErrorList(data);

                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS);

            } else if (status.equals(Status.ERROR)) {
                Timber.d(message != null ? message : STATUS_ERROR);
            }
        });

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

    private void setupInfoPlayer() {
        tvVideoTitle.setText(mSubtitleResponse.videoTitleTranslated);
        tvTotalTime.setText(TimeUtils.getTimeFormat(this, mSelectedTask.video.getVideoDurationInMs()));
        if (mPlayFromBeginning)
            tvCurrentTime.setText(TimeUtils.getTimeFormat(this, 0));
        else
            tvCurrentTime.setText(TimeUtils.getTimeFormat(this, mUserTask.getTimeWatched()));

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

    private void updateUserTimeWatched() {
        if (mVideoView != null) {
            if (mVideoView.getCurrentPosition() > 0) {
                //Update current time of the video
                Timber.d("stopVideo() => Time%s", mVideoView.getCurrentPosition());
                mUserTask.setTimeWatched(mVideoView.getCurrentPosition());

                updateUserTask(mUserTask);

            }
        }
    }

    private void showPlayerInfoOnPause() {
        rlVideoPlayerInfo.setVisibility(View.VISIBLE);
    }

    private void dismissPlayerInfoOnPause() {
        rlVideoPlayerInfo.setVisibility(View.GONE);
    }

    private void showPlayerProgress() {
        if (rlVideoPlayerProgress.getVisibility() == View.GONE) {
            mLastProgressPlayerTime = SystemClock.elapsedRealtime();
            rlVideoPlayerProgress.setVisibility(View.VISIBLE);
        }
    }

    private void dismissPlayerProgress() {
        if (rlVideoPlayerProgress.getVisibility() == View.VISIBLE) {
            mLastProgressPlayerTime = 0;
            rlVideoPlayerProgress.setVisibility(View.GONE);
        }
    }

    private void updateUserTask(UserTask userTask) {
        //Update values and exit from the video
        mViewModel.updateUserTask(userTask);
        firebaseLoginEvents(FIREBASE_LOG_EVENT_UPDATE_USER_TASK);
    }
}
