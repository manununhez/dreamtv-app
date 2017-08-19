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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;


public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1456;
    private static final int PREFERENCES_SETTINGS_RESULT_CODE = 1256;
    public static final int MY_LIST_CATEGORY = 1250;
    public final String MY_LIST_CATEGORY_TITLE = "My List";
    public static final int SEE_AGAIN_CATEGORY = 1251;
    public final String SEE_AGAIN_CATEGORY_TITLE = "Continue watching";
    public static final int CHECK_NEW_TASKS_CATEGORY = 1252;
    public final String CHECK_NEW_TASKS_CATEGORY_TITLE = "Check out these videos";

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;

    //    private String[] projects = {"tedtalks", "tedxtalks", "ted-ed", "otp-resources", "best-of-tedxtalks"};
//    private String[] projects = {"tedtalks", "tedxtalks", "ted-ed", "otp-resources"};


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();

        setupUIElements();
        setupVideosList();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.GET_ACCOUNTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

                // MY_PERMISSIONS_REQUEST_GET_ACCOUNTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
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

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving user data...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                User user = gson.fromJson(response, User.class);

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

//            int describeContents = account.describeContents();
//            int hashCode = account.hashCode();
//
//            accountsInfo.add("name = " + name +
//                    "\ntype = " + type +
//                    "\ndescribeContents = " + describeContents +
//                    "\nhashCode = " + hashCode);
//        }
//        String[] result = new String[accountsInfo.size()];
//        accountsInfo.toArray(result);
//
//        DreamTVApp.Logger.d("Accounts = "+accountsInfo.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

//    private void getTedProjects() {
//        getVideos(projects[0], 0);
//
////        setFootersOptions();
//    }

    private void setupVideosList() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }

    private void getUserTasks(String pagina) {
//        SharedPreferenceUtils.save(getActivity(), getString(R.string.dreamTVApp_token), "$2y$10$RCahpKrpkDxcqQvTo4IRju2VXiXoL3be4jJRuHRdc0SbGc4mdvqia");

//        Task task = new Task();
//        task.type_task = new String[]{"Review", "Approve"};
//        task.team = "ted";
//        task.limit = 5;
//        task.offset = 0;

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", pagina);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving users tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                loadVideos(taskList, CHECK_NEW_TASKS_CATEGORY);
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

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS, urlParams, responseListener, this);

    }


    private void getUserFinishedTasks(String pagina) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", pagina);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving users tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                loadVideos(taskList, SEE_AGAIN_CATEGORY);

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

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving users tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                loadVideos(taskList, MY_LIST_CATEGORY);

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
            if (taskState == MY_LIST_CATEGORY) {
                header = new HeaderItem(MY_LIST_CATEGORY_TITLE);
                state = MY_LIST_CATEGORY;
            } else if (taskState == SEE_AGAIN_CATEGORY) {
                header = new HeaderItem(SEE_AGAIN_CATEGORY_TITLE);
                state = SEE_AGAIN_CATEGORY;
            } else {
                header = new HeaderItem(CHECK_NEW_TASKS_CATEGORY_TITLE);
                state = CHECK_NEW_TASKS_CATEGORY;
            }

            for (Task task : taskList.data) {
                listRowAdapter.add(task.getVideo(state));
            }

            if (state == CHECK_NEW_TASKS_CATEGORY) { //Only in the Check_New_Tasks we add the SeeAll options. In others categories is not necessary
                Video lastVideoSeeMore = new Video();
                lastVideoSeeMore.title = "See more videos";
                lastVideoSeeMore.description = "";
                lastVideoSeeMore.thumbnail = "https://upload.wikimedia.org/wikipedia/commons/5/59/R2244.png";
                listRowAdapter.add(lastVideoSeeMore);
            }


            mRowsAdapter.add(new ListRow(header, listRowAdapter));

            setAdapter(mRowsAdapter);


        }

    }

//    private void loadVideos(VideoList videoList) {
//
//        VideoCardPresenter videoCardPresenter = new VideoCardPresenter();
//
//        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(videoCardPresenter);
//        for (Video video : videoList.objects) {
//            listRowAdapter.add(video);
//        }
//
//        HeaderItem header = new HeaderItem("Videos para ver");
//        mRowsAdapter.add(new ListRow(header, listRowAdapter));
//
//        setAdapter(mRowsAdapter);
//
////        if (index < projects.length - 1)
////            getVideos(projects[index + 1], index + 1);
//
//
//        setFootersOptions();
//    }

    private void setFootersOptions() {
        HeaderItem gridHeader = new HeaderItem("Preferences");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
//        gridRowAdapter.add(getResources().getString(R.string.grid_view));
//        gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
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
                R.drawable.logo_dream_tv));
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


//    private void getVideos() {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("team", "ted");
//        urlParams.put("project", "tedxtalks");
//        urlParams.put("order_by", "-created");
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving videos...") {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                DreamTVApp.Logger.d(response);
//                VideoList videoList = gson.fromJson(response, VideoList.class);
//                DreamTVApp.Logger.d(videoList.toString());
//
//                loadVideos(videoList);
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
//        ConnectionManager.get(getActivity(), ConnectionManager.Urls.VIDEOS, urlParams, responseListener, this);
//
//    }

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
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                final ListRow listRow = (ListRow) row;
                final ArrayObjectAdapter currentRowAdapter = (ArrayObjectAdapter) listRow.getAdapter();
                int selectedIndex = currentRowAdapter.indexOf(item);

                if (selectedIndex != -1 && (currentRowAdapter.size() - 1) == selectedIndex && listRow.getHeaderItem().getName().equals(CHECK_NEW_TASKS_CATEGORY_TITLE)) { //Es el ultimo elemento de la fila (SEE ALL option)
                    Intent intent = new Intent(getActivity(), SeeAllActivity.class);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                    getActivity().startActivity(intent, bundle);
                } else {
                    Video video = (Video) item;
                    DreamTVApp.Logger.d("Item: " + item.toString());
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(VideoDetailsActivity.VIDEO, video);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                    getActivity().startActivity(intent, bundle);
                }
            } else if (item instanceof String) {
//                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
//                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
//                    startActivity(intent);
                if (((String) item).contains(getString(R.string.personal_settings))) {
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                    startActivityForResult(intent, PREFERENCES_SETTINGS_RESULT_CODE);
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
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
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
            if (requestCode == PREFERENCES_SETTINGS_RESULT_CODE) { //After PreferencesSettings
                //Clear the screen
                setSelectedPosition(0);
                setupVideosList();
                //Load new video list
                getUserTasks("1");
            }
        }
    }
}
