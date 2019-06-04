package com.dream.dreamtv.ui.PlayVideo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

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
import static com.dream.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.dream.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_TASK;

/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackVideoActivity extends FragmentActivity implements ErrorSelectionDialogFragment.OnListener,
        IPlayBackVideoListener, IReasonsDialogListener, ISubtitlePlayBackListener, RatingDialogFragment.OnListener {

    private static final String TAG = PlaybackVideoActivity.class.getSimpleName();

    private static final int DELAY_IN_MS = 100;
    private static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    private static final int ONE_SEC_IN_MS = 1000;
    private static final int DIFFERENCE_TIME_IN_MS = 1000;
    private static final int PLAY = 0;
    private static final int PAUSE = 1;
    private static final int POSITION_OFFSET_IN_MS = 7000;//7 secs in ms
    private int lastSelectedUserTaskShown = -1;
    private int isPlayPauseAction = PAUSE;
    private boolean handlerRunning = true; //we have to manually stop the handler execution, because apparently it is running in a different thread, and removeCallbacks does not work.
    private boolean mPlayFromBeginning;
    private VideoView mVideoView;
    private TextView tvSubtitle;
    private TextView tvTime;
    private Handler handler;
    private Runnable myRunnable;
    private RelativeLayout rlVideoPlayerInfo;
    private LoadingDialog loadingDialog;
    private ArrayList<UserTaskError> userTaskErrorListForSttlPos;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TaskEntity mSelectedTask;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTask;
    private PlaybackViewModel mViewModel;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback_videos);

        PlaybackViewModelFactory factory = InjectorUtils.providePlaybackViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(PlaybackViewModel.class);

        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvTime = findViewById(R.id.tvTime);
        rlVideoPlayerInfo = findViewById(R.id.rlVideoPlayerInfo);

        mSelectedTask = getIntent().getParcelableExtra(INTENT_TASK);
        mSubtitleResponse = getIntent().getParcelableExtra(INTENT_SUBTITLE);
        mUserTask = getIntent().getParcelableExtra(INTENT_USER_TASK);
        mPlayFromBeginning = getIntent().getBooleanExtra(INTENT_PLAY_FROM_BEGINNING, true);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        subtitleHandlerSyncConfig();
        setupVideoPlayer();
        playVideoMode();

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
    public void onDestroy() {
        super.onDestroy();

        stopVideo();
        mVideoView.suspend();
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d(TAG, "KEYCODE_DPAD_LEFT");

                mVideoView.seekTo(mVideoView.getCurrentPosition() - POSITION_OFFSET_IN_MS);
                Toast.makeText(this, getString(R.string.title_video_backward, (POSITION_OFFSET_IN_MS / ONE_SEC_IN_MS)),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO);

                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "KEYCODE_DPAD_RIGHT");

                mVideoView.seekTo(mVideoView.getCurrentPosition() + POSITION_OFFSET_IN_MS);
                Toast.makeText(this, getString(R.string.title_video_forward, (POSITION_OFFSET_IN_MS / ONE_SEC_IN_MS)),
                        Toast.LENGTH_SHORT).show();

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO);

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

                        pauseVideo((long) mVideoView.getCurrentPosition());
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
//                Utils.getAlertDialog(PlaybackVideoActivity.this,
//                        getString(R.string.alert_title_video_terminated),
//                        getString(R.string.alert_msg_video_terminated), getString(R.string.btn_ok),
//                        dialog -> finish()).show();


                mUserTask.setCompleted(1);

                showRatingDialog();
            });

        });

        showLoading();
    }

    private void showLoading() {
        loadingDialog = new LoadingDialog(PlaybackVideoActivity.this, getString(R.string.title_loading_buffering));
        loadingDialog.setCanceledOnTouchOutside(false);
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
            playVideo(null);
        else
            playVideo(mUserTask.getTimeWatched());
    }

    @Override
    public void playVideo(Integer seekToMs) {
        isPlayPauseAction = PLAY;

        if (seekToMs != null)
            mVideoView.seekTo(seekToMs);

        rlVideoPlayerInfo.setVisibility(View.GONE);
        startSyncSubtitle(null);
        mVideoView.start();

        //Analytics Report Event
        firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY);
    }

    @Override
    public void pauseVideo(Long position) {
        stopSyncSubtitle();

        mVideoView.pause();

        isPlayPauseAction = PAUSE;

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

        if (mVideoView != null) {

            Log.d(TAG, "stopVideo() => Time" + mVideoView.getCurrentPosition());

            //Update current time of the video
            mUserTask.setTimeWatched(mVideoView.getCurrentPosition());


            mVideoView.stopPlayback();

            //Analytics Report Event
            firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP);

        }
    }

    @Override
    public void subtitleHandlerSyncConfig() {
        handler = new Handler();
        myRunnable = () -> {
            runOnUiThread(() -> {
                if (!handlerRunning)
                    return;

                Subtitle selectedSubtitle = mSubtitleResponse.getSyncSubtitleText(mVideoView.getCurrentPosition());

                if (selectedSubtitle != null) {
                    Log.d(TAG, "Selected subtitle: #" + selectedSubtitle.position + " - " + selectedSubtitle.text);

                    userTaskErrorListForSttlPos = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(selectedSubtitle.position);

                    if (userTaskErrorListForSttlPos != null && userTaskErrorListForSttlPos.size() > 0) {
                        Log.d(TAG, "Position = " + selectedSubtitle.position
                                + " List: " + userTaskErrorListForSttlPos.toString());

                        if (lastSelectedUserTaskShown != selectedSubtitle.position) {  //pause the video and show the popup
                            //Automatic pop-up with selected errors
                            pauseVideo((long) mVideoView.getCurrentPosition());
                            controlReasonDialogPopUp();

                            lastSelectedUserTaskShown = selectedSubtitle.position;
                        }
                    }

                    showSubtitle(selectedSubtitle);
                }
            });

            if (handlerRunning)
                handler.postDelayed(myRunnable, DELAY_IN_MS);
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

            dismissLoading(); //in case the loading is still visible

//            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
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

//            if (!mSelectedTask.category.equals(TASKS_MY_LIST_CAT)) { //For now, we dont show the popup in my list category . This category is just to see saved videos
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
//        Subtitle subtitleOriginal = mSubtitleResponse.subtitles.get(subtitleOriginalPosition - 1);
        Subtitle subtitleOneBeforeNew;

        // A subtitle from the subtitle navigation was pressed. The video is moving forward or backward
//        if (selectedSubtitle != null) {
        //if selectedSubtitle is null means that the onDialogDismiss action comes from the informative user reason dialog (it shows the selected reasons of the user)

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
    public void onBackPressed() {
        super.onBackPressed();

        stopVideo();
        mVideoView.suspend();
    }


    @Override
    public void onSaveReasons(UserTaskError userTaskError) {
        Log.d(TAG, "onSaveReasons() =>" + userTaskError.toString());

        mViewModel.saveErrors(userTaskError);


        //TODO UPDATE  mUserTask.userTaskErrorList
//        mUserTask.setUserTaskErrorList();
    }


    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.updateUserTask(mUserTask);
    }

    @Override
    public void setRating(int rating) {
        mUserTask.setRating(rating);

        //Update values and exit from the video
        mViewModel.updateUserTask(mUserTask);
        finish();
    }
}
