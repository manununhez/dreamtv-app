package com.dream.dreamtv.repository;

import android.util.Log;

import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.AppExecutors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
//    private TaskDao mTaskDao;
//    private AppExecutors mExecutors;

    private AppRepository(/*TaskDao taskDao, */NetworkDataSource networkDataSource, AppExecutors executors) {
        mNetworkDataSource = networkDataSource;
//        mTaskDao = taskDao;
//        mExecutors = executors;


//        // As long as the repository exists, observe the network LiveData.
//        // If that LiveData changes, update the database.
//        mNetworkDataSource.responseFromFetchAllTasks().observeForever(
//                tasksFromNetwork -> mExecutors.diskIO().execute(() -> {
//                    if (tasksFromNetwork != null) {
//                        if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
//                            // Insert our new weather data into the database
//                            insertAllTasks(tasksFromNetwork.data);
//
//                            Log.d(TAG, "New values inserted");
//                        } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
//                            //TODO do something
//                            if (tasksFromNetwork.message != null)
//                                Log.d(TAG, tasksFromNetwork.message);
//                            else
//                                Log.d(TAG, "Status ERROR");
//                        }
//                    }
//                }));
//
//
//        mNetworkDataSource.responseFromFetchContinueTasks().observeForever(
//                tasksFromNetwork -> mExecutors.diskIO().execute(() -> {
//                    if (tasksFromNetwork != null) {
//                        if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
//                            // Insert our new weather data into the database
//                            insertAllTasks(tasksFromNetwork.data);
//
//                            Log.d(TAG, "New values inserted");
//                        } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
//                            //TODO do something
//                            if (tasksFromNetwork.message != null)
//                                Log.d(TAG, tasksFromNetwork.message);
//                            else
//                                Log.d(TAG, "Status ERROR");
//                        }
//                    }
//                }));
//
//
//        mNetworkDataSource.responseFromFetchTestTasks().observeForever(
//                tasksFromNetwork -> mExecutors.diskIO().execute(() -> {
//                    if (tasksFromNetwork != null) {
//                        if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
//                            // Insert our new weather data into the database
//                            insertAllTasks(tasksFromNetwork.data);
//
//                            Log.d(TAG, "New values inserted");
//                        } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
//                            //TODO do something
//                            if (tasksFromNetwork.message != null)
//                                Log.d(TAG, tasksFromNetwork.message);
//                            else
//                                Log.d(TAG, "Status ERROR");
//                        }
//                    }
//                }));
//
//        mNetworkDataSource.responseFromFetchFinishedTasks().observeForever(
//                tasksFromNetwork -> mExecutors.diskIO().execute(() -> {
//                    if (tasksFromNetwork != null) {
//                        if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
//                            // Insert our new weather data into the database
//                            insertAllTasks(tasksFromNetwork.data);
//
//                            Log.d(TAG, "New values inserted");
//                        } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
//                            //TODO do something
//                            if (tasksFromNetwork.message != null)
//                                Log.d(TAG, tasksFromNetwork.message);
//                            else
//                                Log.d(TAG, "Status ERROR");
//                        }
//                    }
//                }));
//
//        mNetworkDataSource.responseFromFetchMyListTasks().observeForever(
//                tasksFromNetwork -> mExecutors.diskIO().execute(() -> {
//                    if (tasksFromNetwork != null) {
//                        if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
//                            // Insert our new weather data into the database
//                            insertAllTasks(tasksFromNetwork.data);
//
//                            Log.d(TAG, "New values inserted");
//                        } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
//                            //TODO do something
//                            if (tasksFromNetwork.message != null)
//                                Log.d(TAG, tasksFromNetwork.message);
//                            else
//                                Log.d(TAG, "Status ERROR");
//                        }
//                    }
//                }));

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

//    private void insertAllTasks(final TaskEntity[] taskList) {
//        mTaskDao.bulkInsert(taskList);
//    }


    public LiveData<Resource<TaskEntity[]>> requestTaskByCategory(String category) {
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

//    public LiveData<TaskEntity[]> requestTaskByCategory(String category) {
//        return mTaskDao.requestTaskByCategory(category);
//    }

//    private LiveData<Resource<TaskEntity[]>> responseFromFetchAllTasks() {
//        return mNetworkDataSource.responseFromFetchAllTasks();
//    }
//
//    private LiveData<Resource<TaskEntity[]>> responseFromFetchContinueTasks() {
//        return mNetworkDataSource.responseFromFetchContinueTasks();
//    }
//
//    private LiveData<Resource<TaskEntity[]>> responseFromFetchTestTasks() {
//        return mNetworkDataSource.responseFromFetchTestTasks();
//    }
//
//    private LiveData<Resource<TaskEntity[]>> responseFromFetchFinishedTasks() {
//        return mNetworkDataSource.responseFromFetchFinishedTasks();
//    }
//
//    private LiveData<Resource<TaskEntity[]>> responseFromFetchMyListTasks() {
//        return mNetworkDataSource.responseFromFetchMyListTasks();
//    }


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    public void initializeSyncData() {

//        // Only perform initialization once per app lifetime. If initialization has already been
//        // performed, we have nothing to do in this method.
//        if (mInitialized) return;
//        mInitialized = true;

        //            if (isFetchNeeded()) { //TODO add this isFetchNeeded. Use NetworkBoundResource Class
//            }
//        mExecutors.diskIO().execute(this::deleteOldData);

        startFetchingData();
    }

//    private void deleteOldData() {
//        mTaskDao.deleteAll();
//    }

    public boolean verifyIfTaskIsInList(TaskEntity taskEntity) {

        Resource<TaskEntity[]> results = mNetworkDataSource.responseFromFetchMyListTasks().getValue();
        if (results != null && results.data != null) {
            if (results.data.length > 0) {
                for (TaskEntity entity : results.data) {
                    if (taskEntity.taskId == entity.taskId)
                        return true;
                }
            }
        }
//        return mTaskDao.verifyTask(taskEntity.taskId, TASKS_MY_LIST_CAT);


        return false;
    }


    /**
     * Checks if there are enough days of future weather for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
//    private boolean isFetchNeeded() {
//        long now = Utils.getUnixTimeNowInSecs();
//        long lastUpdate = getLastUpdateTime();
//
//        return ((now - lastUpdate) >= (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS));
//    }

    /**
     * Obtains the last dt of the weather entries in DB. This is used to determine
     * if new values should be fetched from the weather server.
     *
     * @return long dt
     * //
     */
//    public long getLastUpdateTime() {
//        return mTaskDao.getLastUpdateTime();
//    }


    //***********************************
    //     Network related operation
    //*********************************
    private void startFetchingData() {
//        new NetworkBoundResource<TaskEntity[], TaskEntity>(mExecutors){
////            @Override
////            protected void saveCallResult(@NonNull TaskEntity item) {
////
////            }
////
////            @Override
////            protected boolean shouldFetch(@Nullable TaskEntity[] data) {
////                return false;
////            }
////
////            @NonNull
////            @Override
////            protected LiveData<TaskEntity[]> loadFromDb() {
////                return null;
////            }
////
////            @NonNull
////            @Override
////            protected LiveData<JsonResponseBaseBean<TaskEntity>> createCall() {
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

    public LiveData<Resource<UserTask>> fetchUserTask(int taskId) {
        return mNetworkDataSource.fetchUserTask(taskId);
    }


    public LiveData<Resource<UserTask>> createUserTask(TaskEntity taskEntity, int mSubtitleVersion) {
        return mNetworkDataSource.createUserTask(taskEntity.taskId, mSubtitleVersion);
    }

    public MutableLiveData<Resource<Boolean>> requestAddToList(TaskEntity taskEntity) {

//        mExecutors.diskIO().execute(() -> mTaskDao.insert(taskEntity));

        return mNetworkDataSource.addTaskToList(taskEntity.taskId, taskEntity.language, taskEntity.video.primaryAudioLanguageCode);

    }

    public MutableLiveData<Resource<Boolean>> requestRemoveFromList(int taskId) {
//        mExecutors.diskIO().execute(() -> mTaskDao.deleteByTaskIdAndCategory(taskId, category));

        return mNetworkDataSource.removeTaskFromList(taskId);
    }


    public void updateUserTask(UserTask userTask) {
        mNetworkDataSource.updateUserTask(userTask);
    }

    public void saveErrors(UserTaskError userTaskError) {
        mNetworkDataSource.saveErrors(userTaskError);
    }
}
