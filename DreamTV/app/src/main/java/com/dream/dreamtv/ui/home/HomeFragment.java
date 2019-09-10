package com.dream.dreamtv.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;
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

import com.dream.dreamtv.BuildConfig;
import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.Card;
import com.dream.dreamtv.data.model.Category.Type;
import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.Resource.Status;
import com.dream.dreamtv.data.model.api.Task;
import com.dream.dreamtv.data.model.api.TasksList;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.data.model.api.VideoTopic;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.presenter.CardPresenterSelector;
import com.dream.dreamtv.ui.categories.CategoryActivity;
import com.dream.dreamtv.ui.preferences.AppPreferencesActivity;
import com.dream.dreamtv.ui.preferences.VideoPreferencesActivity;
import com.dream.dreamtv.ui.search.SearchActivity;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.LoadingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.data.model.Category.Type.ALL;
import static com.dream.dreamtv.data.model.Category.Type.CONTINUE;
import static com.dream.dreamtv.data.model.Category.Type.FINISHED;
import static com.dream.dreamtv.data.model.Category.Type.MY_LIST;
import static com.dream.dreamtv.data.model.Category.Type.SETTINGS;
import static com.dream.dreamtv.data.model.Category.Type.TEST;
import static com.dream.dreamtv.data.model.Category.Type.TOPICS;
import static com.dream.dreamtv.utils.Constants.EMPTY_ITEM;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SETTINGS_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUB_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TASK_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TASK_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_CATEGORIES;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_SETTINGS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_TASK_SELECTED;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_CALL_TASKS;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_RESTART;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TOPIC_NAME;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_USER_UPDATED;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;


public class HomeFragment extends BrowseSupportFragment {
    public static final String ICON_SETTINGS_APP = "ic_settings_app";
    public static final String ICON_SETTINGS_VIDEO = "ic_settings_video";
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    //    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;
    private static final int REQUEST_APP_SETTINGS = 45690;
    private static final int REQUEST_VIDEO_SETTINGS = 45710;
    private ArrayObjectAdapter mRowsAdapter;
    private HomeViewModel mViewModel;
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
    private LiveData<Resource<VideoTopic[]>> categoriesLiveData;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach()");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated()");

//        // Get the ViewModel from the factory
        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(getContext());
        mViewModel = ViewModelProviders.of(this, factory).get(HomeViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        setupVideosList();

        instantiateLoading();

        initSettingsRow();

        syncData();
    }

    public FragmentActivity getContext() {
        return Objects.requireNonNull(getActivity());
    }

    private void syncData() {
        if (DEBUG) Log.d(TAG, "syncData()");


        boolean testingMode = mViewModel.getTestingMode();

        addRowSettings();

//------- ALL TASKS
        allTaskLiveData = mViewModel.requestTasksByCategory(ALL);
        allTaskLiveData.removeObservers(getViewLifecycleOwner());
        allTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            TasksList data = tasksListResource.data;
            String errorMessage = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    if (data.data != null && data.data.length > 0)
                        loadVideos(data);
                    else verifyRowExistenceAndRemove(rowAllTasks);
                }

                if (DEBUG) Log.d(TAG, "task response: rowAllTasks");

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

        });
        mViewModel.updateTaskByCategory(ALL);


//------- CONTINUE TASKS
        continueTaskLiveData = mViewModel.requestTasksByCategory(CONTINUE);
        continueTaskLiveData.removeObservers(getViewLifecycleOwner());
        continueTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            TasksList data = tasksListResource.data;
            String errorMessage = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    if (data.data != null && data.data.length > 0)
                        loadVideos(data);
                    else verifyRowExistenceAndRemove(rowContinueTasks);
                }

                if (DEBUG) Log.d(TAG, "task response: rowContinueTasks");


                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

        });

        mViewModel.updateTaskByCategory(CONTINUE);


//------- FINISHED TASKS
        finishedTaskLiveData = mViewModel.requestTasksByCategory(FINISHED);
        finishedTaskLiveData.removeObservers(getViewLifecycleOwner());
        finishedTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            TasksList data = tasksListResource.data;
            String errorMessage = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    if (data.data != null && data.data.length > 0)
                        loadVideos(data);
                    else verifyRowExistenceAndRemove(rowFinishedTasks);
                }

                if (DEBUG) Log.d(TAG, "task response: rowFinishedTasks");

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

            // TODO throw new RuntimeException("Get list sorted of finished tasks");

        });

        mViewModel.updateTaskByCategory(FINISHED);


//------- MY LIST TASKS
        myListTaskLiveData = mViewModel.requestTasksByCategory(MY_LIST);
        myListTaskLiveData.removeObservers(getViewLifecycleOwner());
        myListTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            TasksList data = tasksListResource.data;
            String errorMessage = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    if (data.data != null && data.data.length > 0) {
                        loadVideos(data);
                    } else verifyRowExistenceAndRemove(rowMyListTasks);
                }

                if (DEBUG) Log.d(TAG, "task response: rowMyListTasks");


                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }

            //  TODO throw new RuntimeException("Get list sorted of my list tasks");

        });

        mViewModel.updateTaskByCategory(MY_LIST);


//------- TEST TASKS
        testTaskLiveData = mViewModel.requestTasksByCategory(TEST);
        testTaskLiveData.removeObservers(getViewLifecycleOwner());
        testTaskLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            TasksList data = tasksListResource.data;
            String errorMessage = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    if (data.data != null && data.data.length > 0)
                        loadVideos(data);
                    else verifyRowExistenceAndRemove(rowTestTasks);
                }

                if (DEBUG) Log.d(TAG, "task response: rowTestTasks");


                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else if (DEBUG) Log.d(TAG, "Status ERROR");

                dismissLoading();
            }

        });

        if (testingMode)
            mViewModel.updateTaskByCategory(TEST);


//        REASONS
        mViewModel.fetchReasons();

//        VIDEO TESTS
        mViewModel.fetchVideoTestsDetails();

//        CATEGORIES
        categoriesLiveData = mViewModel.fetchCategories();
        categoriesLiveData.removeObservers(getViewLifecycleOwner());
        categoriesLiveData.observe(getViewLifecycleOwner(), resource -> {
            Status status = resource.status;
            VideoTopic[] data = resource.data;
            String errorMessage = resource.message;

            if (status.equals(Status.LOADING)) {
                showLoading();
            } else if (status.equals(Status.SUCCESS)) {
                if (data != null) {
                    categoriesRowSettings(data);
                } else verifyRowExistenceAndRemove(rowCategories);


                if (DEBUG) Log.d(TAG, "task response: rowCategories");

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                //TODO do something
                if (errorMessage != null) {
                    if (DEBUG) Log.d(TAG, errorMessage);
                } else {
                    if (DEBUG) Log.d(TAG, "Status ERROR");
                }

                dismissLoading();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHeadersState(HEADERS_DISABLED);
    }

    private void initSettingsRow() {
        if (DEBUG) Log.d(TAG, "initSettingsRow()");


        rowSettings = createListRow(getString(R.string.title_preferences_category), SETTINGS);

        rowCategories = createListRow(getString(R.string.title_topics_category), TOPICS);

        rowAllTasks = createListRow(getString(R.string.title_check_new_tasks_category), ALL);
        rowMyListTasks = createListRow(getString(R.string.title_my_list_category), MY_LIST);
        rowFinishedTasks = createListRow(getString(R.string.title_finished_category), FINISHED);
        rowContinueTasks = createListRow(getString(R.string.title_continue_watching_category), CONTINUE);
        rowTestTasks = createListRow(getString(R.string.title_test_category), TEST);

    }

    private ListRow createListRow(String title, Type category) {

        switch (category) {
            case SETTINGS:
                ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(new CardPresenterSelector(getContext()));

                gridRowAdapter.add(new Card(getString(R.string.pref_title_app_settings), Card.Type.ICON, ICON_SETTINGS_APP));
                gridRowAdapter.add(new Card(getString(R.string.pref_title_video_settings), Card.Type.ICON, ICON_SETTINGS_VIDEO));

                return new ListRow(new HeaderItem(title), gridRowAdapter);

            case TOPICS:
            case ALL:
            case MY_LIST:
            case FINISHED:
            case CONTINUE:
            case TEST:
                return new ListRow(new HeaderItem(title), new ArrayObjectAdapter(new CardPresenterSelector(getContext())));
            default:
                throw new RuntimeException("VideoTopic " + category + " not contemplated");
        }
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

        if (categoriesLiveData != null)
            categoriesLiveData.removeObservers(getViewLifecycleOwner());

    }


    private void setupVideosList() {
        if (DEBUG) Log.d(TAG, "setupVideosList()");

        setBadgeDrawable(getContext().getResources().getDrawable(R.drawable.dreamtv_logo, null));
        setHeadersTransitionOnBackEnabled(false);


        setOnSearchClickedListener(view -> {
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    startActivity(intent);
                }
        );

        setOnItemViewClickedListener(new ItemViewClickedListener());

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

    }


    private void loadVideos(TasksList tasksList) {

        Type category = tasksList.category;

        if (DEBUG) Log.d(TAG, "Loading video => VideoTopic:" + category);

        List<Card> cards = new ArrayList<>();

        for (Task task : tasksList.data) {
            cards.add(new Card(task, Card.Type.SIDE_INFO, tasksList.category));
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
            case MY_LIST:
                listRow = rowMyListTasks;
                break;
            case FINISHED:
                listRow = rowFinishedTasks;
                break;
            case CONTINUE:
                listRow = rowContinueTasks;
                break;
            case ALL:
                listRow = rowAllTasks;
                break;
            case TEST:
                listRow = rowTestTasks;
                break;
            default:
                throw new RuntimeException("Category " + category + " not contemplated!");
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

    private void categoriesRowSettings(VideoTopic[] categories) {
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


        for (VideoTopic videoTopic : categories) {
            Card card = new Card(videoTopic.name, Card.Type.SINGLE_LINE, videoTopic.imageName);
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


    private void addRowSettings() {

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


    private void firebaseLoginEvents(String value, String logEventName) {
        Bundle bundle = new Bundle();

        if (logEventName.equals(FIREBASE_LOG_EVENT_CATEGORIES)) {
            bundle.putString(FIREBASE_KEY_CATEGORY_SELECTED, value);
        } else if (logEventName.equals(FIREBASE_LOG_EVENT_SETTINGS)) {
            bundle.putString(FIREBASE_KEY_SETTINGS_CATEGORY_SELECTED, value);
        }
        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    private void firebaseLoginEvents(String category, int taskId, String logEventName) {
        Bundle bundle = new Bundle();

        if (logEventName.equals(FIREBASE_LOG_EVENT_TASK_SELECTED)) {
            bundle.putString(FIREBASE_KEY_TASK_CATEGORY_SELECTED, category);
            bundle.putInt(FIREBASE_KEY_TASK_SELECTED, taskId);
            mFirebaseAnalytics.logEvent(logEventName, bundle);
        }

    }

    private void firebaseLoginEvents(String logEventName) {
        boolean testingMode = mViewModel.getTestingMode();

        User user = mViewModel.getUser();

        Log.d(TAG, "User from firebaseLog: " + user);

        Bundle bundle = new Bundle();

        // FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN
        if (FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN.equals(logEventName)) {
            if (testingMode)
                bundle.putBoolean(FIREBASE_KEY_TESTING_MODE, true);
            else
                bundle.putBoolean(FIREBASE_KEY_TESTING_MODE, false);

            //User Settings Saved
            bundle.putString(FIREBASE_KEY_SUB_LANGUAGE, user.subLanguage);
            bundle.putString(FIREBASE_KEY_AUDIO_LANGUAGE, user.audioLanguage);
            bundle.putString(FIREBASE_KEY_INTERFACE_MODE, user.interfaceMode);
        } else {//User Settings Saved - Analytics Report Event
            bundle.putString(FIREBASE_KEY_SUB_LANGUAGE, user.subLanguage);
            bundle.putString(FIREBASE_KEY_AUDIO_LANGUAGE, user.audioLanguage);
            bundle.putString(FIREBASE_KEY_INTERFACE_MODE, user.interfaceMode);
        }

        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    private void updateUser(User userUpdated) {
        updateUserLiveData = mViewModel.updateUser(userUpdated);
        updateUserLiveData.removeObservers(getViewLifecycleOwner());
        updateUserLiveData.observe(getViewLifecycleOwner(), response -> {
            Status status = response.status;
            User data = response.data;
            String message = response.message;

            if (status.equals(Status.SUCCESS)) {
                if (DEBUG) Log.d(TAG, "Response from userUpdate");
                if (data != null) {
                    if (DEBUG) Log.d(TAG, data.toString());

                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN);
                }
            } else if (status.equals(Status.ERROR)) {
                //TODO do something error
                if (message != null) {
                    if (DEBUG) Log.d(TAG, message);
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
            if (requestCode == REQUEST_APP_SETTINGS) {
                // Parameters considered here:
                // pref_key_list_app_languages
                // pref_key_list_interface_mode
                // pref_key_testing_mode
                User userToUpdate = data.getParcelableExtra(INTENT_EXTRA_USER_UPDATED);
                updateUser(userToUpdate);

                boolean restart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);

                if (restart) {
                    //To update screen language
                    getContext().recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_APP_SETTINGS - Different language. Updating screen.");
                } else {
                    //we check is we are not in testing mode. If the language screen does not recreate the activity,
                    // we manually delete the row testing
                    boolean testingMode = mViewModel.getTestingMode();

                    if (!testingMode) {
                        verifyRowExistenceAndRemove(rowTestTasks);
                        if (DEBUG)
                            Log.d(TAG, "REQUEST_APP_SETTINGS - Removed test category.");
                    } else {
                        mViewModel.updateTaskByCategory(TEST);
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
                    //we force to call firebaselog here, because otherwise the app restarts and the data get lost
                    firebaseLoginEvents(FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN);
                    //To update screen language
                    getContext().recreate(); //Recreate activity
                    if (DEBUG)
                        Log.d(TAG, "REQUEST_VIDEO_SETTINGS - Different video audio language. Updating screen.");
                } else {
                    boolean callAllCategoriesTasks = data.getBooleanExtra(INTENT_EXTRA_CALL_TASKS, false);
                    if (callAllCategoriesTasks) {
                        mViewModel.updateTaskByCategory(ALL);
                        mViewModel.updateTaskByCategory(CONTINUE);
                        mViewModel.updateTaskByCategory(FINISHED);
                        mViewModel.updateTaskByCategory(MY_LIST);
                        mViewModel.updateTaskByCategory(TEST);
                        if (DEBUG)
                            Log.d(TAG, "REQUEST_VIDEO_SETTINGS - Changed video duration. Call all Tasks again.");
                    }
                }

            }
        }

    }

    //********************************************
    // Loading and progress bar related functions
    //********************************************
    private void instantiateLoading() {
        if (DEBUG) Log.d(TAG, "instantiateLoading()");

        loadingDialog = new LoadingDialog(getContext(), getString(R.string.title_loading_retrieve_tasks));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }

    private void showLoading() {
        if (!getContext().isFinishing())
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
                        Intent intent = new Intent(getContext(), VideoPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_VIDEO_SETTINGS);
                        firebaseLoginEvents(card.getTitle(), FIREBASE_LOG_EVENT_SETTINGS);
                    } else if (card.getTitle().equals(getString(R.string.pref_title_app_settings))) {
                        Intent intent = new Intent(getContext(), AppPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_APP_SETTINGS);
                        firebaseLoginEvents(card.getTitle(), FIREBASE_LOG_EVENT_SETTINGS);
                    }
                } else if (row.getHeaderItem().getName().equals(getString(R.string.title_topics_category))) {
                    Intent intent = new Intent(getContext(), CategoryActivity.class);
                    intent.putExtra(INTENT_EXTRA_TOPIC_NAME, card.getTitle());
                    startActivity(intent);
                    firebaseLoginEvents(card.getTitle(), FIREBASE_LOG_EVENT_CATEGORIES);
                } else {
                    Task task = card.getTask();

                    Intent intent = new Intent(getContext(), VideoDetailsActivity.class);
                    intent.putExtra(INTENT_TASK, task);
                    intent.putExtra(INTENT_CATEGORY, card.getCategory());

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getContext(), itemViewHolder.view,
                            VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();

                    startActivity(intent, bundle);

                    firebaseLoginEvents(card.getCategory().toString(), task.taskId, FIREBASE_LOG_EVENT_TASK_SELECTED);
                }
            } else
                Toast.makeText(getContext(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();
        }
    }


}
