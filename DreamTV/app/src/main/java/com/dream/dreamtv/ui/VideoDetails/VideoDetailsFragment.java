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
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserData;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.presenter.DetailsDescriptionPresenter;
import com.dream.dreamtv.ui.PlaybackVideoActivity;
import com.dream.dreamtv.ui.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.SparseArrayObjectAdapter;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));

        userData = getActivity().getIntent().getParcelableExtra(Constants.USER_DATA);
        if (userData.mSelectedTask != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            updateBackground(userData.mSelectedTask.video.thumbnail);
            //verifyIfVideoIsInMyList();
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

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_VIDEO) {
                    prepareSubtitle(userData.mSelectedTask.video);

                } else if (action.getId() == ACTION_ADD_MY_LIST) {
                    //addVideoToMyList();
                } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                   // removeVideoFromMyList();
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
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


//    private void verifyIfVideoIsInMyList() {
//
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_VIDEO_ID, userData.mSelectedTask.video_id);
//
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_verifying_list)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//                TypeToken type = new TypeToken<JsonResponseBaseBean<UserVideo[]>>() {
//                };
//                JsonResponseBaseBean<UserVideo[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
//
//
//                if (jsonResponse.data.length > 0) {
//                    SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
//                    adapter.clear(ACTION_ADD_MY_LIST);
//                    adapter.set(ACTION_REMOVE_MY_LIST, new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_from_my_list)));
//
//                }
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//            }
//        };
//
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_VIDEO_DETAILS, urlParams, responseListener, this);
//
//    }

//    private void addVideoToMyList() {
//        final Video video = new Video();
//        video.primary_audio_language_code = userData.mSelectedTask.primary_audio_language_code;
//        video.video_id = userData.mSelectedTask.video_id;
//
//
//        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), video);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_adding_videos_list)) {
//
//            @Override
//            public void processResponse(String response) {
//                Log.d(TAG, response);
//
//
//                SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
//                adapter.clear(ACTION_ADD_MY_LIST);
//                adapter.set(ACTION_REMOVE_MY_LIST, new Action(ACTION_REMOVE_MY_LIST, getString(R.string.btn_remove_from_my_list)));
//
//                ((VideoDetailsActivity) Objects.requireNonNull(getActivity())).updateScreenAfterChanges = true;
//
//                //Analytics Report Event
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, video.video_id);
//                bundle.putString(Constants.FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE, video.primary_audio_language_code);
//                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN, bundle);
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//            }
//        };
//
//        NetworkDataSource.post(getActivity(), NetworkDataSource.Urls.USER_VIDEOS, null, jsonRequest, responseListener, this);
//
//    }

//    private void removeVideoFromMyList() {
//
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_VIDEO_ID, userData.mSelectedTask.video_id);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_removing_videos_list)) {
//
//            @Override
//            public void processResponse(String response) {
//                Log.d(TAG, response);
//
//                SparseArrayObjectAdapter adapter = (SparseArrayObjectAdapter) rowPresenter.getActionsAdapter();
//                adapter.clear(ACTION_REMOVE_MY_LIST);
//                adapter.set(ACTION_ADD_MY_LIST, new Action(ACTION_ADD_MY_LIST, getString(R.string.btn_add_to_my_list)));
//
//                ((VideoDetailsActivity) Objects.requireNonNull(getActivity())).updateScreenAfterChanges = true;
//
//                //Analytics Report Event
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.FIREBASE_KEY_VIDEO_ID, userData.mSelectedTask.video_id);
//                mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN, bundle);
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//            }
//        };
//
//        NetworkDataSource.delete(getActivity(), NetworkDataSource.Urls.USER_VIDEOS, urlParams, responseListener, this);
//
//    }

    private void prepareSubtitle(Video mSelectedVideo) {
        String mode = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getTestingMode();

        //Testing mode true
        if (mode.equals(getString(R.string.text_yes_option)))
            getVideoTests(mSelectedVideo);
        else
            getSubtitleJson(mSelectedVideo, 0);


    }

    private void getVideoTests(final Video selectedVideo) {

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
                getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                Log.d(TAG, response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTests[]>>() {
                };
                JsonResponseBaseBean<VideoTests[]> jsonResponse = JsonUtils.getJsonResponse(response, type);

                VideoTests[] videoTests = jsonResponse.data;
                Log.d(TAG, Arrays.toString(videoTests));

                int videoTestIndex = 0;
                for (int i = 0; i < videoTests.length; i++) {
                    if (videoTests[i].video_id.equals(selectedVideo.video_id)) {
                        videoTestIndex = i;
                        break;
                    }
                }

                getSubtitleJson(selectedVideo, videoTests[videoTestIndex].version);


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

       // NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.VIDEO_TESTS, null, responseListener, this);
    }


    private void getSubtitleJson(final Video video, int version) {
        User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();

        //sharing mode
//        final String sharingMode = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getSharingMode();


        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_VIDEO_ID, video.video_id);
        urlParams.put(PARAM_LANGUAGE_CODE, (user.sub_language != null &&
                !user.sub_language.equals(Constants.NONE_OPTIONS_CODE)) ?
                user.sub_language : userData.mSelectedTask.language);

        if (version > 0)  //if version == 0, no need to pass version parameter, For default is fetched last subtitle versions
            urlParams.put(PARAM_VERSION, String.valueOf(version));


        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
                getString(R.string.title_loading_retrieve_subtitle)) {

            @Override
            public void processResponse(String response) {
                Log.d(TAG, response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleResponse>>() {
                };
                JsonResponseBaseBean<SubtitleResponse> jsonResponse = JsonUtils.getJsonResponse(response, type);

                userData.subtitle_json = jsonResponse.data;

                if (userData.subtitle_json != null && userData.subtitle_json.subtitles != null) { //Si se encontraron los subtitulos, vamos a la pantalla de reproduccion
                    Log.d(TAG, jsonResponse.data.toString());
                    //verify the type of task. We get the data from users tasks
                    switch (userData.mSelectedTask.category) {
                        case Constants.TASKS_ALL:
//                            if (sharingMode.equals(getString(R.string.text_no_option)))
                                goToPlayVideo();
//                            else if (sharingMode.equals(getString(R.string.text_yes_option)))
//                                getOtherTasksForThisVideo();
                            break;
                        case Constants.TASKS_CONTINUE:
                            getMyTaskForThisVideo();
                            break;
                        default:
                            goToPlayVideo();
                            break;
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_subtitle_not_found), Toast.LENGTH_SHORT).show();
                }

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

//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.SUBTITLE, urlParams, responseListener, this);

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

//    private void getOtherTasksForThisVideo() {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_TASK_ID, String.valueOf(userData.mSelectedTask.task_id));
//        urlParams.put(PARAM_TYPE, Constants.TASKS_OTHER_USERS);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask[]>>() {
//                };
//                JsonResponseBaseBean<UserTask[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
//                userData.userTaskList = jsonResponse.data;
//
//                goToPlayVideo();
//
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//            }
//        };
//
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_TASKS, urlParams, responseListener, this);
//
//    }


}
