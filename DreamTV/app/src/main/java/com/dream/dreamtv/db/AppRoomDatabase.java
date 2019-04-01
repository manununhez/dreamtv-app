package com.dream.dreamtv.db;


import android.content.Context;
import android.util.Log;

import com.dream.dreamtv.db.dao.UserDao;
import com.dream.dreamtv.db.entity.UserEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/*
  Created by manuel on 21,August,2018
 */

/**
 * {@link AppRoomDatabase} database for the application including a table for {@link UserEntity}
 * with the DAO {@link UserDao}.
 */
@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppRoomDatabase extends RoomDatabase {
    private static final String TAG = AppRoomDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "dreamtv_database";
    // For Singleton instantiation
    private static AppRoomDatabase INSTANCE;

    public static AppRoomDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, AppRoomDatabase.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();

                    Log.d(TAG, "Created a New Database ");


                }
            }
        }
        return INSTANCE;
    }

    // The associated DAOs for the database
    public abstract UserDao userDao();

}
