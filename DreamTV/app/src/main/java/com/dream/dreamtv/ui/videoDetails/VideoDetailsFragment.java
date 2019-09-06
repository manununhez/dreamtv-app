package com.dream.dreamtv.ui.videoDetails;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SparseArrayObjectAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.presenter.detailsPresenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.ui.playVideo.PlaybackVideoActivity;
import com.dream.dreamtv.ui.playVideo.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_DURATION;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_PROJECT_NAME;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_PLAY_FROM_BEGINNING;
import static com.dream.dreamtv.utils.Constants.INTENT_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_TASK;
import static com.dream.dreamtv.utils.Constants.TASKS_ALL_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_FINISHED_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;
import static com.dream.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;


/*
 * LeanbackDetailsFragment extends DetailsSupportFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related video.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {

    private static final String TRANSITION_NAME = "t_for_transition";
    private static final String TAG = "VideoDetailsFragment";
    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_CONTINUE_VIDEO = 2;
    private static final int ACTION_PLAY_VIDEO_FROM_BEGGINING = 3;
    private static final int ACTION_ADD_MY_LIST = 4;
    private static final int ACTION_REMOVE_MY_LIST = 5;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private ArrayObjectAdapter mAdapter;
    private DetailsOverviewRow rowPresenter;

    private FirebaseAnalytics mFirebaseAnalytics;

    private VideoDetailsViewModel mViewModel;
    private Task mSelectedTask;
    private String mSelectedCategory;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTask;
    private LoadingDialog loadingDialog;

    private LiveData<Resource<Boolean>> addToListLiveData;
    private LiveData<Resource<Boolean>> removeFromListLiveData;
    private LiveData<Resource<SubtitleResponse>> fetchSubtitleLiveData;
    private LiveData<Resource<UserTask>> fetchUserTaskLiveData;
    private LiveData<Resource<UserTask>> createUserTaskLiveData;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        // Get the ViewModel from the factory
        VideoDetailsViewModelFactory factory = InjectorUtils.provideVideoDetailsViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(VideoDetailsViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        mSelectedTask = getActivity().getIntent().getParcelableExtra(INTENT_TASK);

        mSelectedCategory = getActivity().getIntent().getStringExtra(INTENT_CATEGORY);

        if (mSelectedTask != null) {
            setupDetailsOverview();
            fetchUserTasks();

            if (mSelectedTask.userTasks != null && mSelectedTask.userTasks.length > 0) {
                mUserTask = mSelectedTask.userTasks[0]; //TODO what happened is there is more than one UserTask
                setupContinueAction();
            }
        } else {
            getActivity().finish(); //back to HomeActivity
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (fetchSubtitleLiveData != null)
            fetchSubtitleLiveData.removeObservers(getViewLifecycleOwner());

        if (fetchUserTaskLiveData != null)
            fetchUserTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (createUserTaskLiveData != null)
            createUserTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (addToListLiveData != null)
            addToListLiveData.removeObservers(getViewLifecycleOwner());

        if (removeFromListLiveData != null)
            removeFromListLiveData.removeObservers(getViewLifecycleOwner());

    }

    private void setupDetailsOverview() {
        setupAdapter();
        setupDetailsOverviewRow();
        initActionPanel();
        updateBackground(mSelectedTask.video.thumbnail);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void updateBackground(String uri) {
        int resourceId = getContext().getResources()
                .getIdentifier("ic_endless_constellation",
                        "drawable", getContext().getPackageName());

        RequestOptions options = new RequestOptions()
//                .centerCrop()
                .centerInside()
                .error(mDefaultBackground);

        Glide.with(this)
                .asBitmap()
                .load(resourceId)
                .apply(options)
                .into(new SimpleTarget<Bitmap>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(
                            @NonNull Bitmap resource,
                            Transition<? super Bitmap> transition) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
    }

    private void setupAdapter() {
        // Set detail background and style.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter()) {

                    @Override
                    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
                        // Customize Actionbar and Content by using custom colors.
                        RowPresenter.ViewHolder viewHolder = super.createRowViewHolder(parent);

                        View actionsView = viewHolder.view.
                                findViewById(R.id.details_overview_actions_background);
//                        actionsView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.detail_view_actionbar_background));
                        actionsView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.button_selected_shape));

                        View detailsView = viewHolder.view.findViewById(R.id.details_frame);
//                        detailsView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.detail_view_background));
                        detailsView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.detail_view_actionbar_background));
                        return viewHolder;
                    }
                };

        FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(), VideoDetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(mHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

        ListRowPresenter shadowDisabledRowPresenter = new ListRowPresenter();
        shadowDisabledRowPresenter.setShadowEnabled(false);

        ClassPresenterSelector detailsPresenterSelector = new ClassPresenterSelector();
        detailsPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        detailsPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        mAdapter = new ArrayObjectAdapter(detailsPresenterSelector);

        setupDetailsOverviewRow();

        detailsPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == ACTION_PLAY_VIDEO) {
                fetchSubtitlePlayVideo(true, FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN);
            } else if (action.getId() == ACTION_PLAY_VIDEO_FROM_BEGGINING) {
                fetchSubtitlePlayVideo(true, FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO);
            } else if (action.getId() == ACTION_CONTINUE_VIDEO) {
                fetchSubtitlePlayVideo(false, FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO);
            } else if (action.getId() == ACTION_ADD_MY_LIST) {
                addVideoToMyList();
            } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                removeVideoFromMyList();
            }
//            } else {
//                Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
//            }
        });


        setAdapter(mAdapter);

        new Handler().postDelayed(this::startEntranceTransition, 500);
    }

    private void setupDetailsOverviewRow() {
        rowPresenter = new DetailsOverviewRow(mSelectedTask);

        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background)
                .dontAnimate();

        Glide.with(this)
                .asBitmap()
                .load(mSelectedTask.video.thumbnail)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(
                            @NonNull Bitmap resource,
                            Transition<? super Bitmap> transition) {
                        rowPresenter.setImageBitmap(Objects.requireNonNull(getActivity()), resource);
                        startEntranceTransition();
                    }
                });


    }

    private void initActionPanel() {
        setActionPanel(new Action(ACTION_PLAY_VIDEO, getResources().getString(R.string.btn_play_video)),
                new Action(ACTION_ADD_MY_LIST, getResources().getString(R.string.btn_add_to_my_list)));


        verifyIfVideoIsInMyList();
    }

    private void verifyIfVideoIsInMyList() {
        boolean result = mViewModel.verifyIfTaskIsInList(mSelectedTask);

        if (result)
            setActionPanel(ACTION_ADD_MY_LIST,
                    new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_to_my_list)));
        else
            setActionPanel(ACTION_REMOVE_MY_LIST,
                    new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));

    }

    private void setActionPanel(int clearAction, Action... actions) {
        SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
        adapter.clear(clearAction);
        for (Action action : actions)
            adapter.set((int) action.getId(), action);
    }

    private void setActionPanel(Action... actions) {
        SparseArrayObjectAdapter adapter = new SparseArrayObjectAdapter();

        for (Action action : actions) {
            adapter.set((int) action.getId(), action);
        }


        rowPresenter.setActionsAdapter(adapter);

        mAdapter.add(rowPresenter);
    }

    private void instantiateAndShowLoading(String message) {
        loadingDialog = new LoadingDialog(getActivity(), message);
        loadingDialog.setCanceledOnTouchOutside(false);

        if (!Objects.requireNonNull(getActivity()).isFinishing())
            loadingDialog.show();
    }

    private void dismissLoading() {
        if (loadingDialog != null) loadingDialog.dismiss();
    }

    private void firebaseLoginEvents(String logEventName) {
        Bundle bundle = new Bundle();

        switch (logEventName) {
            case FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN:
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                bundle.putString(FIREBASE_KEY_VIDEO_PROJECT_NAME, mSelectedTask.video.project);
                bundle.putLong(FIREBASE_KEY_VIDEO_DURATION, mSelectedTask.video.getVideoDurationInMs());
                break;
            case FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN:
            case FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN:
            case FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO:
            case FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO:
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                break;
            default: // FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN
                bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
                bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
                bundle.putString(FIREBASE_KEY_VIDEO_PROJECT_NAME, mSelectedTask.video.project);
                bundle.putLong(FIREBASE_KEY_VIDEO_DURATION, mSelectedTask.video.getVideoDurationInMs());
                break;
        }

        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    private void setupContinueAction() {
        Log.d(TAG, mUserTask.toString());
        if (mUserTask.getTimeWatchedInSecs() > 0) { //To avoid messages like "0 min, 0 secs"
            String timeFormatted = Utils.getTimeFormatMinSecs(mUserTask.getTimeWatched());

            setActionPanel(ACTION_PLAY_VIDEO,
                    new Action(ACTION_CONTINUE_VIDEO, getString(R.string.btn_continue_watching, timeFormatted)),
                    new Action(ACTION_PLAY_VIDEO_FROM_BEGGINING, getString(R.string.btn_no_from_beggining)));
        } else {
            initActionPanel();
        }
    }

    private void fetchSubtitlePlayVideo(boolean playFromBeginning, String logEventName) {
        String subtitleVersion = getSubtitleVersion();
        //We retrieve subtitles first
        mViewModel.setSubtitleId(mSelectedTask.video.videoId,
                mSelectedTask.subLanguage, subtitleVersion);

        fetchSubtitleLiveData = mViewModel.fetchSubtitle();

        fetchSubtitleLiveData.removeObservers(getViewLifecycleOwner());

        fetchSubtitleLiveData.observe(getViewLifecycleOwner(), subtitleResponseResource -> {
            if (subtitleResponseResource.status.equals(Resource.Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_retrieve_subtitle));

            else if (subtitleResponseResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "Subtitle response");

                if (subtitleResponseResource.data != null &&
                        mSelectedTask.video.title.equals(String.valueOf(subtitleResponseResource.data.videoTitleOriginal))) {

                    mSubtitleResponse = subtitleResponseResource.data;

                    if (mSubtitleResponse.subtitles == null)
                        Toast.makeText(getActivity(), "Subtitle not found", Toast.LENGTH_SHORT).show();
                    else {
                        //PLAY VIDEO
                        if (mUserTask == null) //the are not user tasks for this video, so we need to create a new one
                            createUserTask(playFromBeginning, logEventName);
                        else
                            playVideo(playFromBeginning, logEventName);
                    }
                }

                dismissLoading();

            } else if (subtitleResponseResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (subtitleResponseResource.message != null)
                    Log.d(TAG, subtitleResponseResource.message);
                else
                    Log.d(TAG, "Status ERROR");

                dismissLoading();
            }
        });

    }

    private void playVideo(boolean playFromBeginning, String logEventName) {
        Intent intent;

        if (mSelectedTask.video.isUrlFromYoutube())
            intent = new Intent(getActivity(), PlaybackVideoYoutubeActivity.class);
        else
            intent = new Intent(getActivity(), PlaybackVideoActivity.class);


        intent.putExtra(INTENT_TASK, mSelectedTask);
        intent.putExtra(INTENT_CATEGORY, mSelectedCategory);
        intent.putExtra(INTENT_SUBTITLE, mSubtitleResponse); //TODO si no hay subtitulo, no deberia avanzar a la sgte pantalla
        intent.putExtra(INTENT_USER_TASK, mUserTask); //@NULLABLE puede no tener errores ya marcados
        intent.putExtra(INTENT_PLAY_FROM_BEGINNING, playFromBeginning);
        startActivity(intent);


        firebaseLoginEvents(logEventName);
    }


    private String getSubtitleVersion() {
        String newestVersion = Constants.SUBTITLE_LAST_VERSION;
        DreamTVApp dreamTVApp = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
        List<VideoTests> videoTestsList = dreamTVApp.getVideoTests();

        // We first look is the video is a test video
        for (VideoTests videoTests : videoTestsList)
            if (videoTests.videoId.equals(mSelectedTask.video.videoId)) {
                return String.valueOf(videoTests.subtitleVersion);
            }

        //If is it not a test video, we look if the task has already an userTask with the subtitle version
        if (mUserTask != null)
            return mUserTask.getSubtitleVersion();

        // Finally, we look for the last version available
        return newestVersion;
    }


    /**
     * @param playFromBeginning
     * @param logEventName
     */
    private void createUserTask(boolean playFromBeginning, String logEventName) {
        createUserTaskLiveData = mViewModel.createUserTask(mSelectedTask, mSubtitleResponse.versionNumber);

        createUserTaskLiveData.removeObservers(getViewLifecycleOwner());

        createUserTaskLiveData.observe(getViewLifecycleOwner(), userTaskResource -> {
            if (userTaskResource.status.equals(Resource.Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_preparing_task));
            else if (userTaskResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "createUserTask() response");

                if (userTaskResource.data != null && userTaskResource.data.getTaskId() == mSelectedTask.taskId) {
                    mUserTask = userTaskResource.data;

//                    fetchSubtitlePlayVideo(true, FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN);

                    playVideo(playFromBeginning, logEventName);

                    mViewModel.updateTaskByCategory(TASKS_CONTINUE_CAT); //update category after a new task was added
                }
                dismissLoading();
            } else if (userTaskResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (userTaskResource.message != null)
                    Log.d(TAG, userTaskResource.message);
                else
                    Log.d(TAG, "Status ERROR");

                dismissLoading();
            }

        });

    }

    /**
     * Verify if the current task has already data created. IF not, is call createTask()
     */
    private void fetchUserTasks() {
        fetchUserTaskLiveData = mViewModel.fetchUserTask();

        fetchUserTaskLiveData.removeObservers(getViewLifecycleOwner());

        fetchUserTaskLiveData.observe(getViewLifecycleOwner(), userTaskResource -> {
            if (userTaskResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "responseFromFetchUserTaskErrorDetails response");

                if (userTaskResource.data != null)
                    if (mSelectedTask.taskId == userTaskResource.data.getTaskId()) { //TODO this is a bug of the livedata. It keeps getting the last saved value from different calls
                        mUserTask = userTaskResource.data;

                        setupContinueAction();

                        if (mUserTask.getCompleted() == VIDEO_COMPLETED_WATCHING_TRUE) { //After the user finished a video, we update all categories
                            mViewModel.updateTaskByCategory(TASKS_CONTINUE_CAT); //trying to keep continue_category always updated
                            mViewModel.updateTaskByCategory(TASKS_FINISHED_CAT); //trying to keep finished_category always updated
                            mViewModel.updateTaskByCategory(TASKS_MY_LIST_CAT); //trying to keep mylist_category always updated
                            mViewModel.updateTaskByCategory(TASKS_ALL_CAT); //trying to keep all_category always updated
                            mViewModel.updateTaskByCategory(TASKS_TEST_CAT); //trying to keep test_category always updated
                        } else {
                            mViewModel.updateTaskByCategory(TASKS_CONTINUE_CAT); //trying to keep continue_category always updated
                            mViewModel.updateTaskByCategory(mSelectedCategory); //we update only the current video category
                        }
                    }
            } else if (userTaskResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (userTaskResource.message != null)
                    Log.d(TAG, userTaskResource.message);
                else
                    Log.d(TAG, "Status ERROR");
            }
        });
    }


    /**
     *
     */
    private void addVideoToMyList() {
        addToListLiveData = mViewModel.requestAddToList(mSelectedTask);

        addToListLiveData.removeObservers(getViewLifecycleOwner());

        addToListLiveData.observe(getViewLifecycleOwner(), booleanResource -> {
            if (booleanResource.status.equals(Resource.Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_add_to_list));
            else if (booleanResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "addVideoToMyList() response");

                setActionPanel(ACTION_ADD_MY_LIST,
                        new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_to_my_list)));


                mViewModel.updateTaskByCategory(TASKS_MY_LIST_CAT); //update category after a new task was added

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN);


                dismissLoading();
            } else if (booleanResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (booleanResource.message != null)
                    Log.d(TAG, booleanResource.message);
                else
                    Log.d(TAG, "Status ERROR");

                dismissLoading();
            }
        });

    }

    /**
     *
     */
    private void removeVideoFromMyList() {
        removeFromListLiveData = mViewModel.requestRemoveFromList(mSelectedTask);

        removeFromListLiveData.removeObservers(getViewLifecycleOwner());

        removeFromListLiveData.observe(getViewLifecycleOwner(), booleanResource -> {
            if (booleanResource.status.equals(Resource.Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_remove_from_list));
            else if (booleanResource.status.equals(Resource.Status.SUCCESS)) {
                Log.d(TAG, "removeVideoFromMyList() response");

                setActionPanel(ACTION_REMOVE_MY_LIST,
                        new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));


                mViewModel.updateTaskByCategory(TASKS_MY_LIST_CAT); //update category after a task was removed


                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN);

                dismissLoading();
            } else if (booleanResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (booleanResource.message != null)
                    Log.d(TAG, booleanResource.message);
                else
                    Log.d(TAG, "Status ERROR");

                dismissLoading();
            }
        });
    }
}
