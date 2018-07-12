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

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.activity.PreferencesActivity;
import com.dream.dreamtv.activity.SeeAllActivity;
import com.dream.dreamtv.activity.VideoDetailsActivity;
import com.dream.dreamtv.adapter.VideoCardPresenter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Task;
import com.dream.dreamtv.beans.TaskList;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = "MainFragment";
    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1456;
    private static final int PREFERENCES_SETTINGS_RESULT_CODE = 1256;
    private static final int VIDEO_DETAILS_RESULT_CODE = 1257;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();
        setupVideosList();

        userRegistration();
        setupEventListeners();


    }

    private void userRegistration() {

        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else //the user has already has an account. Proceed to get the videos
            getTasks(); //for the mainscreen, only the first page

    }

    private void createUserAccount(String accountName, String accountType) {
        User user = new User();
        user.name = accountName;
        user.type = accountType;

        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), user);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = JsonUtils.getJsonResponse(response, type);
                User user = jsonResponse.data;


//                Gson gson = new Gson();
//                DreamTVApp.Logger.d(response);
//                User user = gson.fromJson(response, User.class);

                ((DreamTVApp) getActivity().getApplication()).setUser(user);


                getTasks(); //for the mainscreen, only the first page
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
                setFootersOptions(); //the settings section is displayed anyway

            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
                setFootersOptions(); //the settings section is displayed anyway

            }
        };

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USERS, null, jsonRequest, responseListener, this);

    }


    private void pickUserAccount() {
        /*This will list all available accounts on device without any filtering*/
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }


    private void setupVideosList() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }

    private void getTasks() {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", "1");

        //testing mode
        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();
        if (mode == null || mode.equals(getString(R.string.text_no_option)))
            urlParams.put("type", Constants.TASKS_ALL);
         else if (mode.equals(getString(R.string.text_yes_option)))
            urlParams.put("type", Constants.TASKS_TEST);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                if (taskList.data.size() > 0)
                    loadVideos(taskList, Constants.CHECK_NEW_TASKS_CATEGORY);

                getUserFinishedTasks("1");
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
//                setFootersOptions(); //the settings section is displayed anyway
            }
        };


        ConnectionManager.get(getActivity(), ConnectionManager.Urls.TASKS, urlParams, responseListener, this);

    }


    private void getUserFinishedTasks(String pagina) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", pagina);
        urlParams.put("type", Constants.TASKS_FINISHED);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                if (taskList.data.size() > 0)
                    loadVideos(taskList, Constants.CONTINUE_WATCHING_CATEGORY);

                getUserVideosList("1");

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
//                setFootersOptions(); //the settings section is displayed anyway
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.TASKS, urlParams, responseListener, this);

    }

    private void getUserVideosList(String pagina) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", pagina);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                if (taskList.data.size() > 0)
                    loadVideos(taskList, Constants.MY_LIST_CATEGORY);

                setFootersOptions();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
                setFootersOptions(); //the settings section is displayed anyway
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
                setFootersOptions(); //the settings section is displayed anyway
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_VIDEOS, urlParams, responseListener, this);

    }

    private void loadVideos(TaskList taskList, int taskState) {

        if (taskList != null) {
            VideoCardPresenter videoCardPresenter = new VideoCardPresenter();

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(videoCardPresenter);

            int state;
            HeaderItem header;
            if (taskState == Constants.MY_LIST_CATEGORY) {
                header = new HeaderItem(getString(R.string.title_my_list_category));
                state = Constants.MY_LIST_CATEGORY;
            } else if (taskState == Constants.CONTINUE_WATCHING_CATEGORY) {
                header = new HeaderItem(getString(R.string.title_continue_watching_category));
                state = Constants.CONTINUE_WATCHING_CATEGORY;
            } else {
                header = new HeaderItem(getString(R.string.title_check_new_tasks_category));
                state = Constants.CHECK_NEW_TASKS_CATEGORY;
            }

            for (Task task : taskList.data) {
                listRowAdapter.add(task.getVideo(state));
            }

            if (state == Constants.CHECK_NEW_TASKS_CATEGORY) { //Only in the Check_New_Tasks we add the SeeAll options. In others categories is not necessary
                Video lastVideoSeeMore = new Video();
                lastVideoSeeMore.title = getString(R.string.title_see_more_videos_category);
                lastVideoSeeMore.description = "";
                lastVideoSeeMore.thumbnail = "https://image.flaticon.com/icons/png/128/181/181532.png";
                listRowAdapter.add(lastVideoSeeMore);
            }

            mRowsAdapter.add(new ListRow(header, listRowAdapter));

            setAdapter(mRowsAdapter);
        }
    }


    private void setFootersOptions() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));
        //HeaderItem gridHeader2 = new HeaderItem(getString(R.string.title_contributions_category));

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        ArrayObjectAdapter gridRowAdapter2 = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.title_video_settings));
        //gridRowAdapter2.add(getResources().getString(R.string.title_user_statistics));
        //mRowsAdapter.add(new ListRow(gridHeader2, gridRowAdapter2));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(
                R.drawable.logo_tv));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_HIDDEN);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.black_opaque));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement Search()", Toast.LENGTH_SHORT).show();

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }


    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), Constants.BACKGROUND_UPDATE_DELAY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PREFERENCES_SETTINGS_RESULT_CODE || requestCode == VIDEO_DETAILS_RESULT_CODE) { //After PreferencesSettings or after add videos to userlist (in videoDetailsActivity)
                //Clear the screen
                setSelectedPosition(0);
                setupVideosList();
                //Load new video listSystem.out.println
                getTasks();
            } else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                // Receiving a result from the AccountPicker
                createUserAccount(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME),
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
//                    DreamTVApp.Logger.d(data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
//                DreamTVApp.Logger.d(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

            }
        } /*else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), "Pick an account", Toast.LENGTH_LONG).show();
        }*/

    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                final ListRow listRow = (ListRow) row;
                final ArrayObjectAdapter currentRowAdapter = (ArrayObjectAdapter) listRow.getAdapter();
                int selectedIndex = currentRowAdapter.indexOf(item);

                if (selectedIndex != -1 && (currentRowAdapter.size() - 1) == selectedIndex && listRow.getHeaderItem().getName().equals(getString(R.string.title_check_new_tasks_category))) { //Es el ultimo elemento de la fila (SEE ALL option)
                    Intent intent = new Intent(getActivity(), SeeAllActivity.class);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Objects.requireNonNull(getActivity()),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            Constants.SHARED_ELEMENT_NAME).toBundle();
                    getActivity().startActivity(intent, bundle);
                } else {
                    Video video = (Video) item;
                    DreamTVApp.Logger.d("Item: " + item.toString());
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(Constants.VIDEO, video);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Objects.requireNonNull(getActivity()),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            Constants.SHARED_ELEMENT_NAME).toBundle();
                    startActivityForResult(intent, VIDEO_DETAILS_RESULT_CODE, bundle);

                }
            } else if (item instanceof String) {

                if (((String) item).contains(Objects.requireNonNull(getActivity()).getApplicationContext().getString(R.string.title_video_settings))) {
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                    startActivityForResult(intent, PREFERENCES_SETTINGS_RESULT_CODE);
                } else if (((String) item).contains(getActivity().getApplicationContext().getString(R.string.title_contributions_category))) {
                    Toast.makeText(getActivity(), "Go to contributions", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), "Some item", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = ((Video) item);
                if (video.thumbnail != null)
                    try {
                        mBackgroundURI = new URI(video.thumbnail);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                startBackgroundTimer();
            }

        }
    }

    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI.toString());
                    }
                }
            });

        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(Constants.GRID_ITEM_WIDTH, Constants.GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }
}
