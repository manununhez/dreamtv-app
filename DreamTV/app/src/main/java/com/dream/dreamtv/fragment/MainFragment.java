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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.dream.dreamtv.activity.VideoDetailsActivity;
import com.dream.dreamtv.activity.SeeAllActivity;
import com.dream.dreamtv.activity.PreferencesActivity;
import com.dream.dreamtv.adapter.VideoCardPresenter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.TaskList;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.beans.Task;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.PermissionUtil;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    private Handler mHandler = new Handler();

    private String URL_THUMBNAIL_ICON = "https://image.flaticon.com/icons/png/128/181/181532.png";
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1456;
    public static final int PREFERENCES_SETTINGS_RESULT_CODE = 1256;
    public static final int VIDEO_DETAILS_RESULT_CODE = 1257;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();
        setupVideosList();

        // Here, thisActivity is the current activity
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.GET_ACCOUNTS)
//                != PackageManager.PERMISSION_GRANTED) {

        if (PermissionUtil.shouldAskPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)) {
/*
            * If permission denied previously
            * */
            if (shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                //listener.onPermissionPreviouslyDenied();
                //show a dialog explaining permission and then request permission
                Toast.makeText(getActivity(), "Get accounts permission disabled and app will not work. Please proceed and give permission to access to accounts", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS
                );
            } else {
                /*
                * Permission denied or first time requested
                * */
                if (SharedPreferenceUtils.isFirstTimeAskingPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)) {
                    SharedPreferenceUtils.firstTimeAskingPermission(getActivity(), Manifest.permission.GET_ACCOUNTS, false);
                    //listener.onNeedPermission();
                    requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                            MY_PERMISSIONS_REQUEST_GET_ACCOUNTS
                    );
                } else {
                    /*
                    * Handle the feature without permission or ask user to manually allow permission
                    * */
                    //listener.onPermissionDisabled();
                    Toast.makeText(getActivity(), "Permission Disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //listener.onPermissionGranted();
            userRegistration();
        }


//        getVideos();
        setupEventListeners();


    }

    public void userRegistration() {

        Account primaryAccount = getAccountManager();

        User user = new User();
        user.name = primaryAccount.name;
        user.type = primaryAccount.type;

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


                getUserTasks("1"); //for the mainscreen, only the first page
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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_CREATE, null, jsonRequest, responseListener, this);

    }

    public Account getAccountManager() {
//        ArrayList<String> accountsInfo = new ArrayList<String>();
        AccountManager manager = AccountManager.get(getActivity());
        Account[] accounts = manager.getAccounts();

        for (Account account : accounts) {
            String name = account.name;
            String type = account.type;

            if (type.equals("com.google"))
                return account;
        }

        return accounts[0]; //we assume that account[0] is the primary users account

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

    private void getUserTasks(String pagina) {
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


        //testing mode
        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();
        if (mode == null || mode.equals("N"))
            ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS, urlParams, responseListener, this);
        else if (mode.equals("Y"))
            ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS_TESTS, urlParams, responseListener, this);

    }


    private void getUserFinishedTasks(String pagina) {
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

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS_FINISHED, urlParams, responseListener, this);

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
                lastVideoSeeMore.thumbnail = URL_THUMBNAIL_ICON;
                listRowAdapter.add(lastVideoSeeMore);
            }

            mRowsAdapter.add(new ListRow(header, listRowAdapter));

            setAdapter(mRowsAdapter);
        }
    }


    private void setFootersOptions() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));
        HeaderItem gridHeader2 = new HeaderItem(getString(R.string.title_contributions_category));

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        ArrayObjectAdapter gridRowAdapter2 = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.title_video_settings));
        gridRowAdapter2.add(getResources().getString(R.string.title_user_statistics));
        mRowsAdapter.add(new ListRow(gridHeader2, gridRowAdapter2));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
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


    protected void updateBackground(String uri) {
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
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            Constants.SHARED_ELEMENT_NAME).toBundle();
                    getActivity().startActivity(intent, bundle);
                } else {
                    Video video = (Video) item;
                    DreamTVApp.Logger.d("Item: " + item.toString());
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(Constants.VIDEO, video);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            Constants.SHARED_ELEMENT_NAME).toBundle();
                    startActivityForResult(intent, VIDEO_DETAILS_RESULT_CODE, bundle);

                }
            } else if (item instanceof String) {

                if (((String) item).contains(getActivity().getApplicationContext().getString(R.string.title_video_settings))) {
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    userRegistration();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PREFERENCES_SETTINGS_RESULT_CODE || requestCode == VIDEO_DETAILS_RESULT_CODE) { //After PreferencesSettings or after add videos to userlist (in videoDetailsActivity)
                //Clear the screen
                setSelectedPosition(0);
                setupVideosList();
                //Load new video list
                getUserTasks("1");
            }
        }
    }
}
