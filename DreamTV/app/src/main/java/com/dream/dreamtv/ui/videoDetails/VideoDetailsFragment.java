package com.dream.dreamtv.ui.videoDetails;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
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
import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.Resource.Status;
import com.dream.dreamtv.data.networking.model.SubtitleResponse;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.data.networking.model.UserTask;
import com.dream.dreamtv.data.networking.model.VideoTest;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.presenter.detailsPresenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.ui.playVideo.PlaybackVideoActivity;
import com.dream.dreamtv.ui.playVideo.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.TimeUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.dream.dreamtv.data.model.Category.Type.ALL;
import static com.dream.dreamtv.data.model.Category.Type.CONTINUE;
import static com.dream.dreamtv.data.model.Category.Type.FINISHED;
import static com.dream.dreamtv.data.model.Category.Type.MY_LIST;
import static com.dream.dreamtv.data.model.Category.Type.TEST;
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
import static com.dream.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;


/*
 * LeanbackDetailsFragment extends DetailsSupportFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related video.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {

    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_CONTINUE_VIDEO = 2;
    private static final int ACTION_PLAY_VIDEO_FROM_BEGGINING = 3;
    private static final int ACTION_ADD_MY_LIST = 4;
    private static final int ACTION_REMOVE_MY_LIST = 5;
    private static final String BACKGROUND_DEFAULT_IMAGE = "ic_endless_constellation";
    private static final String TYPE_DRAWABLE = "drawable";

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private ArrayObjectAdapter mAdapter;
    private DetailsOverviewRow rowPresenter;

    private FirebaseAnalytics mFirebaseAnalytics;

    private VideoDetailsViewModel mViewModel;
    private Task mSelectedTask;
    private Category.Type mSelectedCategory;
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
        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(VideoDetailsViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());


        mSelectedTask = getContext().getIntent().getParcelableExtra(INTENT_TASK);

        mSelectedCategory = (Category.Type) getContext().getIntent().getSerializableExtra(INTENT_CATEGORY);


        setupDetailsOverview();
        fetchUserTasksObserver();
        fetchSubtitle();

    }

    private void setupDetailsOverview() {
        setupAdapter();
        setupDetailsOverviewRow();
        initActionPanel();
        updateBackground();

        initializePanel();
    }

    private void initializePanel() {
        if (mSelectedTask.userTasks != null && mSelectedTask.userTasks.length > 0) {
            mUserTask = mSelectedTask.userTasks[0]; //TODO what happened is there is more than one UserTask
            setupContinueAction();
        }
    }

    public FragmentActivity getContext() {
        return Objects.requireNonNull(getActivity());
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


    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getContext());
        Window window = getContext().getWindow();
//        window.setLayout(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mBackgroundManager.attach(window);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
//        mMetrics = new DisplayMetrics();
//        getContext().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void updateBackground() {
        mBackgroundManager.setDrawable(getResources().getDrawable(R.drawable.ic_video_details_background, null));


//        int resourceId = getContext().getResources()
//                .getIdentifier("dreamtv_logo",
//                        TYPE_DRAWABLE, getContext().getPackageName());
//
//        RequestOptions options = new RequestOptions()
//                .fitCenter()
//                .error(mDefaultBackground);
//
//        Glide.with(this)
//                .asBitmap()
//                .load(resourceId)
//                .apply(options)
//                .into(new SimpleTarget<Bitmap>(500, 500) {
//                    @Override
//                    public void onResourceReady(
//                            @NonNull Bitmap resource,
//                            Transition<? super Bitmap> transition) {
//                        mBackgroundManager.setBitmap(resource);
//                    }
//                });
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
                        actionsView.setBackgroundColor(ContextCompat.getColor(getContext(),
                                R.color.button_selected_shape));

                        View detailsView = viewHolder.view.findViewById(R.id.details_frame);
                        detailsView.setBackgroundColor(ContextCompat.getColor(getContext(),
                                R.color.detail_view_actionbar_background));
                        return viewHolder;
                    }
                };

        FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getContext(), VideoDetailsActivity.SHARED_ELEMENT_NAME);
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
                playVideo(true, FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN);
            } else if (action.getId() == ACTION_PLAY_VIDEO_FROM_BEGGINING) {
                playVideo(true, FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO);
            } else if (action.getId() == ACTION_CONTINUE_VIDEO) {
                playVideo(false, FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO);
            } else if (action.getId() == ACTION_ADD_MY_LIST) {
                addVideoToMyList();
            } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                removeVideoFromMyList();
            }
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
                        rowPresenter.setImageBitmap(getContext(), resource);
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
        loadingDialog = new LoadingDialog(getContext(), message);
        loadingDialog.setCanceledOnTouchOutside(false);

        if (!getContext().isFinishing())
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
            default:
                throw new RuntimeException("Event not contemplated: " + logEventName);
        }

        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    private void setupContinueAction() {
        Timber.d(mUserTask.toString());
        if (mUserTask.getTimeWatchedInSecs() > 0) { //To avoid messages like "0 min, 0 secs"
            String timeFormatted = TimeUtils.getTimeFormatMinSecs(mUserTask.getTimeWatched());

            setActionPanel(ACTION_PLAY_VIDEO,
                    new Action(ACTION_CONTINUE_VIDEO, getString(R.string.btn_continue_watching, timeFormatted)),
                    new Action(ACTION_PLAY_VIDEO_FROM_BEGGINING, getString(R.string.btn_no_from_beggining)));
        } else {
            initActionPanel();
        }
    }

    private void fetchSubtitle() {
        String subtitleVersion = getSubtitleVersion();
        //We retrieve subtitles first
        mViewModel.setSubtitleId(mSelectedTask.video.videoId,
                mSelectedTask.subLanguage, subtitleVersion);

        fetchSubtitleLiveData = mViewModel.fetchSubtitle();

        fetchSubtitleLiveData.removeObservers(getViewLifecycleOwner());

        fetchSubtitleLiveData.observe(getViewLifecycleOwner(), subtitleResponseResource -> {
            Status status = subtitleResponseResource.status;
            SubtitleResponse data = subtitleResponseResource.data;

            if (status.equals(Status.SUCCESS)) {
                Timber.d("Subtitle response");

                if (data != null && mSelectedTask.video.title.equals(data.videoTitleOriginal)) {
                    mSubtitleResponse = data;

//                    If the translated text are empty, we show the original title and description without updating them
                    if (!mSubtitleResponse.videoTitleTranslated.isEmpty())
                        mSelectedTask.videoTitleTranslated = mSubtitleResponse.videoTitleTranslated;

                    if (!mSubtitleResponse.videoDescriptionTranslated.isEmpty())
                        mSelectedTask.videoDescriptionTranslated = mSubtitleResponse.videoDescriptionTranslated;
                }

                setupDetailsOverview();

            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (subtitleResponseResource.message != null)
                    Timber.d(subtitleResponseResource.message);
                else
                    Timber.d("Status ERROR");
            }
        });

    }

    private void playVideo(boolean playFromBeginning, String logEventName) {
        if (mSubtitleResponse.subtitles == null)
            Toast.makeText(getContext(), getContext().getString(R.string.subtitle_not_found_alert), Toast.LENGTH_SHORT).show();
        else {
            //PLAY VIDEO
            if (mUserTask == null) //the are not user tasks for this video, so we need to create a new one
                createUserTask(playFromBeginning, logEventName);
            else
                goToPlaybackVideo(playFromBeginning, logEventName);
        }
    }

    private void goToPlaybackVideo(boolean playFromBeginning, String logEventName) {
        Intent intent;

        if (mSelectedTask.video.isUrlFromYoutube())
            intent = new Intent(getContext(), PlaybackVideoYoutubeActivity.class);
        else
            intent = new Intent(getContext(), PlaybackVideoActivity.class);


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
        List<VideoTest> videoTestList = mViewModel.getVideoTests();

        // We first look is the video is a test video
        for (VideoTest videoTest : videoTestList)
            if (videoTest.videoId.equals(mSelectedTask.video.videoId)) {
                return String.valueOf(videoTest.subtitleVersion);
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
            Status status = userTaskResource.status;
            UserTask data = userTaskResource.data;
            String message = userTaskResource.message;

            if (status.equals(Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_preparing_task));
            else if (status.equals(Status.SUCCESS)) {
                Timber.d("createUserTask() response");

                if (data != null && data.getTaskId() == mSelectedTask.taskId) {
                    mUserTask = data;

                    goToPlaybackVideo(playFromBeginning, logEventName);

                    mViewModel.updateTaskByCategory(CONTINUE); //update category after a new task was added
                }

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (message != null)
                    Timber.d(message);
                else
                    Timber.d("Status ERROR");

                dismissLoading();
            }

        });

    }

    /**
     * Verify if the current task has already data created. IF not, is call createTask()
     */
    private void fetchUserTasksObserver() {
        fetchUserTaskLiveData = mViewModel.fetchUserTask();

        fetchUserTaskLiveData.removeObservers(getViewLifecycleOwner());

        fetchUserTaskLiveData.observe(getViewLifecycleOwner(), userTaskResource -> {
            Status status = userTaskResource.status;
            UserTask data = userTaskResource.data;

            if (status.equals(Status.SUCCESS)) {
                Timber.d("responseFromFetchUserTaskErrorDetails response");

                if (data != null) {
                    if (mSelectedTask.taskId == data.getTaskId()) { //TODO this is a bug of the livedata. It keeps getting the last saved value from different calls
                        mUserTask = data;

                        setupContinueAction();

                        if (mUserTask.getCompleted() == VIDEO_COMPLETED_WATCHING_TRUE) { //After the user finished a video, we update all categories
                            mViewModel.updateTaskByCategory(CONTINUE); //trying to keep continue_category always updated
                            mViewModel.updateTaskByCategory(FINISHED); //trying to keep finished_category always updated
                            mViewModel.updateTaskByCategory(MY_LIST); //trying to keep mylist_category always updated
                            mViewModel.updateTaskByCategory(ALL); //trying to keep all_category always updated
                            mViewModel.updateTaskByCategory(TEST); //trying to keep test_category always updated
                        } else {
                            mViewModel.updateTaskByCategory(CONTINUE); //trying to keep continue_category always updated
                            mViewModel.updateTaskByCategory(mSelectedCategory); //we update only the current video category
                        }
                    }
                }
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (userTaskResource.message != null)
                    Timber.d(userTaskResource.message);
                else
                    Timber.d("Status ERROR");
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
            Status status = booleanResource.status;
            if (status.equals(Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_add_to_list));
            else if (status.equals(Status.SUCCESS)) {
                Timber.d("addVideoToMyList() response");

                setActionPanel(ACTION_ADD_MY_LIST,
                        new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_to_my_list)));


                mViewModel.updateTaskByCategory(MY_LIST); //update category after a new task was added

                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN);


                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (booleanResource.message != null)
                    Timber.d(booleanResource.message);
                else
                    Timber.d("Status ERROR");

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
            Status status = booleanResource.status;
            if (status.equals(Status.LOADING))
                instantiateAndShowLoading(getString(R.string.title_loading_remove_from_list));
            else if (status.equals(Status.SUCCESS)) {
                Timber.d("removeVideoFromMyList() response");

                setActionPanel(ACTION_REMOVE_MY_LIST,
                        new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));


                mViewModel.updateTaskByCategory(MY_LIST); //update category after a task was removed


                //Analytics Report Event
                firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN);

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (booleanResource.message != null)
                    Timber.d(booleanResource.message);
                else
                    Timber.d("Status ERROR");

                dismissLoading();
            }
        });
    }
}
