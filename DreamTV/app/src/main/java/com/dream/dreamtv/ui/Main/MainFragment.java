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

import android.accounts.AccountManager;
import android.app.Activity;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserData;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.presenter.GridItemPresenter;
import com.dream.dreamtv.presenter.ImageCardViewCustom;
import com.dream.dreamtv.presenter.VideoCardPresenter;
import com.dream.dreamtv.ui.Settings.SettingsActivity;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;

import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.HeaderItem;
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
    private LoadingDialog loadingDialog;
    private Observer<Resource<TaskEntity[]>> allTaskObserver;
    private Observer<Resource<TaskEntity[]>> continueTasksObserver;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

//        // Get the ViewModel from the factory
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        prepareBackgroundManager();

        setupUIElements();
        setupVideosList();

        setupEventListeners();

        instantiateLoading();

        userRegistration();

        observeResponseFromUserUpdate();

//        populateScreen();
        observeFromSyncData();

        observeResponseFromAllTasks();

        observeResponseFromContinueTasks();
        observeResponseFromTestTasks();

        observeResponseFromMyListTasks();
        observeResponseFromFinishedTasks();

    }


//    private void tempData() {
//        Log.d(TAG, "TempData");
//        TaskEntity[] taskEntities = new TaskEntity[2];
//        taskEntities[0] = new TaskEntity(1, "en", "Review",
//                "", "", "", new Video("video_id",
//                "en", "title 0", "description", 1000,
//                SEE_MORE_VIDEOS_ICON_URL, "team", "project", SEE_MORE_VIDEOS_ICON_URL),
//                Constants.TASKS_ALL);
//        taskEntities[1] = new TaskEntity(2, "en", "Review",
//                "", "", "", new Video("video_id",
//                "en", "title 1", "description", 1000,
//                SEE_MORE_VIDEOS_ICON_URL, "team", "project", SEE_MORE_VIDEOS_ICON_URL),
//                Constants.TASKS_ALL);
//
//        loadVideos(taskEntities, Constants.CHECK_NEW_TASKS_CATEGORY);
//
//        tempData2();
//    }
//
//    private void tempData2() {
//        Log.d(TAG, "TempData2");
//        TaskEntity[] taskEntities = new TaskEntity[2];
//        taskEntities[0] = new TaskEntity(3, "en", "Review",
//                "", "", "", new Video("video_id",
//                "en", "title 2", "description", 1000,
//                SEE_MORE_VIDEOS_ICON_URL, "team", "project", SEE_MORE_VIDEOS_ICON_URL),
//                Constants.TASKS_ALL);
//        taskEntities[1] = new TaskEntity(4, "en", "Review",
//                "", "", "", new Video("video_id",
//                "en", "title 3", "description", 1000,
//                SEE_MORE_VIDEOS_ICON_URL, "team", "project", SEE_MORE_VIDEOS_ICON_URL),
//                Constants.TASKS_ALL);
//
//        loadVideos(taskEntities, Constants.CHECK_NEW_TASKS_CATEGORY);
//
//    }


    private void requestLogin(String email) {
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>REQUEST LOGIN");
        showLoading();
        mViewModel.requestFromLogin(email, "com.google"); //TODO change password
    }


//    private void requestSyncData() {
//        Log.d(TAG, "requestSyncData()");
//
//        showLoading();
//        mViewModel.requestSyncData();
//    }


    private void populateScreen() {
        LiveData<TaskEntity[]> taskEntities = mViewModel.requestAllTasks();
        taskEntities.observe(this, new Observer<TaskEntity[]>() {
            @Override
            public void onChanged(TaskEntity[] taskEntities) {
                if (taskEntities.length > 0)
                    loadVideos(taskEntities, Constants.TASKS_ALL);
            }
        });

        LiveData<TaskEntity[]> taskContinueEntities = mViewModel.requestContinueTasks();
        taskContinueEntities.observe(this, new Observer<TaskEntity[]>() {
            @Override
            public void onChanged(TaskEntity[] taskEntities) {
                if (taskEntities.length > 0)
                    loadVideos(taskEntities, Constants.TASKS_CONTINUE);

            }
        });

    }

    //********************************************
    // Loading and progress bar related functions
    //********************************************
    private void instantiateLoading() {
        loadingDialog = new LoadingDialog(getActivity(), getString(R.string.title_loading_retrieve_tasks));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }


    private void showLoading() {
        loadingDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        mViewModel.responseFromUserUpdate().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromContinueTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromMyListTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFinishedTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromTestTasks().removeObservers(getViewLifecycleOwner());


    }

    private void observeResponseFromUserUpdate() {
        mViewModel.responseFromUserUpdate().removeObservers(getViewLifecycleOwner());

        mViewModel.responseFromUserUpdate().observe(getViewLifecycleOwner(), new Observer<Resource<User>>() {
            @Override
            public void onChanged(@Nullable Resource<User> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from userUpdate");
                        if (response.data != null) {
                            Log.d(TAG, response.data.toString());
                            setupVideosList();
                            updateScreenLanguage(response.data);
                        }
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }

                }

                dismissLoading();
            }
        });
    }


    private void observeResponseFromAllTasks() {
        mViewModel.responseFromTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromTasks().observe(getViewLifecycleOwner(), new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> response) {

                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from all tasks");
                        if (response.data != null && response.data.length > 0)
                            loadVideos(response.data, Constants.TASKS_ALL);
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


    private void observeResponseFromContinueTasks() {
        mViewModel.responseFromContinueTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromContinueTasks().observe(getViewLifecycleOwner(), new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from continue tasks");
                        if (response.data != null && response.data.length > 0)
                            loadVideos(response.data, Constants.TASKS_CONTINUE);
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }

                dismissLoading();
            }
        });


    }

    private void observeResponseFromTestTasks() {
        mViewModel.responseFromTestTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromTestTasks().observe(getViewLifecycleOwner(), new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from test tasks");

                        if (response.data != null && response.data.length > 0)
                            loadVideos(response.data, Constants.TASKS_TEST);
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }

                dismissLoading();
            }
        });


    }

    private void observeResponseFromMyListTasks() {
        mViewModel.responseFromMyListTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromMyListTasks().observe(getViewLifecycleOwner(), new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from my list tasks");
                        if (response.data != null && response.data.length > 0)
                            loadVideos(response.data, Constants.TASKS_MY_LIST);
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }

                dismissLoading();
            }
        });


    }

    private void observeResponseFromFinishedTasks() {
        mViewModel.responseFromFinishedTasks().removeObservers(getViewLifecycleOwner());
        mViewModel.responseFromFinishedTasks().observe(getViewLifecycleOwner(), new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        Log.d(TAG, "Response from finished tasks");
                        if (response.data != null && response.data.length > 0)
                            loadVideos(response.data, Constants.TASKS_FINISHED);
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }

                dismissLoading();
            }
        });


    }

    private void observeFromSyncData() {
        LiveData<Resource<String>> responseFromSyncData = mViewModel.responseFromSyncData();
        responseFromSyncData.observe(this, new Observer<Resource<String>>() {
            @Override
            public void onChanged(Resource<String> response) {
                if (response != null) {
                    if (response.status.equals(Resource.Status.SUCCESS)) {
                        if (response.data != null && response.data.equals("Completed")) {
                            Log.d(TAG, response.data);
//                            populateScreen();
                            setFootersOptions();
                        }
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }

                }

                dismissLoading();
            }
        });
    }


    private void updateScreenLanguage(User user) {
        if (!LocaleHelper.getLanguage(getActivity()).equals(user.interface_language)) {
            LocaleHelper.setLocale(getActivity(), user.interface_language);
            Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
            Log.d(TAG, "Different language. Updating screen.");
        }
    }


    private void userRegistration() {

        Log.d(TAG, "userRegistration()");
        String token = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getToken();
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (token == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            requestLogin(user.email);

    }


    private void pickUserAccount() {
        Log.d(TAG, "pickUserAccount()");

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
        Log.d(TAG, "New mRowsAdapter()");
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }


    private void loadVideos(TaskEntity[] tasks, String category) {

        if (tasks != null) {
            Log.d(TAG, "Loading videos");
            VideoCardPresenter videoCardPresenter = new VideoCardPresenter();

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(videoCardPresenter);

//            val diffCallback = object : DiffCallback<DummyItem>() {
//                override fun areItemsTheSame(oldItem: DummyItem,
//                        newItem: DummyItem): Boolean =
//                        oldItem.id == newItem.id
//                override fun areContentsTheSame(oldItem: DummyItem,
//                        newItem: DummyItem): Boolean =
//                        oldItem == newItem
//            }

            DiffCallback diffCallback = new DiffCallback<TaskEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
                    return oldItem.task_id == newItem.task_id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
                    return oldItem == newItem;
                }
            };


            HeaderItem header;
            switch (category) {
                case Constants.TASKS_MY_LIST:
                    header = new HeaderItem(getString(R.string.title_my_list_category));
                    break;
                case Constants.TASKS_FINISHED:
                    header = new HeaderItem(getString(R.string.title_finished_category));
                    break;
                case Constants.TASKS_CONTINUE:
                    header = new HeaderItem(getString(R.string.title_continue_watching_category));
                    break;
                case Constants.TASKS_ALL:
                    header = new HeaderItem(getString(R.string.title_check_new_tasks_category));
                    break;
                case Constants.TASKS_TEST:
                    header = new HeaderItem(getString(R.string.title_test_category));
                    break;
                default:
                    header = new HeaderItem(getString(R.string.title_check_new_tasks_category));
                    break;
            }


//            if (category == Constants.CHECK_NEW_TASKS_CATEGORY) { //Only in the Check_New_Tasks we add the SeeAll options. In others categories is not necessary
//                Video lastVideoSeeMore = new Video();
//                lastVideoSeeMore.title = getString(R.string.title_see_more_videos_category);
//                lastVideoSeeMore.description = "";
//                lastVideoSeeMore.thumbnail = SEE_MORE_VIDEOS_ICON_URL;
//                listRowAdapter.add(lastVideoSeeMore);
//            }
            //If we found a task category already exists, instead of adding a new row,
            // we found the correspondent category and add the tasks
            boolean foundHeaderRow = false;
            for (int i = 0; i < mRowsAdapter.size(); i++) {
                ListRow listRow = ((ListRow) mRowsAdapter.get(i));
                if (!foundHeaderRow && listRow.getHeaderItem().getName().equals(header.getName())) {
                    foundHeaderRow = true;
                    ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) listRow.getAdapter());
//                    List<Video> videoEntities = new ArrayList<>();
//                    for (int j = 0; j < tasks.length; j++) {
//                        videoEntities.add(tasks[j]);
//                    }
                    arrayObjectAdapter.setItems(Arrays.asList(tasks), diffCallback);
                }
            }

            //If we have not found a certain category, we add a new one
            if (!foundHeaderRow) {
                for (TaskEntity task : tasks) {
                    listRowAdapter.add(task);
                }

                mRowsAdapter.add(0, new ListRow(header, listRowAdapter));
            }

            setAdapter(mRowsAdapter);
        }
    }


//    private void setFootersOptions() {
//        if ((mRowsAdapter.size() - 1) >= 0) { //If the footer category exists, we don't create a new one
//
//            ListRow listRow = (ListRow) mRowsAdapter.get(mRowsAdapter.size() - 1);
//
//            if (!listRow.getHeaderItem().getName().equals(getString(R.string.title_preferences_category))) {
//                HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));
//
//                GridItemPresenter mGridPresenter = new GridItemPresenter();
//                ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
//                gridRowAdapter.add(Constants.SETTINGS);
//                mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
//
//                setAdapter(mRowsAdapter);
//            }
//        }
//    }

    private void setFootersOptions() {
        boolean foundHeader = false;
        for (int i = 0; i < mRowsAdapter.size(); i++) {
            ListRow listRow = (ListRow) mRowsAdapter.get(i);
            if (listRow.getHeaderItem().getName().equals(getString(R.string.title_preferences_category))) {
                foundHeader = true;
                break;
            }
        }

        if (!foundHeader) { //To avoid duplicate category
            HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));

            GridItemPresenter mGridPresenter = new GridItemPresenter();
            ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
            gridRowAdapter.add(Constants.SETTINGS);
            mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

            setAdapter(mRowsAdapter);
        }
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.logo_tv, null));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                Log.d(TAG, "onActivityResult() - Result from pickAccount()");

                // Receiving a result from the AccountPicker
                requestLogin(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

            }
        }

    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof TaskEntity) {
                TaskEntity taskEntity = (TaskEntity) item;

                UserData userData = new UserData();
                userData.mSelectedTask = taskEntity;
                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(Constants.USER_DATA, userData);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Objects.requireNonNull(getActivity()),
                        ((ImageCardViewCustom) itemViewHolder.view).getMainImageView(),
                        Constants.SHARED_ELEMENT_NAME).toBundle();
                startActivity(intent, bundle);

            } else if (item instanceof String) {
                String value = String.valueOf(item);
                if (value.equals(Constants.SETTINGS)) {
                    Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                } else
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof TaskEntity) {
                Video video = ((TaskEntity) item).video;
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
