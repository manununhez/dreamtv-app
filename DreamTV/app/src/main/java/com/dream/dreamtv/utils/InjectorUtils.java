package com.dream.dreamtv.utils;

import android.content.Context;

import com.dream.dreamtv.db.AppRoomDatabase;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ui.Main.MainViewModelFactory;
import com.dream.dreamtv.ui.Settings.SettingsViewModelFactory;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    public static AppRepository provideRepository(Context context) {
        AppRoomDatabase database = AppRoomDatabase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        NetworkDataSource networkDataSource = NetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return AppRepository.getInstance(database.taskDao(), networkDataSource, executors);
    }

    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }

    public static SettingsViewModelFactory provideSettingsViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new SettingsViewModelFactory(repository);
    }

    public static VideoDetailsViewModelFactory provideVideoDetailsViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new VideoDetailsViewModelFactory(repository);
    }


}