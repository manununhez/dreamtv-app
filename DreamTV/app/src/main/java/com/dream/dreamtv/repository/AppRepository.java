package com.dream.dreamtv.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dream.dreamtv.data.local.prefs.AppPreferencesHelper;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.networking.NetworkDataSourceImpl;
import com.dream.dreamtv.data.networking.model.AuthResponse;
import com.dream.dreamtv.data.networking.model.ErrorReason;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.Resource.Status;
import com.dream.dreamtv.data.networking.model.SubtitleResponse;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.data.networking.model.TaskRequest;
import com.dream.dreamtv.data.networking.model.TasksList;
import com.dream.dreamtv.data.networking.model.UserTask;
import com.dream.dreamtv.data.networking.model.UserTaskError;
import com.dream.dreamtv.data.networking.model.VideoTest;
import com.dream.dreamtv.data.networking.model.VideoTopicSchema;
import com.dream.dreamtv.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class AppRepository {

    // For Singleton instantiation
    private static AppRepository INSTANCE;
    private final NetworkDataSourceImpl mNetworkDataSource;
    private AppPreferencesHelper mPreferencesHelper;

    private AppRepository(NetworkDataSourceImpl networkDataSource, AppPreferencesHelper preferencesHelper) {
        mNetworkDataSource = networkDataSource;
        mPreferencesHelper = preferencesHelper;

        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        // Group of current weathers that corresponds to the weather of all cities saved in the DB. Updated
        // using job schedules
        MutableLiveData<Resource<AuthResponse>> auth = getAuthResponse();
        MutableLiveData<Resource<User>> newUser = fetchUserDetails();
        MutableLiveData<Resource<User>> updatedUser = updateUserDetails();
        MutableLiveData<Resource<ErrorReason[]>> reasonsResponse = fetchReasonsResponse();
        MutableLiveData<Resource<VideoTest[]>> videoTestsResponse = fetchVideoTestsResponse();

        newUser.observeForever(userResource -> {
            if (userResource != null) {
                Status status = userResource.status;
                User data = userResource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new user data into preferences

                    setUser(data);

                    Timber.d("New user data - setUser() called");
                }
            }
        });

        updatedUser.observeForever(userResource -> {
            if (userResource != null) {
                Status status = userResource.status;
                User data = userResource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new weather data into the database
                    setUser(data);

                    Timber.d("Update user data - setUser() called: subLanguage = %s", data.getSubLanguage());
                }
            }
        });

        reasonsResponse.observeForever(reasonsResource -> {
            if (reasonsResource != null) {
                Status status = reasonsResource.status;
                ErrorReason[] data = reasonsResource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new weather data into the database
                    mPreferencesHelper.setReasons(data);

                    Timber.d("Reasons data - setReasons() called");
                }
            }
        });

        videoTestsResponse.observeForever(videoTestResource -> {
            if (videoTestResource != null) {
                Status status = videoTestResource.status;
                VideoTest[] data = videoTestResource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new weather data into the database
                    mPreferencesHelper.setVideoTests(data);

                    Timber.d("VideoTests data - setVideoTests() called");
                }
            }
        });

        auth.observeForever(resource -> {
            if (resource != null) {
                Status status = resource.status;
                AuthResponse data = resource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new weather data into the database
                    if (data != null) {
                        mPreferencesHelper.setAccessToken(data.token);
                        Timber.d("AccessToken updated - setAccessToken() called");
                    }
                }
            }
        });

    }


    public synchronized static AppRepository getInstance(/*TaskDao taskDao,*/
            NetworkDataSourceImpl networkDataSource,
            AppExecutors executors, AppPreferencesHelper preferencesHelper) {
        Timber.d("Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(/*taskDao, */networkDataSource, preferencesHelper);
                Timber.d("Made new repository");
            }
        }
        return INSTANCE;
    }

    /***************************
     **  NETWORK DATA SOURCE  **
     ***************************/
    public LiveData<Resource<TasksList>> requestTaskByCategory(Category.Type category) {
        switch (category) {
            case MY_LIST:
                return mNetworkDataSource.responseFromFetchMyListTasks();
            case FINISHED:
                return mNetworkDataSource.responseFromFetchFinishedTasks();
            case CONTINUE:
                return mNetworkDataSource.responseFromFetchContinueTasks();
            case ALL:
                return mNetworkDataSource.responseFromFetchAllTasks();
            case TEST:
                return mNetworkDataSource.responseFromFetchTestTasks();
            default:
                throw new RuntimeException("Category " + category + " not contemplated!");

        }
    }

    public void updateTasksCategory(Category.Type category) {
        VideoDuration videoDuration = getVideoDurationPref();
        TaskRequest taskRequest = new TaskRequest(category, videoDuration);

        switch (category) {
            case MY_LIST:
                mNetworkDataSource.fetchMyListTaskCategory(taskRequest);
                break;
            case FINISHED:
                mNetworkDataSource.fetchFinishedTaskCategory(taskRequest);
                break;
            case CONTINUE:
                mNetworkDataSource.fetchContinueTaskCategory(taskRequest);
                break;
            case ALL:
                mNetworkDataSource.fetchAllTaskCategory(taskRequest);
                break;
            case TEST:
                mNetworkDataSource.fetchTestTaskCategory(taskRequest);
                break;
            case TOPICS:
                mNetworkDataSource.fetchCategories();
                break;
            default:
                throw new RuntimeException("Category " + category + " not contemplated!");
        }

    }


    public void login(final String email, final String password) {
        //TODO Check login in DB first, and then in Server
        mNetworkDataSource.login(email, password);
    }

    public void fetchReasons() {
        mNetworkDataSource.fetchReasons();
    }

    public void fetchVideoTestsDetails() {
        mNetworkDataSource.fetchVideoTestsDetails();
    }

    public void updateUserTask(UserTask userTask) {
        mNetworkDataSource.updateUserTask(userTask);
    }

    public LiveData<Resource<User>> updateUser(final User user) {
        return mNetworkDataSource.updateUser(user);
    }


    public LiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, String version) {
        return mNetworkDataSource.fetchSubtitle(videoId, languageCode, version);
    }

    public LiveData<Resource<UserTask>> fetchUserTask() {
        return mNetworkDataSource.fetchUserTask();
    }


    public LiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        return mNetworkDataSource.createUserTask(taskId, mSubtitleVersion);
    }

    public LiveData<Resource<Boolean>> requestAddToList(int taskId, String language,
                                                        String primaryAudioLanguageCode) {
        return mNetworkDataSource.addTaskToList(taskId, language, primaryAudioLanguageCode);

    }

    public LiveData<Resource<Boolean>> requestRemoveFromList(int taskId) {
        return mNetworkDataSource.removeTaskFromList(taskId);
    }


    public LiveData<Resource<Task[]>> search(String query) {
        return mNetworkDataSource.search(query);
    }

    public LiveData<Resource<VideoTopicSchema[]>> fetchCategories() {
        return mNetworkDataSource.responseFromFetchCategories();
    }

    public LiveData<Resource<Task[]>> searchByKeywordCategory(String category) {
        return mNetworkDataSource.searchByKeywordCategory(category);
    }


    public LiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mNetworkDataSource.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    public LiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion,
                                                                  UserTaskError userTaskError) {
        return mNetworkDataSource.updateErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    public MutableLiveData<Resource<User>> fetchUserDetails() {
        return mNetworkDataSource.responseFromFetchUserDetails();
    }

    public MutableLiveData<Resource<User>> updateUserDetails() {
        return mNetworkDataSource.responseFromUserDetailsUpdate();
    }

    public MutableLiveData<Resource<ErrorReason[]>> fetchReasonsResponse() {
        return mNetworkDataSource.responseFromFetchReasons();
    }

    public MutableLiveData<Resource<VideoTest[]>> fetchVideoTestsResponse() {
        return mNetworkDataSource.responseFromVideoTests();
    }

    private MutableLiveData<Resource<AuthResponse>> getAuthResponse() {
        return mNetworkDataSource.responseFromAuth();
    }


    public boolean verifyIfTaskIsInList(Task task) {
        Resource<TasksList> results = mNetworkDataSource.responseFromFetchMyListTasks().getValue();
        if (results != null && results.data != null && results.data.data != null) {
            if (results.data.data.length > 0) {
                for (Task entity : results.data.data) {
                    if (task.taskId == entity.taskId)
                        return true;
                }
            }
        }
        return false;
    }


    /**************************
     **  LOCAL DATA SOURCE   **
     **************************/

    public ArrayList<ErrorReason> getReasons() {
        return mPreferencesHelper.getReasons();
    }

    public VideoDuration getVideoDurationPref() {
        return mPreferencesHelper.getVideoDurationPref();
    }

    public boolean getTestingModePref() {
        return mPreferencesHelper.getTestingModePref();
    }

    public User getUser() {
        return mPreferencesHelper.getUser();
    }

    public void setUser(User user) {
        mPreferencesHelper.setUser(user);
    }

    public String getSubtitleSizePref() {
        return mPreferencesHelper.getSubtitleSizePref();
    }

    public List<VideoTest> getVideoTests() {
        return mPreferencesHelper.getVideoTests();
    }

    public String getAccessToken() {
        return mPreferencesHelper.getAccessToken();
    }

    public String getAudioLanguagePref() {
        return mPreferencesHelper.getAudioLanguagePref();
    }

    public String getInterfaceMode() {
        return mPreferencesHelper.getInterfaceModePref();
    }

    public String getInterfaceAppLanguage() {
        return mPreferencesHelper.getInterfaceLanguagePref();
    }
}
