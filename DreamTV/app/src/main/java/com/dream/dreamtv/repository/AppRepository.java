package com.dream.dreamtv.repository;

import android.util.Log;

import com.dream.dreamtv.db.dao.TaskDao;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.AppExecutors;
import com.dream.dreamtv.utils.Constants;

import androidx.lifecycle.LiveData;

public class AppRepository {
    private final static String TAG = AppRepository.class.getSimpleName();
    private static final int SYNC_INTERVAL_HOURS = 1;

    // For Singleton instantiation
    private static AppRepository INSTANCE;
    private final NetworkDataSource mNetworkDataSource;
    private boolean mInitialized = false;
    private TaskDao mTaskDao;
    private AppExecutors mExecutors;

    private AppRepository(TaskDao taskDao, NetworkDataSource networkDataSource, AppExecutors executors) {
        mNetworkDataSource = networkDataSource;
        mTaskDao = taskDao;
        mExecutors = executors;


        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        // Group of current weathers that corresponds to the weather of all cities saved in the DB. Updated
        // using job schedules
        LiveData<Resource<TaskEntity[]>> newTasksEntities = responseFromTasks();
        LiveData<Resource<TaskEntity[]>> newContinueTasksEntities = responseFromContinueTasks();
        LiveData<Resource<TaskEntity[]>> newTestTasksEntities = responseFromTestTasks();
        LiveData<Resource<TaskEntity[]>> newFinishedTasksEntities = responseFromFinishedTasks();
        LiveData<Resource<TaskEntity[]>> newMyListTasksEntities = responseFromMyListTasks();


        newTasksEntities.observeForever(tasksFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                if (tasksFromNetwork != null) {
                    if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertAllTasks(tasksFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (tasksFromNetwork.message != null)
                            Log.d(TAG, tasksFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }
            });
        });


        newContinueTasksEntities.observeForever(tasksFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                if (tasksFromNetwork != null) {
                    if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertAllTasks(tasksFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (tasksFromNetwork.message != null)
                            Log.d(TAG, tasksFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }
            });
        });


        newTestTasksEntities.observeForever(tasksFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                if (tasksFromNetwork != null) {
                    if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertAllTasks(tasksFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (tasksFromNetwork.message != null)
                            Log.d(TAG, tasksFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }
            });
        });

        newFinishedTasksEntities.observeForever(tasksFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                if (tasksFromNetwork != null) {
                    if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertAllTasks(tasksFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (tasksFromNetwork.message != null)
                            Log.d(TAG, tasksFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }
            });
        });

        newMyListTasksEntities.observeForever(tasksFromNetwork -> {
            mExecutors.diskIO().execute(() -> {
                if (tasksFromNetwork != null) {
                    if (tasksFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertAllTasks(tasksFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (tasksFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (tasksFromNetwork.message != null)
                            Log.d(TAG, tasksFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }
            });
        });

    }


    public synchronized static AppRepository getInstance(TaskDao taskDao,
                                                         NetworkDataSource networkDataSource,
                                                         AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(taskDao, networkDataSource, executors);
                Log.d(TAG, "Made new repository");
            }
        }
        return INSTANCE;
    }


    //*****************************
    //  Database related operation
    //****************************

    private void insertAllTasks(final TaskEntity[] taskList) {
        mTaskDao.bulkInsert(taskList);
    }


    public LiveData<TaskEntity[]> requestTaskByCategory(String category) {
        return mTaskDao.getTasksByCategory(category);
    }


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    public void initializeSyncData() {

//        // Only perform initialization once per app lifetime. If initialization has already been
//        // performed, we have nothing to do in this method.
//        if (mInitialized) return;
//        mInitialized = true;

        mExecutors.diskIO().execute(() -> {
//            if (isFetchNeeded()) {
            deleteOldData();
            startFetchingData();
//            }
        });
    }

    private void deleteOldData() {
        mTaskDao.deleteAll();
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
        mNetworkDataSource.syncData();
    }



    private LiveData<Resource<TaskEntity[]>> responseFromTasks() {
        return mNetworkDataSource.responseFromTasks();
    }

    private LiveData<Resource<TaskEntity[]>> responseFromContinueTasks() {
        return mNetworkDataSource.responseFromContinueTasks();
    }

    private LiveData<Resource<TaskEntity[]>> responseFromTestTasks() {
        return mNetworkDataSource.responseFromTestTasks();
    }

    private LiveData<Resource<TaskEntity[]>> responseFromFinishedTasks() {
        return mNetworkDataSource.responseFromFinishedTasks();
    }

    private LiveData<Resource<TaskEntity[]>> responseFromMyListTasks() {
        return mNetworkDataSource.responseFromMyListTasks();
    }


    public void requestFromLogin(final String email, final String password) {
        mNetworkDataSource.login(email, password);
    }


    public void requestFromUserUpdate(final User user) {
        mNetworkDataSource.userDetailsUpdate(user);
    }

    public LiveData<Resource<User>> responseFromUserUpdate() {
        return mNetworkDataSource.responseFromUserUpdate();
    }


    public void requestAddToList(TaskEntity taskEntity) {
        //TODO add to the db and with webservice
        mExecutors.diskIO().execute(() -> {
            mTaskDao.insert(taskEntity);
        });


    }

    public void requestRemoveFromList(int taskId, String category) {
        //TODO remove from the db and from the webservice
        mExecutors.diskIO().execute(() -> {
            mTaskDao.deleteByTaskIdAndCategory(taskId, category);
        });
    }

    public LiveData<TaskEntity> verifyIfTaskIsInList(TaskEntity taskEntity, String category) {
        return mTaskDao.verifyTask(taskEntity.task_id, category);
    }
}
