/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dream.dreamtv.ui.VideoDetails;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.presenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.ui.PlaybackVideoActivity;
import com.dream.dreamtv.ui.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.utils.InjectorUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Objects;

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
import androidx.leanback.widget.SparseArrayObjectAdapter;
import androidx.lifecycle.ViewModelProviders;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_DURATION;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_ID;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_VIDEO_PROJECT_NAME;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_TASK;
import static com.dream.dreamtv.utils.Constants.INTENT_USER_DATA_TASK_ERRORS;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;


/*
 * LeanbackDetailsFragment extends DetailsSupportFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {
    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_ADD_MY_LIST = 3;
    private static final int ACTION_REMOVE_MY_LIST = 4;
    private static final String TAG = "VideoDetailsFragment";
    private ArrayObjectAdapter mAdapter;
    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRow rowPresenter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private VideoDetailsViewModel mViewModel;
    private TaskEntity mSelectedTask;
    private SubtitleResponse mSubtitleResponse;
    private UserTask mUserTaskErrorsDetails;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        // Get the ViewModel from the factory
        VideoDetailsViewModelFactory factory = InjectorUtils.provideVideoDetailsViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(VideoDetailsViewModel.class);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        mSelectedTask = getActivity().getIntent().getParcelableExtra(INTENT_USER_DATA_TASK);

        if (mSelectedTask != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            updateBackground(mSelectedTask.video.thumbnail);
            verifyIfVideoIsInMyList();
            prepareSubtitle();
            getMyTaskForThisVideo();
        } else {
            getActivity().finish(); //back to MainActivity
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewModel.responseFromFetchSubtitle().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFetchUserTaskErrorDetails().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromCreateUserTask().removeObservers(getViewLifecycleOwner());
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void updateBackground(String uri) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(mDefaultBackground);

        Glide.with(this)
                .asBitmap()
                .load(uri)
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
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());


        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.selected_background));
        detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);
        detailsPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

        detailsPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == ACTION_PLAY_VIDEO) {
//                prepareSubtitle(userData.mSelectedTask);
                goToPlayVideo();
            } else if (action.getId() == ACTION_ADD_MY_LIST) {
                addVideoToMyList();
            } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                removeVideoFromMyList();
            } else {
                Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        ClassPresenterSelector mPresenterSelector = new ClassPresenterSelector();
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
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

        SparseArrayObjectAdapter adapter = new SparseArrayObjectAdapter();


        adapter.set(ACTION_PLAY_VIDEO, new Action(ACTION_PLAY_VIDEO, getResources().getString(R.string.btn_play_video)));
        adapter.set(ACTION_ADD_MY_LIST, new Action(ACTION_ADD_MY_LIST, getResources().getString(R.string.btn_add_to_my_list)));


        rowPresenter.setActionsAdapter(adapter);
        mAdapter.add(rowPresenter);
    }

    private void verifyIfVideoIsInMyList() {
        mViewModel.verifyIfTaskIsInList(mSelectedTask).observe(getViewLifecycleOwner(), taskEntity -> {
            SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
            if (taskEntity != null) {
                adapter.clear(ACTION_ADD_MY_LIST);
                adapter.set(ACTION_REMOVE_MY_LIST, new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_to_my_list)));
            } else {
                adapter.clear(ACTION_REMOVE_MY_LIST);
                adapter.set(ACTION_ADD_MY_LIST, new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));
            }
        });

    }

    private void addVideoToMyList() {
        //TODO bug fix, when remove the last item of the row, and then add a new task, the livedata receive an extra wrong value and it is added to the list
        mViewModel.requestAddToList(mSelectedTask);

        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN, bundle);


    }

    private void removeVideoFromMyList() {
        //TODO bug fix, when remove the last item of the row, and then add a new task, the livedata receive an extra wrong value and it is added to the list
        mViewModel.requestRemoveFromList(mSelectedTask);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN, bundle);
    }

    private void prepareSubtitle() {

        getSubtitleJson(getSubtitleVersion());

    }


    private int getSubtitleVersion() {
        int newestVersion = 0;
        DreamTVApp dreamTVApp = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
        List<VideoTests> videoTestsList = dreamTVApp.getVideoTests();

        if (mSelectedTask.category.equals(TASKS_TEST_CAT)) {
            //We find the version of the video test
            for (VideoTests videoTests : videoTestsList)
                if (videoTests.videoId.equals(mSelectedTask.video.videoId)) {
                    return videoTests.version;
                }
        }

        return newestVersion;
    }

    private void getSubtitleJson(int subtitleVersion) {
//        Request
        mViewModel.fetchSubtitle(mSelectedTask.video.videoId, mSelectedTask.language, subtitleVersion);

//        Response
        mViewModel.responseFromFetchSubtitle().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFetchSubtitle().observe(getViewLifecycleOwner(), subtitleResponseResource -> {
            if (subtitleResponseResource.status.equals(Resource.Status.SUCCESS)) {
                mSubtitleResponse = subtitleResponseResource.data;

                Log.d(TAG, "Subtitle response");
            } else if (subtitleResponseResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (subtitleResponseResource.message != null)
                    Log.d(TAG, subtitleResponseResource.message);
                else
                    Log.d(TAG, "Status ERROR");
            }

        });


    }


    private void goToPlayVideo() {
        if (mSubtitleResponse == null)
            Toast.makeText(getActivity(), "Subtitle not found.", Toast.LENGTH_SHORT).show();
        else if (mUserTaskErrorsDetails == null)
            createUserTask();
        else
            playVideo();
    }


    private void createUserTask() {
        //        Request
        mViewModel.createUserTask(mSelectedTask, mSubtitleResponse.versionNumber);

        //        Response
        mViewModel.responseFromCreateUserTask().removeObservers(getViewLifecycleOwner());

        mViewModel.responseFromCreateUserTask().observe(getViewLifecycleOwner(), userTaskResource -> {
            if (userTaskResource.status.equals(Resource.Status.SUCCESS)) {
                mUserTaskErrorsDetails = userTaskResource.data;

                goToPlayVideo();

                Log.d(TAG, "createUserTask() response");
            } else if (userTaskResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (userTaskResource.message != null)
                    Log.d(TAG, userTaskResource.message);
                else
                    Log.d(TAG, "Status ERROR");
            }
        });

    }

    private void playVideo() {
        //TODO put loading until subtitles and other are retrieved. You cant play video until that
        Intent intent;

        if (mSelectedTask.video.isUrlFromYoutube())
            intent = new Intent(getActivity(), PlaybackVideoYoutubeActivity.class);
        else
            intent = new Intent(getActivity(), PlaybackVideoActivity.class);


        intent.putExtra(INTENT_USER_DATA_TASK, mSelectedTask);
        intent.putExtra(INTENT_USER_DATA_SUBTITLE, mSubtitleResponse); //TODO si no hay subtitulo, no deberia avanzar a la sgte pantalla
        intent.putExtra(INTENT_USER_DATA_TASK_ERRORS, mUserTaskErrorsDetails); //@NULLABLE puede no tener errores ya marcados
        startActivity(intent);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(FIREBASE_KEY_VIDEO_ID, mSelectedTask.video.videoId);
        bundle.putString(FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedTask.video.primaryAudioLanguageCode);
        bundle.putString(FIREBASE_KEY_VIDEO_PROJECT_NAME, mSelectedTask.video.project);
        bundle.putLong(FIREBASE_KEY_VIDEO_DURATION, mSelectedTask.video.getVideoDurationInMs());
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN, bundle);
    }

    /**
     * Verify if the current task has already data created. IF not, is call createTask()
     */
    private void getMyTaskForThisVideo() {
        //        Request
        mViewModel.fetchUserTaskErrorDetails(mSelectedTask.taskId);

        //        Response
        mViewModel.responseFromFetchUserTaskErrorDetails().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFetchUserTaskErrorDetails().observe(getViewLifecycleOwner(), userTaskResource -> {
            if (userTaskResource.status.equals(Resource.Status.SUCCESS)) {
                mUserTaskErrorsDetails = userTaskResource.data;

                Log.d(TAG, "responseFromFetchUserTaskErrorDetails response");
            } else if (userTaskResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (userTaskResource.message != null)
                    Log.d(TAG, userTaskResource.message);
                else
                    Log.d(TAG, "Status ERROR");
            }
        });
    }


}
