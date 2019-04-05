package com.dream.dreamtv.db;


import android.content.Context;
import android.os.AsyncTask;

import com.dream.dreamtv.db.dao.TaskDao;
import com.dream.dreamtv.db.dao.UserDao;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.db.entity.UserEntity;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;


/*
  Created by manuel on 21,August,2018
 */

/**
 * {@link AppRoomDatabase} database for the application including a table for {@link UserEntity}
 * with the DAO {@link UserDao}.
 */
@Database(entities = {UserEntity.class, TaskEntity.class}, version = 1)
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
                            .addCallback(sRoomDatabaseCallback)
                            .build();




                }
            }
        }
        return INSTANCE;
    }

    // The associated DAOs for the database
    public abstract UserDao userDao();
    public abstract TaskDao taskDao();



    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){

        @Override
        public void onOpen (@NonNull SupportSQLiteDatabase db){
            super.onOpen(db);
            // If you want to keep the data through app restarts,
            // comment out the following line.
            new ClearDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database in the background.
     * If you want to start with more words, just add them.
     */
    private static class ClearDbAsync extends AsyncTask<Void, Void, Void> {

        private final UserDao mUserDao;
        private final TaskDao mTaskDao;

        ClearDbAsync(AppRoomDatabase db) {
            mUserDao = db.userDao();
            mTaskDao = db.taskDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            mUserDao.deleteAll();
            mTaskDao.deleteAll();

            return null;
        }
    }

}
