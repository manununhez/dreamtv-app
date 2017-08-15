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

package com.dream.dreamtv.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.activity.VideoDetailsActivity;
import com.dream.dreamtv.activity.PlaybackVideoActivity;
import com.dream.dreamtv.activity.PlaybackVideoYoutubeActivity;
import com.dream.dreamtv.activity.PreferencesActivity;
import com.dream.dreamtv.adapter.DetailsDescriptionPresenter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.SubtitleJson;
import com.dream.dreamtv.beans.Task;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.beans.UserTaskList;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.Utils;
import com.google.gson.Gson;

import java.util.List;


/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_PLAY_VIDEO = 1;
    //    private static final int ACTION_CHOOSE_SUBTITLE_LANG = 2;
    private static final int ACTION_ADD_MY_LIST = 3;
    private static final int ACTION_REMOVE_MY_LIST = 4;
    private static final int CHOOSE_SUB_LANG_CODE = 152;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

//    private static final int NUM_COLS = 10;

    private Video mSelectedVideo;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRow rowPresenter;

    //    private String selectedLanguageCode = "";
    private String LAST_VERSION = "last";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        mSelectedVideo = (Video) getActivity().getIntent()
                .getParcelableExtra(VideoDetailsActivity.VIDEO);
        if (mSelectedVideo != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
//            setupMovieListRow();
//            setupMovieListRowPresenter();
            updateBackground(mSelectedVideo.thumbnail);
            verifyIfVideoIsInMyList();
//            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            getActivity().finish(); //back to MainActivity
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
//        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }


    private void setupDetailsOverviewRow() {
//        Log.d(TAG, "doInBackground: " + mSelectedVideo.toString());
        rowPresenter = new DetailsOverviewRow(mSelectedVideo);
        rowPresenter.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);

        Glide.with(getActivity())
                .load(mSelectedVideo.thumbnail)
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
//                        Log.d(TAG, "details overview card image url ready: " + resource);
                        rowPresenter.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        Action actionPlay = new Action(ACTION_PLAY_VIDEO, getResources().getString(
                R.string.play_video));
        Action actionAdd = new Action(ACTION_ADD_MY_LIST, getResources().getString(
                R.string.add_to_my_list));
//        actionPlay.setLabel2("");
//        actionAdd.setLabel2("");
//        actionPlay.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_circle_outline));
//        actionAdd.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_add_circle_outline));

        rowPresenter.addAction(actionPlay);
//        rowPresenter.addAction(new Action(ACTION_CHOOSE_SUBTITLE_LANG, getResources().getString(
//                R.string.dialog_choose_language_title)));
        rowPresenter.addAction(actionAdd);

        mAdapter.add(rowPresenter);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                VideoDetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_VIDEO) {
                    getSubtitleJson(mSelectedVideo);


//                } else if (action.getId() == ACTION_CHOOSE_SUBTITLE_LANG) {
//                    Intent intent = new Intent(getActivity(), DialogChooseLanguageActivity.class);
//                    intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
//                    startActivityForResult(intent, CHOOSE_SUB_LANG_CODE);
                } else if (action.getId() == ACTION_ADD_MY_LIST) {
                    addVideoToMyList();
                } else if (action.getId() == ACTION_REMOVE_MY_LIST) {
                    removeVideoFromMyList();
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }


//    private void getSubtitleVtt(final Video video) {
//        String url = ConnectionManager.Urls.VIDEOS.value + '/' + video.id + "/languages/" + (selectedLanguageCode.isEmpty() ? video.languages.get(1).code : selectedLanguageCode) + "/subtitles";
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("sub_format", "vtt");
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving subtitle...") {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                DreamTVApp.Logger.d(response);
//                SubtitleVtt mSubtitleVtt = gson.fromJson(response, SubtitleVtt.class);
//                DreamTVApp.Logger.d(mSubtitleVtt.toString());
//
//                mSelectedVideo.subtitle_vtt = mSubtitleVtt;
//                getSubtitleJson(mSelectedVideo);
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                DreamTVApp.Logger.d(error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                DreamTVApp.Logger.d(jsonResponse.toString());
//            }
//        };
//
//        ConnectionManager.get(getActivity(), url, urlParams, responseListener, this);
//
//    }

    private void verifyIfVideoIsInMyList() {
        Task task = new Task();
        task.video_id = mSelectedVideo.id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Verifying is the current video is in my list...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                if (!Boolean.valueOf(response)) {
                    List<Action> actionList = rowPresenter.getActions();
                    for (Action action : actionList) { //We change the actions values
                        if (action.getId() == ACTION_ADD_MY_LIST) {
                            rowPresenter.removeAction(action);
                            rowPresenter.addAction(new Action(ACTION_REMOVE_MY_LIST, "- Remove from my list"));
                        }

                    }
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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_VIDEOS_INFO, null, jsonRequest, responseListener, this);

    }

    private void addVideoToMyList() {
        Task task = new Task();
        task.video_id = mSelectedVideo.id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Adding video to my list...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                List<Action> actionList = rowPresenter.getActions();
                for (Action action : actionList) { //We change the actions values
                    if (action.getId() == ACTION_ADD_MY_LIST) {
                        rowPresenter.removeAction(action);
                        rowPresenter.addAction(new Action(ACTION_REMOVE_MY_LIST, "- Remove from my list"));
                    }

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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_VIDEOS_CREATE, null, jsonRequest, responseListener, this);

    }

    private void removeVideoFromMyList() {
        Task task = new Task();
        task.video_id = mSelectedVideo.id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Removing video to my list...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                List<Action> actionList = rowPresenter.getActions();
                for (Action action : actionList) { //We change the actions values
                    if (action.getId() == ACTION_REMOVE_MY_LIST) {
                        rowPresenter.removeAction(action);
                        rowPresenter.addAction(new Action(ACTION_ADD_MY_LIST, getString(R.string.add_to_my_list)));
                    }

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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_VIDEOS_DELETE, null, jsonRequest, responseListener, this);

    }


    private void getSubtitleJson(final Video video) {
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        SubtitleJson subtitleJson = new SubtitleJson();
        subtitleJson.video_id = video.id;
        subtitleJson.version = LAST_VERSION;
        subtitleJson.language_code = (user.sub_language != null && !user.sub_language.equals(PreferencesActivity.NONE_OPTIONS_CODE)) ? user.sub_language
                : video.subtitle_language;

        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), subtitleJson);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
                "Retrieving subtitle...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                SubtitleJson mSubtitleJson = gson.fromJson(response, SubtitleJson.class);
                DreamTVApp.Logger.d(mSubtitleJson.toString());
                mSelectedVideo.subtitle_json = mSubtitleJson;

                //verify the type of task. We get the data from users tasks
                if (mSelectedVideo.task_state == MainFragment.CHECK_NEW_TASKS_CATEGORY) {
                    getOtherTasksForThisVideo();
                } else if ((mSelectedVideo.task_state == MainFragment.SEE_AGAIN_CATEGORY)) {
                    getMyTaskForThisVideo();
                } else {
                    goToPlayVideo();
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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.SUBTITLE, null, jsonRequest, responseListener, this);

    }

    private void goToPlayVideo() {
        Intent intent;

        if (mSelectedVideo.isFromYoutube()) {
//                        intent = new Intent(getActivity(), YoutubeActivityFragment.class);
            intent = new Intent(getActivity(), PlaybackVideoYoutubeActivity.class);

        } else {
            intent = new Intent(getActivity(), PlaybackVideoActivity.class);
        }

        intent.putExtra(VideoDetailsActivity.VIDEO, mSelectedVideo);
        startActivity(intent);
    }


    private void getMyTaskForThisVideo() {
        Task task = new Task();
        task.task_id = mSelectedVideo.task_id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d("Tasks -> Mine: " + response);

                mSelectedVideo.userTaskList = gson.fromJson(response, UserTask[].class);

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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_TASKS_MY_TASKS, null, jsonRequest, responseListener, this);

    }

    private void getOtherTasksForThisVideo() {
        Task task = new Task();
        task.task_id = mSelectedVideo.task_id;


        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d("Tasks -> Others: " + response);

                mSelectedVideo.userTaskList = gson.fromJson(response, UserTask[].class);

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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_TASKS_OTHER_USER_TASKS, null, jsonRequest, responseListener, this);

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK)
//            if (requestCode == CHOOSE_SUB_LANG_CODE) {
//                selectedLanguageCode = data.getStringExtra("selectedCodeLanguage");
//            }
//
//    }

    //    private void setupMovieListRow() {
//        String subcategories[] = {getString(R.string.related_movies), "Mas videos", "More movies", "Documentaries"};
//        List<Movie> list = MovieList.list;
//
//        int i;
//        for (i = 0; i < subcategories.length; i++) {
//            if (i != 0) {
//                Collections.shuffle(list);
//            }
//            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//            for (int j = 0; j < NUM_COLS; j++) {
//                listRowAdapter.add(list.get(j % 5));
//            }
//            HeaderItem header = new HeaderItem(i, subcategories[i]);
//            mAdapter.add(new ListRow(header, listRowAdapter));
//        }
//    }

//    private void setupMovieListRowPresenter() {
//        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
//    }

//    private final class ItemViewClickedListener implements OnItemViewClickedListener {
//        @Override
//        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
//                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
//
//            if (item instanceof Video) {
//                Video video = (Video) item;
//                Log.d(TAG, "Item: " + item.toString());
//                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
//                intent.putExtra(getResources().getString(R.string.movie), mSelectedVideo);
//                intent.putExtra(getResources().getString(R.string.should_start), true);
//
//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);
//            }
//        }
//    }
}
