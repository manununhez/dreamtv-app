package com.dream.dreamtv.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.Card;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.Category.Type;
import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.VideoTopic;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.Resource.Status;
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

import timber.log.Timber;

import static com.dream.dreamtv.data.model.Category.Type.CONTINUE;
import static com.dream.dreamtv.data.model.Category.Type.FINISHED;
import static com.dream.dreamtv.data.model.Category.Type.MY_LIST;
import static com.dream.dreamtv.data.model.Category.Type.NEW;
import static com.dream.dreamtv.data.model.Category.Type.SETTINGS;
import static com.dream.dreamtv.data.model.Category.Type.TEST;
import static com.dream.dreamtv.data.model.Category.Type.TOPICS;
import static com.dream.dreamtv.utils.Constants.EMPTY_ITEM;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_MAX_VIDEO_DURATION_PREFS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_MIN_VIDEO_DURATION_PREFS;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SETTINGS_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_SUBTITLE_SIZE_PREFS;
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
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TESTING_MODE;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TOPIC_NAME;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_UPDATE_USER;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.STATUS_ERROR;


public class HomeFragment extends BrowseSupportFragment {
    private static final String ICON_SETTINGS_APP = "ic_settings_app";
    private static final String ICON_SETTINGS_VIDEO = "ic_settings_video";
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
    private FirebaseAnalytics mFirebaseAnalytics;
    private LiveData<Resource<TasksList[]>> tasksLiveData;
    private LiveData<Resource<VideoTopic[]>> categoriesLiveData;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.i("onActivityCreated()");

        // Get the ViewModel from the factory
        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(requireContext());
        mViewModel = ViewModelProviders.of(this, factory).get(HomeViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());

        mViewModel.initSyncData();

        setupVideosList();

        instantiateLoading();

        initCategoriesRow();

        fetchTasks();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHeadersState(HEADERS_DISABLED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Timber.d("onDestroyView");

        if (tasksLiveData != null)
            tasksLiveData.removeObservers(getViewLifecycleOwner());


        if (categoriesLiveData != null)
            categoriesLiveData.removeObservers(getViewLifecycleOwner());
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

                boolean extraIntentRestart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);
                boolean extraIntentUpdateUser = data.getBooleanExtra(INTENT_EXTRA_UPDATE_USER, false);

                if (extraIntentUpdateUser) {
                    mViewModel.updateUser(mViewModel.getUser());
                    firebaseLogEvents_SettingsUpdate();
                }

                if (extraIntentRestart) {
                    //To update screen language
                    requireActivity().recreate(); //Recreate activity

                    Timber.d("REQUEST_APP_SETTINGS - Different language, updating screen.");
                } else {
                    boolean extraIntentTestingMode = data.getBooleanExtra(INTENT_EXTRA_TESTING_MODE, false);
                    //we check is we are not in testing mode. If the language screen does not recreate the activity,
                    // we manually delete the row testing

                    if (extraIntentTestingMode) {
                        mViewModel.syncTasks();
//                        mViewModel.refreshTasks(true);
                        Timber.d("REQUEST_APP_SETTINGS - Changed testing config, calling all Tasks again.");

                    }
                }
            } else if (requestCode == REQUEST_VIDEO_SETTINGS) {
                // Parameters considered here:
                // pref_key_video_duration
                // pref_key_list_audio_languages

                boolean extraIntentRestart = data.getBooleanExtra(INTENT_EXTRA_RESTART, false);
                boolean extraIntentUpdateUser = data.getBooleanExtra(INTENT_EXTRA_UPDATE_USER, false);

                if (extraIntentUpdateUser) {
                    mViewModel.updateUser(mViewModel.getUser());
                    firebaseLogEvents_SettingsUpdate();
                }

                if (extraIntentRestart) {
                    //To update screen language
                    requireActivity().recreate(); //Recreate activity

                    Timber.d("REQUEST_VIDEO_SETTINGS - Different video audio language, updating screen to change app language");
                } else {
                    boolean extraIntentCallAllCategoriesTasks = data.getBooleanExtra(INTENT_EXTRA_CALL_TASKS, false);
                    if (extraIntentCallAllCategoriesTasks) {
                        mViewModel.syncTasks();
//                        mViewModel.refreshTasks(true);

                        Timber.d("REQUEST_VIDEO_SETTINGS - Changed video duration, calling all Tasks again.");
                    }
                }

            }
        }

    }


    private void fetchTasks() {
//        mViewModel.refreshTasks(true);

        tasksLiveData = mViewModel.fetchTasks();
        tasksLiveData.removeObservers(getViewLifecycleOwner());
        tasksLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource != null) {
                Status status = tasksListResource.status;

                if (status.equals(Status.LOADING))
                    showLoading();
                else if (status.equals(Status.SUCCESS)) {
                    TasksList[] data = tasksListResource.data;
                    if (data != null) {
                        displayData(data);

                    }

                    dismissLoading();
                } else if (status.equals(Status.ERROR)) {
                    String message = tasksListResource.message;

                    Timber.d(message != null ? message : STATUS_ERROR);

                    dismissLoading();
                }
            }
        });

    }

    private void displayData(TasksList[] data) {
//        mViewModel.refreshTasks(false);

        for (TasksList tasksList : data) {
            verifyRowExistenceAndRemove(tasksList.getCategory().getCategoryType());

            if (tasksList.getCategory().isVisible()) { //if the category is not empty
                if (tasksList.getCategory().getCategoryType().equals(SETTINGS)) {
                    addRowSettings();
                } else if (tasksList.getCategory().getCategoryType().equals(TOPICS)) {
                    addTopicsCategory();
                } else {
                    if (tasksList.getCategory().getCategoryType().equals(TEST)) {
                        if (mViewModel.getTestingMode()) //We add testing category only if settings says so
                            loadVideos(tasksList);
                    } else {
                        loadVideos(tasksList); //any other category is loaded
                    }
                }
            }
        }
    }

    private void addTopicsCategory() {
        categoriesLiveData = mViewModel.fetchCategories();
        categoriesLiveData.removeObservers(getViewLifecycleOwner());
        categoriesLiveData.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                Status status = resource.status;

                if (status.equals(Status.LOADING)) {

                } else if (status.equals(Status.SUCCESS)) {
                    VideoTopic[] data = resource.data;

                    if (data != null) {
                        loadCategories(data);
                    }
                } else if (status.equals(Status.ERROR)) {
                    String message = resource.message;

                    Timber.d(message != null ? message : STATUS_ERROR);

                    dismissLoading();
                }

            }
        });
    }

    private void initCategoriesRow() {
        Timber.d("initCategoriesRow()");


        rowSettings = createListRow(getString(R.string.title_preferences_category), SETTINGS);

        rowCategories = createListRow(getString(R.string.title_topics_category), TOPICS);

        rowAllTasks = createListRow(getString(R.string.title_check_new_tasks_category), NEW);
        rowMyListTasks = createListRow(getString(R.string.title_my_list_category), MY_LIST);
        rowFinishedTasks = createListRow(getString(R.string.title_finished_category), FINISHED);
        rowContinueTasks = createListRow(getString(R.string.title_continue_watching_category), CONTINUE);
        rowTestTasks = createListRow(getString(R.string.title_test_category), TEST);

    }

    private ListRow createListRow(String title, Type category) {

        switch (category) {
            case SETTINGS:
                ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(new CardPresenterSelector(requireContext()));

                gridRowAdapter.add(new Card(getString(R.string.pref_title_app_settings), Card.Type.ICON, ICON_SETTINGS_APP));
                gridRowAdapter.add(new Card(getString(R.string.pref_title_video_settings), Card.Type.ICON, ICON_SETTINGS_VIDEO));

                return new ListRow(new HeaderItem(title), gridRowAdapter);

            case TOPICS:
            case NEW:
            case MY_LIST:
            case FINISHED:
            case CONTINUE:
            case TEST:
                return new ListRow(new HeaderItem(title),
                        new ArrayObjectAdapter(new CardPresenterSelector(requireContext())));
            default:
                throw new RuntimeException("VideoTopic " + category + " not contemplated");
        }
    }


    private void setupVideosList() {
        Timber.d("setupVideosList()");

        setBadgeDrawable(requireContext().getResources().getDrawable(R.drawable.dreamtv_logo, null));
        setHeadersTransitionOnBackEnabled(false);


        setOnSearchClickedListener(view -> {
                    Intent intent = new Intent(requireContext(), SearchActivity.class);
                    startActivity(intent);
                }
        );

        setOnItemViewClickedListener(new ItemViewClickedListener());

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

    }


    private void loadVideos(TasksList tasksList) {

        if (tasksList.getTasks().length > 0) {
            Type category = tasksList.getCategory().getCategoryType();

            Timber.d("Loading video => VideoTopic:%s", category);

            List<Card> cards = new ArrayList<>();

            for (Task task : tasksList.getTasks()) {
                cards.add(new Card(task, Card.Type.SIDE_INFO, tasksList.getCategory().getCategoryType()));
            }


            ListRow listRow = getListRowForCategory(tasksList.getCategory().getCategoryType());

            ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) listRow.getAdapter());
            arrayObjectAdapter.clear(); //clear row before add new ones
            arrayObjectAdapter.addAll(0, cards);

            mRowsAdapter.add(listRow);


            setAdapter(mRowsAdapter);

        }
    }

    private void loadCategories(VideoTopic[] categories) {
        List<Card> cards = new ArrayList<>();

        for (VideoTopic videoTopic : categories) {
            Card card = new Card(videoTopic.getName(), Card.Type.SINGLE_LINE, videoTopic.getImageName());
            cards.add(card);
        }

        ArrayObjectAdapter arrayObjectAdapter = ((ArrayObjectAdapter) rowCategories.getAdapter());
        arrayObjectAdapter.clear(); //clear row before add new ones
        arrayObjectAdapter.addAll(arrayObjectAdapter.size(), cards);

        mRowsAdapter.add(rowCategories);

        setAdapter(mRowsAdapter);

    }

    private ListRow getListRowForCategory(Type category) {
        ListRow listRow;
        switch (category) {
            case TOPICS:
                listRow = rowCategories;
                break;
            case SETTINGS:
                listRow = rowSettings;
                break;
            case MY_LIST:
                listRow = rowMyListTasks;
                break;
            case FINISHED:
                listRow = rowFinishedTasks;
                break;
            case CONTINUE:
                listRow = rowContinueTasks;
                break;
            case NEW:
                listRow = rowAllTasks;
                break;
            case TEST:
                listRow = rowTestTasks;
                break;
            default:
                throw new RuntimeException("Category " + category + " not contemplated!");
        }
        return listRow;
    }


    private void addRowSettings() {

        int lastIndexOf = mRowsAdapter.indexOf(rowSettings);

        if (lastIndexOf == -1) //If preferences already exists, first we remove it and then add it to the end of the list
            mRowsAdapter.add(rowSettings);

        setAdapter(mRowsAdapter);
    }

    private void verifyRowExistenceAndRemove(Category.Type category) {

        ListRow listRow = getListRowForCategory(category);

        if (mRowsAdapter.indexOf(listRow) != -1) {
//            ((ArrayObjectAdapter) listRow.getAdapter()).clear();//clear elements from row

            mRowsAdapter.remove(listRow);
        }
    }


    private void firebaseLogEvents(String value, String logEventName) {
        Bundle bundle = new Bundle();

        if (logEventName.equals(FIREBASE_LOG_EVENT_CATEGORIES)) {
            bundle.putString(FIREBASE_KEY_CATEGORY_SELECTED, value);
        } else if (logEventName.equals(FIREBASE_LOG_EVENT_SETTINGS)) {
            bundle.putString(FIREBASE_KEY_SETTINGS_CATEGORY_SELECTED, value);
        }
        mFirebaseAnalytics.logEvent(logEventName, bundle);

    }

    private void firebaseLogEvents_TaskSelected(String category, int taskId) {
        Bundle bundle = new Bundle();

        bundle.putString(FIREBASE_KEY_TASK_CATEGORY_SELECTED, category);
        bundle.putInt(FIREBASE_KEY_TASK_SELECTED, taskId);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_TASK_SELECTED, bundle);

    }

    private void firebaseLogEvents_SettingsUpdate() {
        boolean testingMode = mViewModel.getTestingMode();

        String subtitleSize = mViewModel.getSubtitleSize();
        VideoDuration videoDuration = mViewModel.getVideoDuration();

        User user = mViewModel.getUser();

        Timber.d("User from firebaseLog: %s", user);

        Bundle bundle = new Bundle();

        // FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN
        bundle.putBoolean(FIREBASE_KEY_TESTING_MODE, testingMode);
        bundle.putString(FIREBASE_KEY_SUB_LANGUAGE, user.getSubLanguage());
        bundle.putString(FIREBASE_KEY_INTERFACE_LANGUAGE, user.getSubLanguage());
        bundle.putString(FIREBASE_KEY_AUDIO_LANGUAGE, user.getAudioLanguage());
        bundle.putString(FIREBASE_KEY_INTERFACE_MODE, user.getInterfaceMode());
        bundle.putString(FIREBASE_KEY_SUBTITLE_SIZE_PREFS, subtitleSize);
        bundle.putInt(FIREBASE_KEY_MIN_VIDEO_DURATION_PREFS, videoDuration.getMinDuration());
        bundle.putInt(FIREBASE_KEY_MAX_VIDEO_DURATION_PREFS, videoDuration.getMaxDuration());


        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN, bundle);

    }


    //********************************************
    // Loading and progress bar related functions
    //********************************************
    private void instantiateLoading() {
        Timber.d("instantiateLoading()");

        loadingDialog = new LoadingDialog(requireContext(), getString(R.string.title_loading_retrieve_tasks));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }

    private void showLoading() {
        if (!requireActivity().isFinishing())
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
                        Intent intent = new Intent(requireContext(), VideoPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_VIDEO_SETTINGS);
                        firebaseLogEvents(card.getTitle(), FIREBASE_LOG_EVENT_SETTINGS);
                    } else if (card.getTitle().equals(getString(R.string.pref_title_app_settings))) {
                        Intent intent = new Intent(requireContext(), AppPreferencesActivity.class);
                        startActivityForResult(intent, REQUEST_APP_SETTINGS);
                        firebaseLogEvents(card.getTitle(), FIREBASE_LOG_EVENT_SETTINGS);
                    }
                } else if (row.getHeaderItem().getName().equals(getString(R.string.title_topics_category))) {
                    Intent intent = new Intent(requireContext(), CategoryActivity.class);
                    intent.putExtra(INTENT_EXTRA_TOPIC_NAME, card.getTitle());
                    startActivity(intent);
                    firebaseLogEvents(card.getTitle(), FIREBASE_LOG_EVENT_CATEGORIES);
                } else {
                    Task task = card.getTask();

                    Intent intent = new Intent(requireContext(), VideoDetailsActivity.class);
                    intent.putExtra(INTENT_TASK, task);
                    intent.putExtra(INTENT_CATEGORY, card.getCategory());

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(), itemViewHolder.view,
                            VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();

                    startActivity(intent, bundle);

                    firebaseLogEvents_TaskSelected(card.getCategory().toString(), task.getTaskId());
                }
            } else
                Toast.makeText(requireContext(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();
        }
    }


}
