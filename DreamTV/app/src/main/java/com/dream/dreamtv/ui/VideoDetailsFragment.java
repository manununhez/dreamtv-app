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

package com.dream.dreamtv.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsSupportFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.content.ContextCompat;
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
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.SubtitleJson;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserVideo;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.network.ConnectionManager;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.presenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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
    private static final String TESTING_VIDEO_ID_1 = "tNE5imiv27uA";
    private static final String TESTING_VIDEO_ID_2 = "DJlZ5QYcHSQB";
    private static final String TESTING_VIDEO_ID_3 = "eC0ZoBNXwcwA";
    private static final String TESTING_VIDEO_ID_4 = "cYjdKCNfh989";
    private static final String TESTING_VIDEO_ID_5 = "8ULN8kSqfMkk";
    private static final String TESTING_VIDEO_ID_6 = "MNMcyyZBTLUc";
    private static final String TESTING_VIDEO_VERSION_NUMBER_1 = "4";
    private static final String TESTING_VIDEO_VERSION_NUMBER_2 = "6";
    private static final String TESTING_VIDEO_VERSION_NUMBER_3 = "11";
    private static final String TESTING_VIDEO_VERSION_NUMBER_4 = "14";
    private static final String TESTING_VIDEO_VERSION_NUMBER_5 = "8";
    private static final String TESTING_VIDEO_VERSION_NUMBER_6 = "14";
    private Video mSelectedVideo;
    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRow rowPresenter;
    private FullWidthDetailsOverviewSharedElementHelper mHelper;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mSelectedVideo = getActivity().getIntent()
                .getParcelableExtra(Constants.VIDEO);
        if (mSelectedVideo != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            updateBackground(mSelectedVideo.thumbnail);
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
                            Bitmap resource,
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
                ContextCompat.getColor(getActivity(), R.color.selected_background));
        detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);

        // Hook up transition element.
        mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(),
                Constants.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(mHelper);
        detailsPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_VIDEO) {
                    getSubtitleJson(mSelectedVideo);

                } else if (action.getId() == ACTION_ADD_MY_LIST) {
                    addVideoToMyList();
                } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                    removeVideoFromMyList();
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPresenterSelector = new ClassPresenterSelector();
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        rowPresenter = new DetailsOverviewRow(mSelectedVideo);


        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background)
                .dontAnimate();

        Glide.with(this)
                .asBitmap()
                .load(mSelectedVideo.thumbnail)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(
                            Bitmap resource,
                            Transition<? super Bitmap> transition) {
                        rowPresenter.setImageBitmap(getActivity(), resource);
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

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_VIDEO_ID, mSelectedVideo.id);


        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_verifying_list)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TypeToken type = new TypeToken<JsonResponseBaseBean<UserVideo[]>>() {
                };
                JsonResponseBaseBean<UserVideo[]> jsonResponse = JsonUtils.getJsonResponse(response, type);


                if (jsonResponse.data.length > 0) {
                    SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
                    adapter.clear(ACTION_ADD_MY_LIST);
                    adapter.set(ACTION_REMOVE_MY_LIST, new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_from_my_list)));

                }

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_VIDEO_DETAILS, urlParams, responseListener, this);

    }

    private void addVideoToMyList() {
        final Video video = new Video();
        video.primary_audio_language_code = mSelectedVideo.primary_audio_language_code;
        video.original_language = mSelectedVideo.original_language;
        video.video_id = mSelectedVideo.id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), video);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_adding_videos_list)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);


                SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
                adapter.clear(ACTION_ADD_MY_LIST);
                adapter.set(ACTION_REMOVE_MY_LIST, new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_from_my_list)));

                ((VideoDetailsActivity) Objects.requireNonNull(getActivity())).updateScreenAfterChanges = true;

                //Analytics Report Event
                Bundle bundle = new Bundle();
                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, video.video_id);
                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, video.primary_audio_language_code);
                bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, video.original_language);
                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN, bundle);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_VIDEOS, null, jsonRequest, responseListener, this);

    }

    private void removeVideoFromMyList() {

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_VIDEO_ID, mSelectedVideo.id);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_removing_videos_list)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);

                SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
                adapter.clear(ACTION_REMOVE_MY_LIST);
                adapter.set(ACTION_ADD_MY_LIST, new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));

                ((VideoDetailsActivity) Objects.requireNonNull(getActivity())).updateScreenAfterChanges = true;

                //Analytics Report Event
                Bundle bundle = new Bundle();
                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.id);
                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN, bundle);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.delete(getActivity(), ConnectionManager.Urls.USER_VIDEOS, urlParams, responseListener, this);

    }


    private void getSubtitleJson(final Video video) {
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_VIDEO_ID, video.id);
        urlParams.put(PARAM_VERSION, PARAM_LAST);
        urlParams.put(PARAM_LANGUAGE_CODE, (user.sub_language != null &&
                !user.sub_language.equals(Constants.NONE_OPTIONS_CODE)) ?
                user.sub_language : video.subtitle_language);

        //Testing mode true
        if (mode != null && mode.equals(getString(R.string.text_yes_option)))
            switch (video.id) {
                case TESTING_VIDEO_ID_1:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_1);
                    break;
                case TESTING_VIDEO_ID_2:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_2);
                    break;
                case TESTING_VIDEO_ID_3:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_3);
                    break;
                case TESTING_VIDEO_ID_4:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_4);
                    break;
                case TESTING_VIDEO_ID_5:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_5);
                    break;
                case TESTING_VIDEO_ID_6:
                    urlParams.put(PARAM_VERSION_NUMBER, TESTING_VIDEO_VERSION_NUMBER_6);
                    break;
            }

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
                getString(R.string.title_loading_retrieve_subtitle)) {

            @Override
            public void processResponse(String response) {
                DreamTVApp.Logger.d(response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleJson>>() {
                };
                JsonResponseBaseBean<SubtitleJson> jsonResponse = JsonUtils.getJsonResponse(response, type);

                mSelectedVideo.subtitle_json = jsonResponse.data;

                if (mSelectedVideo.subtitle_json != null && mSelectedVideo.subtitle_json.subtitles != null) { //Si se encontraron los subtitulos, vamos a la pantalla de reproduccion
                    DreamTVApp.Logger.d(jsonResponse.data.toString());
                    //verify the type of task. We get the data from users tasks
                    if (mSelectedVideo.task_state == Constants.CHECK_NEW_TASKS_CATEGORY) {
                        getOtherTasksForThisVideo();
                    } else if ((mSelectedVideo.task_state == Constants.CONTINUE_WATCHING_CATEGORY)) {
                        getMyTaskForThisVideo();
                    } else {
                        goToPlayVideo();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_subtitle_not_found), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.SUBTITLE, urlParams, responseListener, this);

    }

    private void goToPlayVideo() {
        Intent intent;

        if (mSelectedVideo.isFromYoutube()) {
            intent = new Intent(getActivity(), PlaybackVideoYoutubeActivity.class);

        } else {
            intent = new Intent(getActivity(), PlaybackVideoActivity.class);
        }

        intent.putExtra(Constants.VIDEO, mSelectedVideo);
        startActivity(intent);

        //Analytics Report Event
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, mSelectedVideo.id);
        bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, mSelectedVideo.primary_audio_language_code);
        bundle.putString(Constants.FIREBASE_KEY_ORIGINAL_LANGUAGE, mSelectedVideo.original_language);
        bundle.putString(Constants.FIREBASE_KEY_VIDEO_PROJECT_NAME, mSelectedVideo.project);
        bundle.putLong(Constants.FIREBASE_KEY_VIDEO_DURATION, mSelectedVideo.getVideoDurationInMs());
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN, bundle);
    }


    private void getMyTaskForThisVideo() {

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_TASK_ID, String.valueOf(mSelectedVideo.task_id));
        urlParams.put(PARAM_TYPE, Constants.TASKS_USER);


        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_tasks)) {

            @Override
            public void processResponse(String response) {
//                Gson gson = new Gson();
                DreamTVApp.Logger.d("Tasks -> Mine: " + response);
                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask[]>>() {
                };
                JsonResponseBaseBean<UserTask[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
                mSelectedVideo.userTaskList = jsonResponse.data;

                goToPlayVideo();


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS, urlParams, responseListener, this);

    }

    private void getOtherTasksForThisVideo() {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_TASK_ID, String.valueOf(mSelectedVideo.task_id));
        urlParams.put(PARAM_TYPE, Constants.TASKS_OTHER_USERS);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_tasks)) {

            @Override
            public void processResponse(String response) {

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask[]>>() {
                };
                JsonResponseBaseBean<UserTask[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
                mSelectedVideo.userTaskList = jsonResponse.data;

                goToPlayVideo();


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS, urlParams, responseListener, this);

    }


}
