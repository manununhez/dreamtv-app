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

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.UserData;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.presenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.ui.PlaybackVideoActivity;
import com.dream.dreamtv.ui.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.SparseArrayObjectAdapter;
import androidx.lifecycle.ViewModelProviders;


/*
 * LeanbackDetailsFragment extends DetailsSupportFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {
    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_ADD_MY_LIST = 3;
    private static final int ACTION_REMOVE_MY_LIST = 4;
    private static final String TAG = "VideoDetailsFragment";
    private static final String PARAM_LAST = "last";
    private static final String PARAM_VIDEO_ID = "video_id";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_LANGUAGE_CODE = "language_code";
    private static final String PARAM_VERSION_NUMBER = "version_number";
    private static final String PARAM_TASK_ID = "task_id";
    private static final String PARAM_TYPE = "type";
    private UserData userData;
    private ArrayObjectAdapter mAdapter;
    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRow rowPresenter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private VideoDetailsViewModel mViewModel;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        // Get the ViewModel from the factory
        VideoDetailsViewModelFactory factory = InjectorUtils.provideVideoDetailsViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(VideoDetailsViewModel.class);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        userData = getActivity().getIntent().getParcelableExtra(Constants.USER_DATA);

        if (userData.mSelectedTask != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            updateBackground(userData.mSelectedTask.video.thumbnail);
            verifyIfVideoIsInMyList();
        } else {
            getActivity().finish(); //back to MainActivity
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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

        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(),
                Constants.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(mHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

        detailsPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == ACTION_PLAY_VIDEO) {
                prepareSubtitle(userData.mSelectedTask);

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
        rowPresenter = new DetailsOverviewRow(userData.mSelectedTask);

        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background)
                .dontAnimate();

        Glide.with(this)
                .asBitmap()
                .load(userData.mSelectedTask.video.thumbnail)
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

        mViewModel.verifyIfTaskIsInList(userData.mSelectedTask).observe(getViewLifecycleOwner(), taskEntity -> {
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
        mViewModel.requestAddToList(userData.mSelectedTask);
        //TODO bug fix, when remove the last item of the row, and then add a new task, the livedata receive an extra wrong value and it is added to the list

        //                Bundle bundle = new Bundle();
//                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, video.video_id);
//                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, video.primary_audio_language_code);
//                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN, bundle);


    }

    private void removeVideoFromMyList() {
        mViewModel.requestRemoveFromList(userData.mSelectedTask);

        //TODO bug fix, when remove the last item of the row, and then add a new task, the livedata receive an extra wrong value and it is added to the list
        //Analytics Report Event
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, userData.mSelectedTask.video_id);
//                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN, bundle);
    }

    private void prepareSubtitle(TaskEntity taskEntity) {
        DreamTVApp dreamTVApp = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
        List<VideoTests> videoTestsList = dreamTVApp.getVideoTests();

        if (taskEntity.category.equals(Constants.TASKS_TEST)) {
            //We find the version of the video test
            for (VideoTests videoTests : videoTestsList)
                if (videoTests.video_id.equals(taskEntity.video.video_id)) {
                    getSubtitleJson(taskEntity, videoTests.version);
                    break;
                }
        } else
            getSubtitleJson(taskEntity, 0);

    }


    private void getSubtitleJson(TaskEntity taskEntity, int version) {

//        Request
        mViewModel.fetchSubtitle(taskEntity.video.video_id, taskEntity.language, version);

//        Response
        mViewModel.responseFromFetchSubtitle().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFetchSubtitle().observe(getViewLifecycleOwner(), subtitleResponseResource -> {
            if (subtitleResponseResource.status.equals(Resource.Status.SUCCESS)) {
                getMyTaskForThisVideo(subtitleResponseResource.data);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewModel.responseFromFetchSubtitle().removeObservers(getViewLifecycleOwner());
    }

    private void goToPlayVideo() {
        Intent intent;

        if (userData.mSelectedTask.video.isUrlFromYoutube()) {
            intent = new Intent(getActivity(), PlaybackVideoYoutubeActivity.class);

        } else {
            intent = new Intent(getActivity(), PlaybackVideoActivity.class);
        }

        intent.putExtra(Constants.USER_DATA, userData);
        startActivity(intent);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, userData.mSelectedTask.video.video_id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, userData.mSelectedTask.video.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_PROJECT_NAME, userData.mSelectedTask.video.project);
        bundle.putLong(Constants.FIREBASE_KEY_VIDEO_DURATION, userData.mSelectedTask.video.getVideoDurationInMs());
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN, bundle);
    }


    private void getMyTaskForThisVideo() {

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_TASK_ID, String.valueOf(userData.mSelectedTask.task_id));
        urlParams.put(PARAM_TYPE, Constants.TASKS_USER);


        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_tasks)) {

            @Override
            public void processResponse(String response) {
//                Gson gson = new Gson();
                Log.d(TAG, "Tasks -> Mine: " + response);
                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask[]>>() {
                };
                JsonResponseBaseBean<UserTask[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
                userData.userTaskList = jsonResponse.data;

                goToPlayVideo();


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                Log.d(TAG, error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                Log.d(TAG, jsonResponse.toString());
            }
        };

//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_TASKS, urlParams, responseListener, this);

    }


}
