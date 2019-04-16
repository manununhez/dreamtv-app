package com.dream.dreamtv.repository;

import android.util.Log;

import com.dream.dreamtv.db.dao.TaskDao;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.Constants;

import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class AppRepository {
    private final static String TAG = AppRepository.class.getSimpleName();
    // For Singleton instantiation
    private static AppRepository INSTANCE;
    private final NetworkDataSource mNetworkDataSource;
    private boolean mInitialized = false;
    private TaskDao mTaskDao;

    private AppRepository(TaskDao taskDao, NetworkDataSource networkDataSource) {
        mNetworkDataSource = networkDataSource;
        mTaskDao = taskDao;


        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        // Group of current weathers that corresponds to the weather of all cities saved in the DB. Updated
        // using job schedules
        LiveData<Resource<TaskEntity[]>> newTasksEntities = responseFromTasks();
        LiveData<Resource<TaskEntity[]>> newContinueTasksEntities = responseFromContinueTasks();
        LiveData<Resource<TaskEntity[]>> newTestTasksEntities = responseFromTestTasks();
        LiveData<Resource<TaskEntity[]>> newFinishedTasksEntities = responseFromFinishedTasks();
        LiveData<Resource<TaskEntity[]>> newMyListTasksEntities = responseFromMyListTasks();


        newTasksEntities.observeForever(new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> tasksFromNetwork) {
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
            }
        });


        newContinueTasksEntities.observeForever(new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> tasksFromNetwork) {
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
            }
        });


        newTestTasksEntities.observeForever(new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> tasksFromNetwork) {
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
            }
        });

        newFinishedTasksEntities.observeForever(new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> tasksFromNetwork) {
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
            }
        });

        newMyListTasksEntities.observeForever(new Observer<Resource<TaskEntity[]>>() {
            @Override
            public void onChanged(Resource<TaskEntity[]> tasksFromNetwork) {
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
            }
        });

    }


    public synchronized static AppRepository getInstance(TaskDao taskDao,
                                                         NetworkDataSource networkDataSource) {
        Log.d(TAG, "Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(taskDao, networkDataSource);
                Log.d(TAG, "Made new repository");
            }
        }
        return INSTANCE;
    }


    //*****************************
    //  Database related operation
    //****************************

    /**
     * Inserts the list of current weather IDs just fetched from the weather server.
     * <p>
     * List of updated weather entries, ready to be inserted into the DB.
     */
//    private void insertUser(final UserEntity userEntity) {
//        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
//                mUserDao.insert(userEntity);
//            }
//        });
//    }
//
//    private void updateUser(final UserEntity userEntity) {
//        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
//                mUserDao.update(userEntity);
//            }
//        });
//    }


    private void insertAllTasks(final TaskEntity[] taskList) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mTaskDao.bulkInsert(taskList);
            }
        });
    }


//    private UserEntity getAuthUser(String email) {
//        return mUserDao.getUser(email);
//    }

    public LiveData<TaskEntity[]> requestAllTasks() {
        return mTaskDao.getAllTasksByCategory(Constants.TASKS_ALL);
    }

    public LiveData<TaskEntity[]> requestContinueTasks() {
        return mTaskDao.getAllTasksByCategory(Constants.TASKS_CONTINUE);
    }


    //***********************************
    //     Network related operation
    //*********************************


    /**
     * Fetch And Insert Weather from network by city name
     */
    public void requestFromLogin(final String email, final String password) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mNetworkDataSource.login(email, password);
            }
        });
    }


    public void requestFromUserUpdate(final User user) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mNetworkDataSource.userDetailsUpdate(user);
            }
        });
    }

    /**
     * Used for search weather by city name.
     *
     * @return Current weather by city name
     */
    public LiveData<Resource<User>> responseFromUserUpdate() {
        return mNetworkDataSource.responseFromUserUpdate();
    }


    /**
     * Used for search weather by city name.
     *
     * @return Current weather by city name
     */
    public LiveData<Resource<TaskEntity[]>> responseFromTasks() {
        return mNetworkDataSource.responseFromTasks();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromContinueTasks() {
        return mNetworkDataSource.responseFromContinueTasks();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromTestTasks() {
        return mNetworkDataSource.responseFromTestTasks();
    }

    public void requestSyncData() {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mNetworkDataSource.syncData();
            }
        });
    }

    public LiveData<Resource<String>> responseFromSyncData() {
        return mNetworkDataSource.responseFromSyncData();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromFinishedTasks() {
        return mNetworkDataSource.responseFromFinishedTasks();
    }

    public LiveData<Resource<TaskEntity[]>> responseFromMyListTasks() {
        return mNetworkDataSource.responseFromMyListTasks();
    }
}
