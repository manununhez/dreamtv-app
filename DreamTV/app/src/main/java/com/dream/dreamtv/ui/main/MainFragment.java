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

package com.dream.dreamtv.ui.main;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.BrowseSupportFragment.BrowseTransitionListener;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.DiffCallback;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.dream.dreamtv.BuildConfig;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Card;
import com.dream.dreamtv.model.Category;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.presenter.SingleLineCardPresenter;
import com.dream.dreamtv.presenter.sideInfoPresenter.SideInfoCardPresenter;
import com.dream.dreamtv.ui.categories.CategoryActivity;
import com.dream.dreamtv.ui.preferences.AppPreferencesActivity;
import com.dream.dreamtv.ui.preferences.SubtitlePreferencesActivity;
import com.dream.dreamtv.ui.preferences.VideoPreferencesActivity;
import com.dream.dreamtv.ui.search.SearchActivity;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.EMPTY_ITEM;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUB_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TOPIC_NAME;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.TASKS_ALL_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_FINISHED_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;


public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private static final int REQUEST_APP_SETTINGS = 45690;
    private static final int REQUEST_VIDEO_SETTINGS = 45710;
    private static final int REQUEST_STT_SETTINGS = 45750;
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private ArrayObjectAdapter mRowsAdapter;
    private MainViewModel mViewModel;
    private LoadingDialog loadingDialog;
    private ListRow rowSettings;
    private ListRow rowAllTasks;
    private ListRow rowMyListTasks;
    private ListRow rowFinishedTasks;
    private ListRow rowContinueTasks;
    private ListRow rowTestTasks;
    private ListRow rowCategories;
    private LiveData<Resource<TasksList>> allTaskLiveData;
    private LiveData<Resource<TasksList>> continueTaskLiveData;
    private LiveData<Resource<TasksList>> finishedTaskLiveData;
    private LiveData<Resource<TasksList>> myListTaskLiveData;
    private LiveData<Resource<TasksList>> testTaskLiveData;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LiveData<Resource<User>> updateUserLiveData;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

//        // Get the ViewModel from the factory
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        setupUIElements();

        setupVideosList();

        setupEventListeners();

        instantiateLoading();

        initSettingsRow();

        userRegistration();

        populateScreen();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHeadersState(HEADERS_DISABLED);
    }

    private void initSettingsRow() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));

        SingleLineCardPresenter mIconCardPresenter = new SingleLineCardPresenter(getActivity());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mIconCardPresenter);


        Card appSettingsCard = new Card();
        appSettingsCard.setTitle(getString(R.string.pref_title_app_settings));
        appSettingsCard.setLocalImageResource("ic_settings_app");
        gridRowAdapter.add(appSettingsCard);

        Card videoSettingsCard = new Card();
        videoSettingsCard.setTitle(getString(R.string.pref_title_video_settings));
        videoSettingsCard.setLocalImageResource("ic_settings_video");
        gridRowAdapter.add(videoSettingsCard);

        Card subtitleSettingsCard = new Card();
        subtitleSettingsCard.setTitle(getString(R.string.pref_title_subtitle_settings));
        subtitleSettingsCard.setLocalImageResource("ic_settings_stt");
        gridRowAdapter.add(subtitleSettingsCard);

        rowSettings = new ListRow(gridHeader, gridRowAdapter);


        rowCategories = new ListRow(new HeaderItem(getString(R.string.title_topics_category)), new ArrayObjectAdapter(new SingleLineCardPresenter(getActivity())));


        rowAllTasks = new ListRow(new HeaderItem(getString(R.string.title_check_new_tasks_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowMyListTasks = new ListRow(new HeaderItem(getString(R.string.title_my_list_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowFinishedTasks = new ListRow(new HeaderItem(getString(R.string.title_finished_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowContinueTasks = new ListRow(new HeaderItem(getString(R.string.title_continue_watching_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowTestTasks = new ListRow(new HeaderItem(getString(R.string.title_test_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));

    }


    private void login(String email) {
        if (DEBUG) Log.d(TAG, ">>>>>>>>>>>>>>>>>>>REQUEST LOGIN");
//        showLoading();
        mViewModel.login(email, "com.google"); //TODO change password
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (DEBUG) Log.d(TAG, "onDestroyView");

        if (allTaskLiveData != null)
            allTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (continueTaskLiveData != null)
            continueTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (finishedTaskLiveData != null)
            finishedTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (myListTaskLiveData != null)
            myListTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (testTaskLiveData != null)
            testTaskLiveData.removeObservers(getViewLifecycleOwner());

        if (updateUserLiveData != null)
            updateUserLiveData.removeObservers(getViewLifecycleOwner());

    }

    private void populateScreen() {


        reorderRowSettings();


        LiveData<Resource<Category[]>> categoriesLiveData = mViewModel.fetchCategories();
        categoriesLiveData.removeObservers(getViewLifecycleOwner());
        categoriesLiveData.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status.equals(Resource.Status.LOADING)) {
                if (DEBUG) Log.d(TAG, "loading");

            } else if (resource.status.equals(Resource.Status.SUCCESS)) {
                if (resource.data != null)
                    categoriesRowSettings(resource.data);
                else verifyRowExistenceAndRemove(rowCategories);


                if (DEBUG) Log.d(TAG, "task response");
            } else if (resource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (resource.message != null) {
                    if (DEBUG) Log.d(TAG, resource.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

//                dismissLoading();
            }
        });

        allTaskLiveData = mViewModel.requestTasksByCategory(TASKS_ALL_CAT);
        allTaskLiveData.removeObservers(getViewLifecycleOwner());
        allTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {

            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();

            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                if (tasksListResource.data != null) {
                    if (tasksListResource.data.data != null && tasksListResource.data.data.length > 0)
                        loadVideos(tasksListResource.data);
                    else verifyRowExistenceAndRemove(rowAllTasks);
                }

                if (DEBUG) Log.d(TAG, "task response");

                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null) {
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

        });

        continueTaskLiveData = mViewModel.requestTasksByCategory(TASKS_CONTINUE_CAT);
        continueTaskLiveData.removeObservers(getViewLifecycleOwner());
        continueTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();
            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                if (tasksListResource.data != null) {
                    if (tasksListResource.data.data != null && tasksListResource.data.data.length > 0)
                        loadVideos(tasksListResource.data);
                    else verifyRowExistenceAndRemove(rowContinueTasks);
                }

                if (DEBUG) Log.d(TAG, "task response");

                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null) {
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

            // TODO throw new RuntimeException("Get list sorted of continued tasks");

        });

        finishedTaskLiveData = mViewModel.requestTasksByCategory(TASKS_FINISHED_CAT);
        finishedTaskLiveData.removeObservers(getViewLifecycleOwner());
        finishedTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();
            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                if (tasksListResource.data != null) {
                    if (tasksListResource.data.data != null && tasksListResource.data.data.length > 0)
                        loadVideos(tasksListResource.data);
                    else verifyRowExistenceAndRemove(rowFinishedTasks);
                }

                if (DEBUG) Log.d(TAG, "task response");
                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null) {
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

            // TODO throw new RuntimeException("Get list sorted of finished tasks");

        });

        myListTaskLiveData = mViewModel.requestTasksByCategory(TASKS_MY_LIST_CAT);
        myListTaskLiveData.removeObservers(getViewLifecycleOwner());
        myListTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();
            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                if (tasksListResource.data != null) {
                    if (tasksListResource.data.data != null && tasksListResource.data.data.length > 0) {
                        if (DEBUG)
                            Log.d(TAG, "task response My list:[" + tasksListResource.data.data.length + "]" + Arrays.toString(tasksListResource.data.data));

                        loadVideos(tasksListResource.data);
                    } else verifyRowExistenceAndRemove(rowMyListTasks);
                }

                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null) {
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

            //  TODO throw new RuntimeException("Get list sorted of my list tasks");

        });

        testTaskLiveData = mViewModel.requestTasksByCategory(TASKS_TEST_CAT);
        testTaskLiveData.removeObservers(getViewLifecycleOwner());
        testTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();
            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                if (tasksListResource.data != null) {
                    if (tasksListResource.data.data != null && tasksListResource.data.data.length > 0)
                        loadVideos(tasksListResource.data);
                    else verifyRowExistenceAndRemove(rowTestTasks);
                }

                if (DEBUG) Log.d(TAG, "task response");

                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null) {
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                } else if (DEBUG) Log.d(TAG, "Status ERROR");

                dismissLoading();
            }

        });

    }


    private DreamTVApp getApplication() {
        return ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
    }

    private void userRegistration() {

        if (DEBUG) Log.d(TAG, "userRegistration()");
        String token = getApplication().getToken();
        User user = getApplication().getUser();
        if (token == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            login(user.email);


        LiveData<Resource<User>> userDetails = mViewModel.fetchUserDetails();
        userDetails.observe(getViewLifecycleOwner(), userResource -> {
            if (userResource.status.equals(Resource.Status.SUCCESS)) {

                if (userResource.data != null) {
                    if (!userResource.data.interfaceLanguage.equals(LocaleHelper.getLanguage(getActivity()))) {
                        Log.d(TAG, "fetchUserDetails() response!: userResource.data.interfaceLanguage=" + userResource.data.interfaceLanguage + " LocaleHelper.getLanguage(getActivity()):" + LocaleHelper.getLanguage(getActivity()));
                        LocaleHelper.setLocale(getActivity(), userResource.data.interfaceLanguage);
                        Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
//                        Log.d(TAG, "fetchUserDetails() response!");
                    } else {
                        mViewModel.initializeSyncData();
                    }
                }
            }
        });

    }


    private void pickUserAccount() {
        if (DEBUG) Log.d(TAG, "pickUserAccount()");

        /*This will list all available accounts on device without any filtering*/

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);

        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    private void setupVideosList() {
        if (DEBUG) Log.d(TAG, "New mRowsAdapter()");
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }


    private void loadVideos(TasksList tasksList) {

        String category = tasksList.category;

        if (DEBUG) Log.d(TAG, "Loading video => Category:" + category);

        List<Card> cards = new ArrayList<>();

        for (Task task : tasksList.data) {
            cards.add(new Card(task, tasksList.category));
        }


        DiffCallback<Card> diffCallback = new DiffCallback<Card>() {
            @Override
            public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return oldItem.getTask().taskId == newItem.getTask().taskId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return Objects.equals(oldItem.getTask(), newItem.getTask());
            }
        };


        ListRow listRow;
        switch (category) {
            case TASKS_MY_LIST_CAT:
                listRow = rowMyListTasks;
                break;
            case TASKS_FINISHED_CAT:
                listRow = rowFinishedTasks;
                break;
            case TASKS_CONTINUE_CAT:
                listRow = rowContinueTasks;
                break;
            case TASKS_ALL_CAT:
                listRow = rowAllTasks;
                break;
            case TASKS_TEST_CAT:
                listRow = rowTestTasks;
                break;
            default:
                listRow = rowAllTasks;
                break;
        }


        int indexOfRow = mRowsAdapter.indexOf(listRow);

        ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) listRow.getAdapter());

        if (indexOfRow != -1)
            arrayObjectAdapter.setItems(cards, diffCallback);
        else {

            arrayObjectAdapter.clear(); //clear row before add new ones

            arrayObjectAdapter.addAll(arrayObjectAdapter.size(), cards);

            mRowsAdapter.add(0, listRow);
        }

        setAdapter(mRowsAdapter);

    }

    private void categoriesRowSettings(Category[] categories) {
        List<Card> cards = new ArrayList<>();


        DiffCallback<Card> diffCallback = new DiffCallback<Card>() {
            @Override
            public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                return Objects.equals(oldItem, newItem);
            }
        };


        for (Category category : categories) {
            Card card = new Card();
            card.setTitle(category.name);
//            card.setFooterColor("#c51162");
            card.setLocalImageResource(category.imageName);
            cards.add(card);
        }

        int indexOfRow = mRowsAdapter.indexOf(rowCategories);

        ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) rowCategories.getAdapter());

        if (indexOfRow != -1)
            arrayObjectAdapter.setItems(cards, diffCallback);
        else {

            arrayObjectAdapter.clear(); //clear row before add new ones

            arrayObjectAdapter.addAll(arrayObjectAdapter.size(), cards);

            mRowsAdapter.add(0, rowCategories);
        }


        setAdapter(mRowsAdapter);

    }


    private void reorderRowSettings() {

        int lastIndexOf = mRowsAdapter.indexOf(rowSettings);

        if (lastIndexOf == -1) //If preferences already exists, first we remove it and then add it to the end of the list
            mRowsAdapter.add(rowSettings);

        setAdapter(mRowsAdapter);
    }

    private void verifyRowExistenceAndRemove(ListRow listRow) {

        if (mRowsAdapter.indexOf(listRow) != -1) {
            ((ArrayObjectAdapter) listRow.getAdapter()).clear();//clear elements from row

            mRowsAdapter.remove(listRow);
        }
    }


    private void setupUIElements() {

        setBadgeDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.dreamtv_logo, null));
//        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent
        // over title
//        setHeadersState(HEADERS_DISABLED);
        setHeadersTransitionOnBackEnabled(false);

//         set fastLane (or headers) background color
//        setBrandColor(ContextCompat.getColor(getActivity(), R.color.accent));
        // set search icon color
//        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));

    }


    private void setupEventListeners() {
        setOnSearchClickedListener(view -> {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(intent);
                }
        );

        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private void firebaseAnalyticsSettingsReportEvent(User user) {
        boolean testingMode = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()))
                .getBoolean(getActivity().getString(R.string.pref_key_testing_mode), false);

        Bundle bundle = new Bundle();

        if (testingMode)
            bundle.putBoolean(FIREBASE_KEY_TESTING_MODE, true);
        else
            bundle.putBoolean(FIREBASE_KEY_TESTING_MODE, false);

        //User Settings Saved - Analytics Report Event
        bundle.putString(FIREBASE_KEY_SUB_LANGUAGE, user.subLanguage);
        bundle.putString(FIREBASE_KEY_AUDIO_LANGUAGE, user.audioLanguage);
        bundle.putString(FIREBASE_KEY_INTERFACE_MODE, user.interfaceMode);
        bundle.putString(FIREBASE_KEY_INTERFACE_LANGUAGE, user.interfaceLanguage);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN, bundle);
    }

    private void updateUser(User userUpdated) {
        updateUserLiveData = mViewModel.updateUser(userUpdated);

        updateUserLiveData.observe(getViewLifecycleOwner(), response -> {
            if (response.status.equals(Resource.Status.SUCCESS)) {
                if (DEBUG) Log.d(TAG, "Response from userUpdate");
                if (response.data != null) {
                    if (DEBUG) Log.d(TAG, response.data.toString());

                    firebaseAnalyticsSettingsReportEvent(userUpdated);
                }
            } else if (response.status.equals(Resource.Status.ERROR)) {
                //TODO do something error
                if (response.message != null) {
                    if (DEBUG) Log.d(TAG, response.message);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }
            }


        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                if (DEBUG) Log.d(TAG, "onActivityResult() - Result from pickAccount()");

                // Receiving a result from the AccountPicker
                login(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            } else if (requestCode == REQUEST_APP_SETTINGS) {
                // Parameters considered here:
                // pref_key_list_app_languages
                // pref_key_list_interface_mode
                // pref_key_testing_mode
                User userToUpdate = data.getParcelableExtra(INTENT_EXTRA_USER_UPDATED);
                updateUser(userToUpdate);

                boolean restart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);

                if (restart) {
                    //To update screen language
                    Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_APP_SETTINGS - Different language. Updating screen.");
                } else {
                    //we check is we are not in testing mode. If the language screen does not recreate the activity,
                    // we manually delete the row testing
                    boolean testingMode = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()))
                            .getBoolean(getActivity().getString(R.string.pref_key_testing_mode), false);

                    if (!testingMode) {
                        verifyRowExistenceAndRemove(rowTestTasks);
                        if (DEBUG)
                            Log.d(TAG, "REQUEST_APP_SETTINGS - Removed test category.");
                    } else {
                        mViewModel.updateTaskByCategory(TASKS_TEST_CAT);
                        if (DEBUG)
                            Log.d(TAG, "REQUEST_APP_SETTINGS - Added test category.");
                    }
                }
            } else if (requestCode == REQUEST_VIDEO_SETTINGS) {
                // Parameters considered here:
                // pref_key_video_duration
                // pref_key_list_audio_languages
                User userToUpdate = data.getParcelableExtra(INTENT_EXTRA_USER_UPDATED);
                updateUser(userToUpdate);

                boolean restart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);

                if (restart) {
                    //To update screen language
                    Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_VIDEO_SETTINGS - Different video audio language. Updating screen.");
                } else {
                    boolean callAllCategoriesTasks = data.getBooleanExtra(INTENT_EXTRA_CALL_TASKS, false);
                    if (callAllCategoriesTasks) {
                        mViewModel.updateTaskByCategory(TASKS_ALL_CAT);
                        mViewModel.updateTaskByCategory(TASKS_CONTINUE_CAT);
                        mViewModel.updateTaskByCategory(TASKS_FINISHED_CAT);
                        mViewModel.updateTaskByCategory(TASKS_MY_LIST_CAT);
                        mViewModel.updateTaskByCategory(TASKS_TEST_CAT);
                        if (DEBUG)
                            Log.d(TAG, "REQUEST_VIDEO_SETTINGS - Changed video duration. Call all Tasks again.");
                    }
                }

            } else if (requestCode == REQUEST_STT_SETTINGS) {
                // Parameters considered here:
                // pref_key_list_subtitle_languages
                // pref_key_subtitle_size
                User userToUpdate = data.getParcelableExtra(INTENT_EXTRA_USER_UPDATED);
                updateUser(userToUpdate);

                boolean restart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);

                if (restart) {
                    //To update screen language
                    Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_STT_SETTINGS - Different video subtitle language. Updating screen.");
                }

            }
        }

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
        if (!Objects.requireNonNull(getActivity()).isFinishing())
            loadingDialog.show();

    }


    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card card = (Card) item;
                if (row.getHeaderItem().getName().equals(getString(R.string.title_preferences_category))) {
                    if (card.getTitle().equals(getString(R.string.pref_title_video_settings))) {
                        Intent intent = new Intent(getActivity(), VideoPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_VIDEO_SETTINGS);
                    } else if (card.getTitle().equals(getString(R.string.pref_title_subtitle_settings))) {
                        Intent intent = new Intent(getActivity(), SubtitlePreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_STT_SETTINGS);
                    } else if (card.getTitle().equals(getString(R.string.pref_title_app_settings))) {
                        Intent intent = new Intent(getActivity(), AppPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_APP_SETTINGS);
                    }
                } else if (row.getHeaderItem().getName().equals(getString(R.string.title_topics_category))) {
                    Intent intent = new Intent(getActivity(), CategoryActivity.class);
                    intent.putExtra(INTENT_EXTRA_TOPIC_NAME, card.getTitle());
                    startActivity(intent);
                } else {
                    Task task = card.getTask();

                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(INTENT_TASK, task);
                    intent.putExtra(INTENT_CATEGORY, card.getCategory());

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Objects.requireNonNull(getActivity()),
                            ((BaseCardView) itemViewHolder.view),
                            VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();

                    startActivity(intent, bundle);
                }
            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }


}
