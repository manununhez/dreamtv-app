package com.dreamproject.dreamtv.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dreamproject.dreamtv.data.local.prefs.AppPreferencesHelper;
import com.dreamproject.dreamtv.data.model.Authentication;
import com.dreamproject.dreamtv.data.model.Category;
import com.dreamproject.dreamtv.data.model.ErrorReason;
import com.dreamproject.dreamtv.data.model.Subtitle;
import com.dreamproject.dreamtv.data.model.Task;
import com.dreamproject.dreamtv.data.model.TasksList;
import com.dreamproject.dreamtv.data.model.User;
import com.dreamproject.dreamtv.data.model.UserTask;
import com.dreamproject.dreamtv.data.model.UserTaskError;
import com.dreamproject.dreamtv.data.model.VideoDuration;
import com.dreamproject.dreamtv.data.model.VideoTest;
import com.dreamproject.dreamtv.data.model.VideoTopic;
import com.dreamproject.dreamtv.data.networking.NetworkDataSourceImpl;
import com.dreamproject.dreamtv.data.model.Resource;
import com.dreamproject.dreamtv.data.model.Resource.Status;
import com.dreamproject.dreamtv.utils.AppExecutors;

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
        MutableLiveData<Resource<Authentication>> auth = getAuthResponse();
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
                    if (data != null) {
                        // Insert our new user data into preferences
                        setUser(data);

                        Timber.d("Update user data - setUser() called: subLanguage = %s", data.getSubLanguage());
                    }
                }
            }
        });

        reasonsResponse.observeForever(reasonsResource -> {
            if (reasonsResource != null) {
                Status status = reasonsResource.status;
                ErrorReason[] data = reasonsResource.data;

                if (status.equals(Status.SUCCESS)) {
                    // Insert our new user data into preferences

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
                    // Insert our new user data into preferences

                    mPreferencesHelper.setVideoTests(data);

                    Timber.d("VideoTests data - setVideoTests() called");
                }
            }
        });

        auth.observeForever(resource -> {
            if (resource != null) {
                Status status = resource.status;
                Authentication data = resource.data;

                if (status.equals(Status.SUCCESS)) {
                    if (data != null) {
                        // Insert our new user data into preferences

                        mPreferencesHelper.setAccessToken(data.token);
                        Timber.d("AccessToken updated - setAccessToken() called");
                    }
                }
            }
        });

    }


    public synchronized static AppRepository getInstance(NetworkDataSourceImpl networkDataSource,
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

    /**
     * Fetch tasks by category
     *
     * @param category Specific category
     * @return list of tasks from the selected category
     */
    public LiveData<Resource<TasksList[]>> fetchTasks(Category.Type category) {
        VideoDuration videoDuration = getVideoDurationPref();

        return mNetworkDataSource.fetchTasks(category, videoDuration);

    }

    /**
     * Fetch all tasks from all categories
     *
     * @return list of tasks from all categories
     */
    public LiveData<Resource<TasksList[]>> fetchTasks() {
        VideoDuration videoDuration = getVideoDurationPref();

        return mNetworkDataSource.fetchTasks(Category.Type.ALL, videoDuration);

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

    public boolean verifyIfTaskIsInList(Task task) {
        //We look in the last response from fetchTask to find MyList categories. Once we find it, we proceed to find the
        //specific task we want to know if belongs to my list or not.
        //TODO replace this by querying the DB instead
        Resource<TasksList[]> results = mNetworkDataSource.responseFromFetchTasks().getValue();
        if (results != null && results.data != null) {
            for (TasksList taskList : results.data) {
                if (taskList.getCategory().getCategoryType().equals(Category.Type.MY_LIST)) {
                    if (taskList.getTasks().length > 0) {
                        for (Task entity : taskList.getTasks()) {
                            if (task.getTaskId() == entity.getTaskId())
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public LiveData<Resource<User>> updateUser(final User user) {
        return mNetworkDataSource.updateUser(user);
    }

    public LiveData<Resource<Subtitle>> fetchSubtitle(String videoId, String languageCode, String version) {
        return mNetworkDataSource.fetchSubtitle(videoId, languageCode, version);
    }

    public LiveData<Resource<UserTask>> fetchUserTask() {
        return mNetworkDataSource.fetchUserTask();
    }

    public LiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        return mNetworkDataSource.createUserTask(taskId, mSubtitleVersion);
    }

    public LiveData<Resource<Boolean>> requestAddToList(Task task) {
        return mNetworkDataSource.addTaskToList(task.getTaskId(), task.getSubLanguage(), task.getVideo().audioLanguage);

    }

    public LiveData<Resource<Boolean>> requestRemoveFromList(Task task) {
        return mNetworkDataSource.removeTaskFromList(task.getTaskId());
    }

    public LiveData<Resource<Task[]>> search(String query) {
        return mNetworkDataSource.search(query);
    }

    public LiveData<Resource<VideoTopic[]>> fetchCategories() {
        return mNetworkDataSource.fetchCategories();
    }

    public LiveData<Resource<Task[]>> searchByKeywordCategory(String category) {
        return mNetworkDataSource.searchByKeywordCategory(category);
    }

    public LiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mNetworkDataSource.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    public LiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
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

    private MutableLiveData<Resource<Authentication>> getAuthResponse() {
        return mNetworkDataSource.responseFromAuth();
    }


    /**************************
     **  LOCAL DATA SOURCE   **
     **************************/

    public ArrayList<ErrorReason> getReasons() {
        return mPreferencesHelper.getReasons();
    }

    public User getUser() {
        return mPreferencesHelper.getUser();
    }

    public void setUser(User user) {
        mPreferencesHelper.setUser(user);
    }

    public VideoDuration getVideoDurationPref() {
        return mPreferencesHelper.getVideoDurationPref();
    }

    public boolean getTestingModePref() {
        return mPreferencesHelper.getTestingModePref();
    }

    public List<VideoTest> getVideoTests() {
        return mPreferencesHelper.getVideoTests();
    }

    public String getSubtitleSizePref() {
        return mPreferencesHelper.getSubtitleSizePref();
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
