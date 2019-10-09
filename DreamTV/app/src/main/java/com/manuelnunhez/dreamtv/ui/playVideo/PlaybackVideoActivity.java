package com.manuelnunhez.dreamtv.ui.playVideo;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.manuelnunhez.dreamtv.R;
import com.manuelnunhez.dreamtv.ViewModelFactory;
import com.manuelnunhez.dreamtv.data.model.Category;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.data.model.Resource.Status;
import com.manuelnunhez.dreamtv.data.model.Subtitle;
import com.manuelnunhez.dreamtv.data.model.Subtitle.SubtitleText;
import com.manuelnunhez.dreamtv.data.model.Task;
import com.manuelnunhez.dreamtv.data.model.UserTask;
import com.manuelnunhez.dreamtv.data.model.UserTaskError;
import com.manuelnunhez.dreamtv.databinding.ActivityPlaybackVideosBinding;
import com.manuelnunhez.dreamtv.di.InjectorUtils;
import com.manuelnunhez.dreamtv.ui.playVideo.dialogs.ErrorSelectionDialogFragment;
import com.manuelnunhez.dreamtv.ui.playVideo.dialogs.RatingDialogFragment;
import com.manuelnunhez.dreamtv.utils.LoadingDialog;
import com.manuelnunhez.dreamtv.utils.LocaleHelper;
import com.manuelnunhez.dreamtv.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.widget.Toast.makeText;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.BUFFER_VALUE_PB;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.DELAY_IN_MS;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.DIFFERENCE_TIME_IN_MS;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.PLAYER_PROGRESS_SHOW_DELAY;
import static com.manuelnunhez.dreamtv.ui.playVideo.PlaybackViewModel.SUBTITLE_DELAY_IN_MS;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_RATING;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_SUBTITLE_NAVEGATION;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_TASK_ID;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_USER_TASK_ID;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_RATING_VIDEO;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_UPDATE_USER_TASK;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_VIDEO_COMPLETED;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_TASK;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_USER_TASK;
import static com.manuelnunhez.dreamtv.utils.Constants.STATUS_ERROR;
import static com.manuelnunhez.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;


public class PlaybackVideoActivity extends FragmentActivity implements IPlaybackVideoListener,
        IReasonsDialogListener, ISubtitlePlayBackListener, IMediaPlayerListener,
        ErrorSelectionDialogFragment.OnListener, RatingDialogFragment.OnListener {

    public static final int POSITION_OFFSET_IN_MS = 7000;//7 secs in ms

    private boolean handlerRunning = true; //we have to manually stop the handler execution, because apparently it is running in a different thread, and removeCallbacks does not work.
    private boolean mPlayFromBeginning;
    private Long mLastProgressPlayerActiveTime = 0L;
    private Long mLastClickTimeForward = 0L;
    private Long mLastClickTimeBackward = 0L;

    private Subtitle mSubtitleResponse;
    private Category.Type mSelectedCategory;
    private Task mSelectedTask;
    private UserTask mUserTask;

    private LoadingDialog loadingDialog;
    private PlaybackViewModel mViewModel;
    private Handler handler;
    private Runnable myRunnable;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos = new ArrayList<>();
    private LiveData<Resource<UserTaskError[]>> saveErrorsLiveData;
    private LiveData<Resource<UserTaskError[]>> updateErrorsLiveData;

    private List<Long> lastClicksForward = new ArrayList<>();
    private List<Long> lastClicksBackward = new ArrayList<>();

    private ActivityPlaybackVideosBinding binding;
    private Toast seekToast;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(INTENT_TASK, mSelectedTask);
        outState.putSerializable(INTENT_CATEGORY, mSelectedCategory);
        outState.putParcelable(INTENT_SUBTITLE, mSubtitleResponse);
        outState.putParcelable(INTENT_USER_TASK, mUserTask);
        outState.putBoolean(INTENT_PLAY_FROM_BEGINNING, mPlayFromBeginning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
        mSelectedCategory = (Category.Type) savedInstanceState.getSerializable(INTENT_CATEGORY);
        mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
        mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
        mPlayFromBeginning = savedInstanceState.getBoolean(INTENT_PLAY_FROM_BEGINNING);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPlaybackVideosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (savedInstanceState != null) {
            mSelectedTask = savedInstanceState.getParcelable(INTENT_TASK);
            mSelectedCategory = (Category.Type) savedInstanceState.getSerializable(INTENT_CATEGORY);
            mSubtitleResponse = savedInstanceState.getParcelable(INTENT_SUBTITLE);
            mUserTask = savedInstanceState.getParcelable(INTENT_USER_TASK);
            mPlayFromBeginning = savedInstanceState.getBoolean(INTENT_PLAY_FROM_BEGINNING);
        } else {
            mSelectedTask = getIntent().getParcelableExtra(INTENT_TASK);
            mSelectedCategory = (Category.Type) getIntent().getSerializableExtra(INTENT_CATEGORY);
            mSubtitleResponse = getIntent().getParcelableExtra(INTENT_SUBTITLE);
            mUserTask = getIntent().getParcelableExtra(INTENT_USER_TASK);
            mPlayFromBeginning = getIntent().getBooleanExtra(INTENT_PLAY_FROM_BEGINNING, true);
        }

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PlaybackViewModel.class);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        seekToast = makeText(this, "message", Toast.LENGTH_SHORT);

        instantiateLoading();
        setupVideoPlayer();
        setupInfoPlayer();
        setupSubtitleScreening();

        startVideoReproduction();

    }

    @Override
    public void setupSubtitleScreening() {
        binding.playbackLayout.tvSubtitle.setTextSize(mViewModel.getListSubtitleSize());

        binding.playbackLayout.tvSubtitleError.setTextSize(mViewModel.getListSubtitleSize());
    }

    @Override
    public void startVideoReproduction() {
        playbackHandlerSync();

        //If it is required, seek to video
        if (!mPlayFromBeginning)
            binding.videoView.seekTo(mUserTask.getTimeWatched());


        //play video
        playVideo();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (action == KeyEvent.ACTION_UP) {
                    Timber.d("KEYCODE_DPAD_RIGHT");

                    //cancel backward buttons
                    mLastClickTimeBackward = 0L;
                    lastClicksBackward.clear();
                    //------
                    showProgressPlayer();
                    mLastClickTimeForward = lastClicksForward.size() == 0 ? SystemClock.elapsedRealtime() :
                            lastClicksForward.get(lastClicksForward.size() - 1);

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTimeForward < DELAY_IN_MS) {
                        lastClicksForward.add(SystemClock.elapsedRealtime());
                        int moveForward = lastClicksForward.size() * POSITION_OFFSET_IN_MS;

                        seekToast.setText(getString(R.string.title_video_forward, (moveForward / 1000)));
                        seekToast.show();

                    }

                    mLastClickTimeForward = SystemClock.elapsedRealtime();

                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (action == KeyEvent.ACTION_UP) {
                    Timber.d("KEYCODE_DPAD_LEFT");

                    //cancel forward buttons
                    mLastClickTimeForward = 0L;
                    lastClicksForward.clear();
                    //------
                    showProgressPlayer();
                    mLastClickTimeBackward = lastClicksBackward.size() == 0 ? SystemClock.elapsedRealtime() :
                            lastClicksBackward.get(lastClicksBackward.size() - 1);

                    // Handling multiple clicks, using threshold of 1 second
                    if (SystemClock.elapsedRealtime() - mLastClickTimeBackward < DELAY_IN_MS) {
                        lastClicksBackward.add(SystemClock.elapsedRealtime());
                        int moveForward = lastClicksBackward.size() * POSITION_OFFSET_IN_MS;

                        seekToast.setText(getString(R.string.title_video_backward, (moveForward / 1000)));
                        seekToast.show();

                    }
                    mLastClickTimeBackward = SystemClock.elapsedRealtime();

                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_UP:
                if (action == KeyEvent.ACTION_UP) {
                    showProgressPlayer();
                    //Analytics Report Event
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER);
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    dismissProgressPlayer();
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER);
                    return true;
                }


            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (action == KeyEvent.ACTION_UP) {
                    if (binding.videoView.isPlaying()) {

                        stateVideoPaused((long) getCurrentPosition());

                    } else { //Play
                        playVideo();

                    }
                    return true;
                }

            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_UP) {
                    Timber.d("$$ dispatchKeyEvent() - KeyEvent.KEYCODE_BACK");
                    stateVideoTerminated();
                    return true;
                }

            default:
                return super.dispatchKeyEvent(event);
        }
    }


    @Override
    public void setupVideoPlayer() {
        binding.videoView.setFocusable(false);
        binding.videoView.setFocusableInTouchMode(false);
        binding.videoView.setVideoPath(mSelectedTask.getVideo().videoUrl);
        binding.videoView.setOnErrorListener((mp, what, extra) -> {
            if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                Timber.e(getString(R.string.video_error_media_load_timeout));
            } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                Timber.e(getString(R.string.video_error_server_inaccessible));
            } else {
                Timber.e(getString(R.string.video_error_unknown_error));
            }
            stopVideo();
            return false;
        });

        binding.videoView.setOnPreparedListener(mp -> {
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


            mp.setOnCompletionListener(mediaPlayer -> stateVideoCompleted());

        });

        showLoading();


    }

    private void verifySeekToBackwardForward() {
        if (!lastClicksBackward.isEmpty()) { //Verify how many backward clicks was done
            // Handling multiple clicks, using threshold of 1 second
            if (SystemClock.elapsedRealtime() - mLastClickTimeBackward > DELAY_IN_MS) {

                int moveBackward = lastClicksBackward.size() * POSITION_OFFSET_IN_MS;

                if (binding.videoView.canSeekBackward()) {
                    binding.videoView.seekTo(getCurrentPosition() - moveBackward);

                    //Analytics Report Event
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);
                }

                lastClicksBackward.clear();

                mLastClickTimeBackward = SystemClock.elapsedRealtime();
            }
        } else if (!lastClicksForward.isEmpty()) { //Verify how many forward clicks was done
            // Handling multiple clicks, using threshold of 1 second
            if (SystemClock.elapsedRealtime() - mLastClickTimeForward > DELAY_IN_MS) {
                int moveForward = lastClicksForward.size() * POSITION_OFFSET_IN_MS;

                if (binding.videoView.canSeekForward()) {
                    binding.videoView.seekTo(getCurrentPosition() + moveForward);

                    //Analytics Report Event
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);
                }

                lastClicksForward.clear();

                mLastClickTimeForward = SystemClock.elapsedRealtime();


            }
        }
    }

    @Override
    public void pauseVideo(Long position) {
        binding.videoView.pause();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE);

    }

    @Override
    public void stopVideo() {
        stopSyncSubtitle();

        updateUserTimeWatched();

        if (binding.videoView != null)
            binding.videoView.stopPlayback();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP);
    }


    @Override
    public void playVideo() {
        binding.videoView.start();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);

        stateVideoPlaying(null);
    }

    @Override
    public void stateVideoPlaying(Long position) {
        startSyncSubtitle(null);
        dismissPlayerInfoOnPause();
    }

    @Override
    public void stateVideoPaused(Long position) {
        stopSyncSubtitle();

        pauseVideo(position);

        showPlayerInfoOnPause(position);

        showReasonDialogPopUp(getCurrentPosition(),
                mUserTask,
                userTaskErrorListForSttlPos);
    }


    @Override
    public void stateVideoCompleted() {
        stopVideo();
        showRatingDialog();
        firebaseLoginEvents(FIREBASE_LOG_EVENT_VIDEO_COMPLETED);
    }

    @Override
    public void stateVideoTerminated() {
        stopVideo();
        binding.videoView.suspend();
        finish();
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN);
    }


    public int getCurrentPosition() {
        return binding.videoView.getCurrentPosition();
    }

    @Override
    public void updateSubtitleScreening() {
        //Subtitles
        SubtitleText selectedSubtitle = mSubtitleResponse.getSyncSubtitleText(getCurrentPosition());

        if (selectedSubtitle != null) //if subtitle == null, there is not subtitle in the time selected
            userTaskErrorListForSttlPos = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle.getPosition());
        else userTaskErrorListForSttlPos.clear();

        //Show subtitle
        if (selectedSubtitle == null) {
            binding.playbackLayout.tvSubtitle.setVisibility(View.GONE);
            binding.playbackLayout.tvSubtitleError.setVisibility(View.GONE);
        } else {
            if (userTaskErrorListForSttlPos.size() > 0) { //show subtitle with errors
                binding.playbackLayout.tvSubtitle.setVisibility(View.GONE);
                binding.playbackLayout.tvSubtitleError.setVisibility(View.VISIBLE);
                binding.playbackLayout.tvSubtitleError.setText(Html.fromHtml(selectedSubtitle.getText()));
            } else { //show normal subtitles
                binding.playbackLayout.tvSubtitleError.setVisibility(View.GONE);
                binding.playbackLayout.tvSubtitle.setVisibility(View.VISIBLE);
                binding.playbackLayout.tvSubtitle.setText(Html.fromHtml(selectedSubtitle.getText()));
            }
        }

    }

    @Override
    public void updateProgressPlayer() {
        if (isProgressPlayerActive()) {
            if (SystemClock.elapsedRealtime() - mLastProgressPlayerActiveTime > PLAYER_PROGRESS_SHOW_DELAY)
                dismissProgressPlayer();

            //Updating progress
            binding.playbackLayout.tvCurrentTime.setText(TimeUtils.getTimeFormat(this, getCurrentPosition()));
            int videoProgress = (int) (((float) getCurrentPosition() / (float) mSelectedTask.getVideo().getVideoDurationInMs()) * 100);
            binding.playbackLayout.pbProgress.setProgress(videoProgress);
            binding.playbackLayout.pbProgress.setSecondaryProgress(videoProgress + BUFFER_VALUE_PB);
        }
    }


    @Override
    public void playbackHandlerSync() {
        handler = new Handler();
        myRunnable = () -> {
            runOnUiThread(() -> {
                if (!handlerRunning)
                    return;

                updateProgressPlayer();

                updateSubtitleScreening();

                verifySeekToBackwardForward();

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
    public void showReasonDialogPopUp(long subtitlePosition, UserTask userTask,
                                      ArrayList<UserTaskError> userTaskErrorList) {
        SubtitleText subtitle = mSubtitleResponse.getSyncSubtitleText(subtitlePosition);
        if (subtitle != null) { //only shows the popup when exist an subtitle
            dismissLoading(); //in case the loading is still visible
            ErrorSelectionDialogFragment errorSelectionDialogFragment;
            if (userTaskErrorList != null && userTaskErrorList.size() > 0) {
                errorSelectionDialogFragment =
                        ErrorSelectionDialogFragment.newInstance(mViewModel.getReasons(), mViewModel.getUser(), mSubtitleResponse,
                                subtitle.getPosition(), mSelectedTask, userTask, userTaskErrorList);
            } else {
                errorSelectionDialogFragment =
                        ErrorSelectionDialogFragment.newInstance(mViewModel.getReasons(), mViewModel.getUser(), mSubtitleResponse,
                                subtitle.getPosition(), mSelectedTask, userTask);
            }

            if (!isFinishing()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                errorSelectionDialogFragment.show(transaction, "Sample Fragment");
            }
        }

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS);
    }

    @Override
    public int getSubtitleNavigationSeekToValue(SubtitleText selectedSubtitle) {
        SubtitleText subtitleOneBeforeNew;

        int value;

        int subtitleIndexForVerification = selectedSubtitle.getPosition() - AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION;

        if (subtitleIndexForVerification >= 0) { //avoid index out of range
            subtitleOneBeforeNew = mSubtitleResponse.getSubtitles().get(subtitleIndexForVerification); //We go to the end of one subtitle before the previous of the selected subtitle
            if (selectedSubtitle.getStart() - subtitleOneBeforeNew.getEnd() < DIFFERENCE_TIME_IN_MS)
                value = subtitleOneBeforeNew.getEnd() - DIFFERENCE_TIME_IN_MS;
            else
                value = subtitleOneBeforeNew.getEnd();
        } else {
            subtitleOneBeforeNew = mSubtitleResponse.getSubtitles().get(0); //nos vamos al primer subtitulo
            value = subtitleOneBeforeNew.getStart() - DIFFERENCE_TIME_IN_MS; //inicio del primer sub
        }

        return value;
    }

    @Override
    public void onDialogClosed(SubtitleText selectedSubtitle, int subtitleOriginalPosition) {

        // A subtitle from the subtitle navigation was pressed. The video is moving forward or backward
        if (selectedSubtitle.getPosition() != subtitleOriginalPosition) { //a different subtitle from the original was selected

            binding.videoView.seekTo(getSubtitleNavigationSeekToValue(selectedSubtitle));

            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T);

        } else { // None subtitle from the subtitle navigation was pressed. The video continues as it was.

            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F);

        }

        playVideo();
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

    @Override
    public void setupInfoPlayer() {
        binding.playbackLayout.tvVideoTitle.setText(mSelectedTask.getVideoTitleTranslated());
        binding.playbackLayout.tvTotalTime.setText(TimeUtils.getTimeFormat(this, mSelectedTask.getVideo().getVideoDurationInMs()));
        if (mPlayFromBeginning)
            binding.playbackLayout.tvCurrentTime.setText(TimeUtils.getTimeFormat(this, 0));
        else
            binding.playbackLayout.tvCurrentTime.setText(TimeUtils.getTimeFormat(this, mUserTask.getTimeWatched()));

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
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.getTaskId());
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
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.getTaskId());
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.getVideo().videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.getVideo().audioLanguage);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.getTaskId());
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.getVideo().videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.getVideo().audioLanguage);
                bundle.putBoolean(FIREBASE_KEY_SUBTITLE_NAVEGATION, true);
                break;
            case FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F:
                logEventName = FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS;
                bundle.putInt(FIREBASE_KEY_TASK_ID, mSelectedTask.getTaskId());
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.getVideo().videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.getVideo().audioLanguage);
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
        if (binding.videoView != null) {
            if (getCurrentPosition() > 0) {
                //Update current time of the video
                Timber.d("stopVideo() => Time%s", getCurrentPosition());
                mUserTask.setTimeWatched(getCurrentPosition());

                updateUserTask(mUserTask);

            }
        }
    }

    private void showPlayerInfoOnPause(Long position) {
        dismissProgressPlayer();

        String currentTime = TimeUtils.getTimeFormat(this, position);
        String videoDuration = TimeUtils.getTimeFormat(this, mSelectedTask.getVideo().getVideoDurationInMs());

        binding.playbackLayout.tvTime.setText(getString(R.string.title_current_time_video, currentTime, videoDuration));

        binding.playbackLayout.rlVideoPlayerInfo.setVisibility(View.VISIBLE);
    }

    private void dismissPlayerInfoOnPause() {
        binding.playbackLayout.rlVideoPlayerInfo.setVisibility(View.GONE);
    }

    private void showProgressPlayer() {
        if (!isProgressPlayerActive()) {
            mLastProgressPlayerActiveTime = SystemClock.elapsedRealtime();
            binding.playbackLayout.rlVideoProgressPlayer.setVisibility(View.VISIBLE);
        }
    }

    private void dismissProgressPlayer() {
        if (isProgressPlayerActive()) {
            mLastProgressPlayerActiveTime = 0L;
            binding.playbackLayout.rlVideoProgressPlayer.setVisibility(View.GONE);
        }
    }

    private boolean isProgressPlayerActive() {
        return binding.playbackLayout.rlVideoProgressPlayer.getVisibility() == View.VISIBLE;
    }

    private void updateUserTask(UserTask userTask) {
        //Update values and exit from the video
        mViewModel.updateUserTask(userTask);
        firebaseLoginEvents(FIREBASE_LOG_EVENT_UPDATE_USER_TASK);
    }
}
