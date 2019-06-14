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
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
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
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.presenter.IconCardCustomPresenter;
import com.dream.dreamtv.presenter.SideInfoCardPresenter;
import com.dream.dreamtv.ui.Preferences.PreferencesActivity;
import com.dream.dreamtv.ui.Search.SearchActivity;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUB_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.SETTINGS_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_ALL_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_FINISHED_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;


public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String EMPTY_ITEM = "Some item";
    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private static final int REQUEST_SETTINGS = 45686;
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

    private void initSettingsRow() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));

        IconCardCustomPresenter mIconCardPresenter = new IconCardCustomPresenter(getActivity());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mIconCardPresenter);
        Card settingsCard = new Card();
        settingsCard.setType(Card.Type.ICON);
        settingsCard.setTitle(SETTINGS_CAT);
        settingsCard.setLocalImageResource("ic_settings_settings");
        gridRowAdapter.add(settingsCard);

        rowSettings = new ListRow(gridHeader, gridRowAdapter);


        rowAllTasks = new ListRow(new HeaderItem(getString(R.string.title_check_new_tasks_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowMyListTasks = new ListRow(new HeaderItem(getString(R.string.title_my_list_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowFinishedTasks = new ListRow(new HeaderItem(getString(R.string.title_finished_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowContinueTasks = new ListRow(new HeaderItem(getString(R.string.title_continue_watching_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));
        rowTestTasks = new ListRow(new HeaderItem(getString(R.string.title_test_category)), new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity())));

    }


    private void requestLogin(String email) {
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
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

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
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

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
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

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
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

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
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

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
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (token == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            requestLogin(user.email);

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

        setBadgeDrawable(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.logo_tv, null));
//        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.default_background));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.search_opaque));

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
                if (response.message != null)
                    if (DEBUG) Log.d(TAG, response.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");
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
                requestLogin(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

            } else if (requestCode == REQUEST_SETTINGS) {
                updateUser(data.getParcelableExtra(INTENT_EXTRA_USER_UPDATED));

                boolean restart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);

                if (restart) {
                    //To update screen language
                    Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_SETTINGS - Different language. Updating screen.");
                } else {

                    boolean callAllCategoriesTasks = data.getBooleanExtra(INTENT_EXTRA_CALL_TASKS, false);
                    if (callAllCategoriesTasks) {
                        mViewModel.updateTaskByCategory(TASKS_ALL_CAT);
                        mViewModel.updateTaskByCategory(TASKS_CONTINUE_CAT);
                        mViewModel.updateTaskByCategory(TASKS_FINISHED_CAT);
                        mViewModel.updateTaskByCategory(TASKS_MY_LIST_CAT);
                        mViewModel.updateTaskByCategory(TASKS_TEST_CAT);
                        if (DEBUG) Log.d(TAG, "REQUEST_SETTINGS - Call all Tasks again.");
                    } else {
                        //we check is we are not in testing mode. If the language screen does not recreate the activity,
                        // we manually delete the row testing
                        boolean testingMode = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()))
                                .getBoolean(getActivity().getString(R.string.pref_key_testing_mode), false);

                        if (!testingMode)
                            verifyRowExistenceAndRemove(rowTestTasks);
                        else {
//                            callTestTasks();
                            mViewModel.updateTaskByCategory(TASKS_TEST_CAT);
                            if (DEBUG) Log.d(TAG, "REQUEST_SETTINGS - Call only test Tasks again.");
                        }
                    }
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
        loadingDialog.show();
    }


    public  final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card value = (Card) item;
                if (value.getTitle().equals(SETTINGS_CAT)) {
//                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                    startActivityForResult(intent, REQUEST_SETTINGS);
//                    startActivity(intent);
                } else {
                    Task task = value.getTask();

                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(INTENT_TASK, task);
                    intent.putExtra(INTENT_CATEGORY, value.getCategory());

                    startActivity(intent);
                }
            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }

}
