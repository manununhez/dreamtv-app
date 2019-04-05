package com.dream.dreamtv.repository;

import android.util.Log;

import com.dream.dreamtv.db.dao.TaskDao;
import com.dream.dreamtv.db.dao.UserDao;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.db.entity.UserEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.Constants;

import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class AppRepository {
    private final static String TAG = AppRepository.class.getSimpleName();
    // For Singleton instantiation
    private static AppRepository INSTANCE;
    private final NetworkDataSource mNetworkDataSource;
    private boolean mInitialized = false;
    private UserDao mUserDao;
    private TaskDao mTaskDao;

    private AppRepository(UserDao userDao, TaskDao taskDao, NetworkDataSource networkDataSource) {
        mNetworkDataSource = networkDataSource;
        mUserDao = userDao;
        mTaskDao = taskDao;


        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        // Group of current weathers that corresponds to the weather of all cities saved in the DB. Updated
        // using job schedules
        LiveData<Resource<UserEntity>> newUserEntity = responseFromUserUpdate();
        LiveData<Resource<TaskEntity[]>> newTasksEntities = responseFromTasks();
        LiveData<Resource<TaskEntity[]>> newContinueTasksEntities = responseFromContinueTasks();

        newUserEntity.observeForever(new Observer<Resource<UserEntity>>() {
            @Override
            public void onChanged(@Nullable Resource<UserEntity> userFromNetwork) {
                if (userFromNetwork != null) {
                    if (userFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        insertUser(userFromNetwork.data);

                        Log.d(TAG, "New values inserted");
                    } else if (userFromNetwork.status.equals(Resource.Status.ERROR)) {
                        //TODO do something
                        if (userFromNetwork.message != null)
                            Log.d(TAG, userFromNetwork.message);
                        else
                            Log.d(TAG, "Status ERROR");
                    }
                }

            }
        });

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

    }

    public synchronized static AppRepository getInstance(UserDao userDao, TaskDao taskDao,
                                                         NetworkDataSource networkDataSource) {
        Log.d(TAG, "Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(userDao, taskDao, networkDataSource);
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
    private void insertUser(final UserEntity userEntity) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.insert(userEntity);
            }
        });
    }


    private void insertAllTasks(final TaskEntity[] taskList) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mTaskDao.bulkInsert(taskList);
            }
        });
    }

    private LiveData<UserEntity> getAuthUser(String email, String password) {
        return mUserDao.getUser(email, password);
    }

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

    /**
     * Used for search weather by city name.
     *
     * @return Current weather by city name
     */
    public LiveData<Resource<UserEntity>> responseFromUserUpdate() {
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

}
