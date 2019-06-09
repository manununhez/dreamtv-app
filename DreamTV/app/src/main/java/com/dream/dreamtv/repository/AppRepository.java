package com.dream.dreamtv.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.AppExecutors;

import static com.dream.dreamtv.utils.Constants.TASKS_ALL_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_FINISHED_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;

public class AppRepository {
    private final static String TAG = AppRepository.class.getSimpleName();
    private static final int SYNC_INTERVAL_HOURS = 1;

    // For Singleton instantiation
    private static AppRepository INSTANCE;
    private final NetworkDataSource mNetworkDataSource;
    private boolean mInitialized = false;
    private AppExecutors mExecutors;

    private AppRepository(NetworkDataSource networkDataSource, AppExecutors executors) {
        mNetworkDataSource = networkDataSource;

    }


    public synchronized static AppRepository getInstance(/*TaskDao taskDao,*/
            NetworkDataSource networkDataSource,
            AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(/*taskDao, */networkDataSource, executors);
                Log.d(TAG, "Made new repository");
            }
        }
        return INSTANCE;
    }


    //*****************************
    //  Database related operation
    //****************************

//    private void insertAllTasks(final Task[] taskList) {
//        mTaskDao.bulkInsert(taskList);
//    }


    public LiveData<Resource<TasksList>> requestTaskByCategory(String category) {
        switch (category) {
            case TASKS_MY_LIST_CAT:
                return mNetworkDataSource.responseFromFetchMyListTasks();
            case TASKS_FINISHED_CAT:
                return mNetworkDataSource.responseFromFetchFinishedTasks();
            case TASKS_CONTINUE_CAT:
                return mNetworkDataSource.responseFromFetchContinueTasks();
            case TASKS_ALL_CAT:
                return mNetworkDataSource.responseFromFetchAllTasks();
            case TASKS_TEST_CAT:
                return mNetworkDataSource.responseFromFetchTestTasks();
            default:
                return mNetworkDataSource.responseFromFetchAllTasks();

        }
    }


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    public void initializeSyncData() {
        startFetchingData();
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


    //***********************************
    //     Network related operation
    //*********************************
    private void startFetchingData() {
//        new NetworkBoundResource<Task[], Task>(mExecutors){
////            @Override
////            protected void saveCallResult(@NonNull Task item) {
////
////            }
////
////            @Override
////            protected boolean shouldFetch(@Nullable Task[] data) {
////                return false;
////            }
////
////            @NonNull
////            @Override
////            protected LiveData<Task[]> loadFromDb() {
////                return null;
////            }
////
////            @NonNull
////            @Override
////            protected LiveData<JsonResponseBaseBean<Task>> createCall() {
////                mNetworkDataSource.syncData();
////                return null;
////            }
////        }.asLiveData();
        mNetworkDataSource.syncData();
    }


    public void login(final String email, final String password) {
        //TODO Check login in DB first, and then in Server
        mNetworkDataSource.login(email, password);
    }

    public void updateTasksCategory(String category) {
        switch (category) {
            case TASKS_MY_LIST_CAT:
                mNetworkDataSource.updateMyListTaskCategory();
                break;
            case TASKS_FINISHED_CAT:
                mNetworkDataSource.updateFinishedTaskCategory();
                break;
            case TASKS_CONTINUE_CAT:
                mNetworkDataSource.updateContinueTaskCategory();
                break;
            case TASKS_ALL_CAT:
                mNetworkDataSource.updateAllTaskCategory();
                break;
            case TASKS_TEST_CAT:
                mNetworkDataSource.updateTestTaskCategory();
                break;
            default:
                mNetworkDataSource.updateAllTaskCategory();
                break;
        }
    }


    public LiveData<Resource<User>> updateUser(final User user) {
        return mNetworkDataSource.updateUser(user);
    }


    public LiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, int version) {
        return mNetworkDataSource.fetchSubtitle(videoId, languageCode, version);
    }

    public LiveData<Resource<UserTask>> fetchUserTask() {
        return mNetworkDataSource.fetchUserTask();
    }


    public LiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        return mNetworkDataSource.createUserTask(taskId, mSubtitleVersion);
    }

    public MutableLiveData<Resource<Boolean>> requestAddToList(int taskId, String language, String primaryAudioLanguageCode) {
        return mNetworkDataSource.addTaskToList(taskId, language, primaryAudioLanguageCode);

    }

    public MutableLiveData<Resource<Boolean>> requestRemoveFromList(int taskId) {
        return mNetworkDataSource.removeTaskFromList(taskId);
    }


    public MutableLiveData<Resource<UserTaskError[]>> errorsUpdate(UserTaskError userTaskError, boolean saveError) {
        return mNetworkDataSource.errorsUpdate(userTaskError, saveError);
    }

    public void updateUserTask(UserTask userTask) {
        mNetworkDataSource.updateUserTask(userTask);
    }

}
