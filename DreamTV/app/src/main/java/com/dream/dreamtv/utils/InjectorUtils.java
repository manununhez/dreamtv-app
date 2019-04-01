package com.dream.dreamtv.utils;

import android.content.Context;

import com.dream.dreamtv.db.AppRoomDatabase;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ui.Main.MainViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    public static AppRepository provideRepository(Context context) {
        AppRoomDatabase database = AppRoomDatabase.getInstance(context.getApplicationContext());
        NetworkDataSource networkDataSource = NetworkDataSource.getInstance(context.getApplicationContext());
        return AppRepository.getInstance(database.userDao(), networkDataSource);
    }

    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }


}