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
import android.view.View;
import android.widget.Toast;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Card;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.presenter.IconCardPresenter;
import com.dream.dreamtv.presenter.SideInfoCardPresenter;
import com.dream.dreamtv.ui.Settings.SettingsActivity;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String EMPTY_ITEM = "Some item";
    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private ArrayObjectAdapter mRowsAdapter;
    private MainViewModel mViewModel;
    private LoadingDialog loadingDialog;
    private ListRow rowSettings;
    private ListRow rowAllTasks;
    private ListRow rowMyListTasks;
    private ListRow rowFinishedTasks;
    private ListRow rowContinueTasks;
    private ListRow rowTestTasks;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

//        // Get the ViewModel from the factory
        MainViewModelFactory factory = InjectorUtils.provideMainViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(MainViewModel.class);

        setupUIElements();
        setupVideosList();

        setupEventListeners();

        instantiateLoading();

        userRegistration();

        observeResponseFromUserUpdate();

        initSettingsRow();
        populateScreen();

    }

    private void initSettingsRow() {
        HeaderItem gridHeader = new HeaderItem(getString(R.string.title_preferences_category));

        IconCardPresenter mIconCardPresenter = new IconCardPresenter(getActivity());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mIconCardPresenter);
        Card settingsCard = new Card();
        settingsCard.setType(Card.Type.ICON);
        settingsCard.setTitle(Constants.SETTINGS);
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
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>REQUEST LOGIN");
//        showLoading();
        mViewModel.login(email, "com.google"); //TODO change password
    }


    private void initializeSyncData() {
        Log.d(TAG, "initializeSyncData()");

//        showLoading();
        mViewModel.initializeSyncData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        mViewModel.responseFromUserUpdate().removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_ALL).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_CONTINUE).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_FINISHED).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_MY_LIST).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_TEST).removeObservers(getViewLifecycleOwner());


    }

    private void populateScreen() {

        reorderRowSettings();

        mViewModel.requestTasksByCategory(Constants.TASKS_ALL).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_ALL).observe(getViewLifecycleOwner(), taskEntities -> {
            if (taskEntities.length > 0)
                loadVideos(taskEntities, Constants.TASKS_ALL);
            else verifyRowExistenceAndRemove(rowAllTasks);
        });

        mViewModel.requestTasksByCategory(Constants.TASKS_CONTINUE).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_CONTINUE).observe(getViewLifecycleOwner(), taskEntities -> {
            if (taskEntities.length > 0)
                loadVideos(taskEntities, Constants.TASKS_CONTINUE);
            else verifyRowExistenceAndRemove(rowContinueTasks);

        });

        mViewModel.requestTasksByCategory(Constants.TASKS_FINISHED).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_FINISHED).observe(getViewLifecycleOwner(), taskEntities -> {
            if (taskEntities.length > 0)
                loadVideos(taskEntities, Constants.TASKS_FINISHED);
            else verifyRowExistenceAndRemove(rowFinishedTasks);

        });

        mViewModel.requestTasksByCategory(Constants.TASKS_MY_LIST).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_MY_LIST).observe(getViewLifecycleOwner(), taskEntities -> {
            if (taskEntities.length > 0)
                loadVideos(taskEntities, Constants.TASKS_MY_LIST);
            else verifyRowExistenceAndRemove(rowMyListTasks);

        });

        mViewModel.requestTasksByCategory(Constants.TASKS_TEST).removeObservers(getViewLifecycleOwner());
        mViewModel.requestTasksByCategory(Constants.TASKS_TEST).observe(getViewLifecycleOwner(), taskEntities -> {
            if (taskEntities.length > 0)
                loadVideos(taskEntities, Constants.TASKS_TEST);
            else verifyRowExistenceAndRemove(rowTestTasks);

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

        //TODO use dismissLoading
        loadingDialog.dismiss();
    }


    private void showLoading() {

        //TODO use showLoading
        loadingDialog.show();
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
                            reInitScreen(response.data);
                        }
                    } else if (response.status.equals(Resource.Status.ERROR)) {
                        //TODO do something error
                        if (response.message != null)
                            Log.d(TAG, response.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }

                }

//                dismissLoading();
            }
        });
    }

    private void reInitScreen(User user) {
//        setupVideosList();
        updateScreenLanguage(user);

        initializeSyncData();
    }


    private void updateScreenLanguage(User user) {
        if (!LocaleHelper.getLanguage(getActivity()).equals(user.interfaceLanguage)) {
            LocaleHelper.setLocale(getActivity(), user.interfaceLanguage);
            Objects.requireNonNull(getActivity()).recreate(); //Recreate activity
            Log.d(TAG, "Different language. Updating screen.");
        } else {
            //we check is we are not in testing mode. If the language screen does not recreate the activity,
            // we manually delete the row testing
            DreamTVApp dreamTVApp = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
            if (dreamTVApp.getTestingMode().equals(getString(R.string.text_no_option)))
                verifyRowExistenceAndRemove(rowTestTasks);

        }
    }

    private void verifyRowExistenceAndRemove(ListRow listRow) {
        if (mRowsAdapter.indexOf(listRow) != -1)
            mRowsAdapter.remove(listRow);
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


    private void setupVideosList() {
        Log.d(TAG, "New mRowsAdapter()");
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    }


    private void loadVideos(TaskEntity[] tasks, String category) {

        if (tasks != null) {
            Log.d(TAG, "Loading videos => Category:" + category);

            List<Card> cards = new ArrayList<>();

            for (TaskEntity task : tasks) {
                cards.add(new Card(task));
            }


            DiffCallback<Card> diffCallback = new DiffCallback<Card>() {
                @Override
                public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                    return oldItem.getTaskEntity().id == newItem.getTaskEntity().id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                    return oldItem.getTaskEntity().equals(newItem.getTaskEntity());
                }
            };


            ListRow listRow;
            switch (category) {
                case Constants.TASKS_MY_LIST:
                    listRow = rowMyListTasks;
                    break;
                case Constants.TASKS_FINISHED:
                    listRow = rowFinishedTasks;
                    break;
                case Constants.TASKS_CONTINUE:
                    listRow = rowContinueTasks;
                    break;
                case Constants.TASKS_ALL:
                    listRow = rowAllTasks;
                    break;
                case Constants.TASKS_TEST:
                    listRow = rowTestTasks;
                    break;
                default:
                    listRow = rowAllTasks;
                    break;
            }


            int indexOfRow = mRowsAdapter.indexOf(listRow);

            ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) listRow.getAdapter());
            if (indexOfRow != -1) {
                arrayObjectAdapter.setItems(cards, diffCallback);
//                mRowsAdapter.remove(listRow);
            } else {

                arrayObjectAdapter.addAll(arrayObjectAdapter.size(), cards);

                mRowsAdapter.add(0, listRow);
            }


            //TEMP - To check size of rows----
//            HeaderItem headerItem = listRow.getHeaderItem();
//            StringBuilder name = new StringBuilder(headerItem.getName());
//            String value = "(".concat(String.valueOf(listRow.getAdapter().size())).concat(")");
//            if(name.lastIndexOf("(") != -1) name.replace(name.lastIndexOf("("), name.length(), value);
//            else name.append(value);
//            listRow.setHeaderItem(new HeaderItem(name.toString()));
            //-----


            setAdapter(mRowsAdapter);
        }
    }


    private void reorderRowSettings() {

        int lastIndexOf = mRowsAdapter.indexOf(rowSettings);

        if (lastIndexOf == -1) //If settings already exists, first we remove it and then add it to the end of the list
            mRowsAdapter.add(rowSettings);

//        mRowsAdapter.add(rowSettings);

        setAdapter(mRowsAdapter);
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
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), getString(R.string.title_search), Toast.LENGTH_SHORT).show();

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
//        setOnItemViewSelectedListener(new ItemViewSelectedListener());
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

            if (item instanceof Card) {
                Card value = (Card) item;
                if (value.getTitle().equals(Constants.SETTINGS)) {
//                    Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                } else {
                    TaskEntity taskEntity = value.getTaskEntity();

                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra(Constants.USER_DATA_TASK, taskEntity);

                    startActivity(intent);
                }
            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }

}
