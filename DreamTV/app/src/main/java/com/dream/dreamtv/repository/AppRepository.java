package com.dream.dreamtv.repository;

import android.util.Log;

import com.dream.dreamtv.db.dao.UserDao;
import com.dream.dreamtv.db.entity.UserEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.utils.Utils;

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

    private AppRepository(UserDao userDao, NetworkDataSource networkDataSource) {
        mNetworkDataSource = networkDataSource;
        mUserDao = userDao;


        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        // Group of current weathers that corresponds to the weather of all cities saved in the DB. Updated
        // using job schedules
        LiveData<Resource<UserEntity>> newUserEntity = responseFromLogin();

        newUserEntity.observeForever(new Observer<Resource<UserEntity>>() {
            @Override
            public void onChanged(@Nullable Resource<UserEntity> userFromNetwork) {
                if (userFromNetwork != null) {
                    if (userFromNetwork.status.equals(Resource.Status.SUCCESS)) {
                        // Insert our new weather data into the database
                        AppRepository.this.insert(userFromNetwork.data);

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
    }


    public synchronized static AppRepository getInstance(UserDao userDao, NetworkDataSource networkDataSource) {
        Log.d(TAG, "Getting the repository");
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                INSTANCE = new AppRepository(userDao, networkDataSource);
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
     *
     *  List of updated weather entries, ready to be inserted into the DB.
     */
    private void insert(final UserEntity userEntity) {
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mUserDao.insert(userEntity);
            }
        });
    }


    //***********************************
    //     Network related operation
    //*********************************


    /**
     * Fetch And Insert Weather from network by city name
     *
     *
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
    public LiveData<Resource<UserEntity>> responseFromLogin() {
        return mNetworkDataSource.responseFromLogin();
    }

}
