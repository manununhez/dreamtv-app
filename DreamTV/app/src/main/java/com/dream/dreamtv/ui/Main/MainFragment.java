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

package com.dream.dreamtv.ui.Main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.UserEntity;
import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TaskResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserData;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.presenter.GridItemPresenter;
import com.dream.dreamtv.presenter.VideoCardPresenter;
import com.dream.dreamtv.ui.SeeAllActivity;
import com.dream.dreamtv.ui.SettingsActivity;
import com.dream.dreamtv.ui.VideoDetailsActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import static com.dream.dreamtv.utils.JsonUtils.getJsonRequest;
import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;


public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_PAGE = "page";
    private static final String SEE_MORE_VIDEOS_ICON_URL = "https://image.flaticon.com/icons/png/128/181/181532.png";
    private static final String FIRST_PAGE = "1";
    private static final String EMPTY_ITEM = "Some item";
    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private static final int PREFERENCES_SETTINGS_RESULT_CODE = 1256;
    private static final int VIDEO_DETAILS_RESULT_CODE = 1257;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private Uri mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    private MainViewModel mViewModel;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        // Get the ViewModel from the factory
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(Objects.requireNonNull(getContext()).getApplicationContext());
        mViewModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        prepareBackgroundManager();

        setupUIElements();
        setupVideosList();

        setupEventListeners();


        //userRegistration();

        mViewModel.requestFromLogin("03.manu@gmail.com", "123456");
        observeResponseFromLogin();


    }

    private void observeResponseFromLogin() {
        LiveData<Resource<UserEntity>> responseFromLogin = mViewModel.responseFromLogin();
        responseFromLogin.observe(this, new Observer<Resource<UserEntity>>() {
            @Override
            public void onChanged(@Nullable Resource<UserEntity> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "AddCityFragment = " + Objects.requireNonNull(response).toString());

                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }

                }
            }
        });
    }

//    private void userRegistration() {
//
//        String token = ((DreamTVApp) getActivity().getApplication()).getToken();
//        if (token == null) //first time the app is initiated. The user has to select an account
//            pickUserAccount();
//        else //the user has already has an account. Proceed to get the videos
//            getTasks(); //for the mainscreen, only the first page
//
//    }


//    private void getUser() {
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
//                };
//                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);
//                User user = jsonResponse.data;
//
//                ((DreamTVApp) getActivity().getApplication()).setUser(user);
//
//                //To update the screen with the selected interface language
//                if (!LocaleHelper.getLanguage(getActivity()).equals(user.interface_language)) {
//                    LocaleHelper.setLocale(getActivity(), user.interface_language);
//                    getActivity().recreate(); //Recreate activity
//                    Log.d(TAG, "Different language. Updating screen.");
//                } else {
//                    Log.d(TAG, "Same language. Not updating screen.");
//                    getTasks();
//
//                }
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
//
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER, null, responseListener, this);
//    }
//
//
//    private void login(final String accountName, final String accountType) {
//        User user = new User();
//        user.email = accountName;
//        user.password = accountType;
//
//        final String jsonRequest = getJsonRequest(getActivity(), user);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
//                };
//                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);
//                User user = jsonResponse.data;
//
//
//                ((DreamTVApp) getActivity().getApplication()).setToken(user.token);
//
//                getUser();
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
//
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//
//                //The user does not exist, needs to register
//                userRegister(accountName, accountType);
//
//            }
//        };
//
//        NetworkDataSource.post(getActivity(), NetworkDataSource.Urls.LOGIN, null, jsonRequest, responseListener, this);
//
//    }
//
//    private void userRegister(final String accountName, final String accountType) {
//        User user = new User();
//        user.email = accountName;
//        user.password = accountType;
//
//        final String jsonRequest = getJsonRequest(getActivity(), user);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
//                };
//                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);
//                User user = jsonResponse.data;
//
//
//                ((DreamTVApp) getActivity().getApplication()).setToken(user.token);
//
//                getUser();
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
//
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//                setFootersOptions(); //the settings section is displayed anyway
//
//            }
//        };
//
//        NetworkDataSource.post(getActivity(), NetworkDataSource.Urls.REGISTER, null, jsonRequest, responseListener, this);
//
//    }

    private void pickUserAccount() {
        /*This will list all available accounts on device without any filtering*/

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);


//        Intent intent = AccountManager.newChooseAccountIntent( null,
//                null, null, null, null, null, null);

//        Account[] accounts = AccountManager.get(getActivity()).getAccounts();

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

//    private void getTasks() {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, FIRST_PAGE);
//
//        //testing mode
//        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();
//        if (mode == null || mode.equals(getString(R.string.text_no_option)))
//            urlParams.put(PARAM_TYPE, Constants.TASKS_ALL);
//        else if (mode.equals(getString(R.string.text_yes_option)))
//            urlParams.put(PARAM_TYPE, Constants.TASKS_TEST);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.CHECK_NEW_TASKS_CATEGORY);
//
//                getUserToContinueTasks(FIRST_PAGE);
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
//
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.TASKS, urlParams, responseListener, this);
//
//    }
//

//    private void getUserToContinueTasks(String pagina) {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, pagina);
//        urlParams.put(PARAM_TYPE, Constants.TASKS_CONTINUE);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.CONTINUE_WATCHING_CATEGORY);
//
//                getUserVideosList(FIRST_PAGE);
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
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.TASKS, urlParams, responseListener, this);
//
//    }

//    private void getUserVideosList(String pagina) {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, pagina);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.MY_LIST_CATEGORY);
//
//                setFootersOptions();
//
//
//                getReasons();
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//                setFootersOptions(); //the settings section is displayed anyway
//            }
//        };
//
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_VIDEOS, urlParams, responseListener, this);
//
//    }

//    private void getReasons() {
//        ResponseListener responseListener = new ResponseListener(getActivity(), false, true, getString(R.string.title_loading_retrieve_options)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<List<ErrorReason>>>() {
//                };
//                JsonResponseBaseBean<List<ErrorReason>> jsonResponse = getJsonResponse(response, type);
//
//                ((DreamTVApp) getActivity().getApplication()).setReasons(jsonResponse.data);
//
//                Log.d(TAG, jsonResponse.data.toString());
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
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.REASONS, null, responseListener, this);
//
//    }

    private void loadVideos(TaskResponse taskResponse, int taskState) {

        if (taskResponse != null) {
            VideoCardPresenter videoCardPresenter = new VideoCardPresenter();

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(videoCardPresenter);

            int category;
            HeaderItem header;
            if (taskState == Constants.MY_LIST_CATEGORY) {
                header = new HeaderItem(getString(R.string.title_my_list_category));
                category = Constants.MY_LIST_CATEGORY;
            } else if (taskState == Constants.CONTINUE_WATCHING_CATEGORY) {
                header = new HeaderItem(getString(R.string.title_continue_watching_category));
                category = Constants.CONTINUE_WATCHING_CATEGORY;
            } else {
                header = new HeaderItem(getString(R.string.title_check_new_tasks_category));
                category = Constants.CHECK_NEW_TASKS_CATEGORY;
            }

            for (Task task : taskResponse.data) {
                listRowAdapter.add(task.videos);
            }

            if (category == Constants.CHECK_NEW_TASKS_CATEGORY) { //Only in the Check_New_Tasks we add the SeeAll options. In others categories is not necessary
                Video lastVideoSeeMore = new Video();
                lastVideoSeeMore.title = getString(R.string.title_see_more_videos_category);
                lastVideoSeeMore.description = "";
                lastVideoSeeMore.thumbnail = SEE_MORE_VIDEOS_ICON_URL;
                listRowAdapter.add(lastVideoSeeMore);
            }

            mRowsAdapter.add(new ListRow(header, listRowAdapter));

            setAdapter(mRowsAdapter);
        }
    }


    private void setFootersOptions() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.title_video_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.logo_tv, null));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_HIDDEN);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.black_opaque));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), getString(R.string.title_search), Toast.LENGTH_SHORT).show();

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
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

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), Constants.BACKGROUND_UPDATE_DELAY);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == PREFERENCES_SETTINGS_RESULT_CODE) { //After PreferencesSettings reload the screen
//                Objects.requireNonNull(getActivity()).recreate();
//
//            } else if (requestCode == VIDEO_DETAILS_RESULT_CODE) { //After add videos to userlist (in videoDetailsActivity) reload the screen
//                //Clear the screen
//                setSelectedPosition(0);
//                setupVideosList();
//                //Load new video listSystem.out.println
//                getTasks();
//            } else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
//                // Receiving a result from the AccountPicker
//                login(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME), data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
//
//            }
//        }
//
//    }

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
                    String categoryName = listRow.getHeaderItem().getName();
                    int category;

                    if (categoryName.equals(getString(R.string.title_my_list_category))) {
                        category = Constants.MY_LIST_CATEGORY;
                    } else if (categoryName.equals(getString(R.string.title_continue_watching_category))) {
                        category = Constants.CONTINUE_WATCHING_CATEGORY;
                    } else {
                        category = Constants.CHECK_NEW_TASKS_CATEGORY;
                    }

                    UserData userData = new UserData();
                    userData.mSelectedVideo = video;
                    userData.category = category;
                    Log.d(TAG, "Item: " + item.toString());
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(Constants.USER_DATA, userData);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Objects.requireNonNull(getActivity()),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            Constants.SHARED_ELEMENT_NAME).toBundle();
                    startActivityForResult(intent, VIDEO_DETAILS_RESULT_CODE, bundle);

                }
            } else if (item instanceof String) {

                if (((String) item).contains(Objects.requireNonNull(getActivity()).getString(R.string.title_video_settings))) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivityForResult(intent, PREFERENCES_SETTINGS_RESULT_CODE);

                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = ((Video) item);
                if (video.thumbnail != null) {
                    mBackgroundURI = Uri.parse(video.thumbnail);
                    startBackgroundTimer();
                }
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

}
