package com.dream.dreamtv.utils;

import android.content.Context;

import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ui.Main.MainViewModelFactory;
import com.dream.dreamtv.ui.PlayVideo.PlaybackViewModelFactory;
import com.dream.dreamtv.ui.Search.SearchViewModelFactory;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    public static AppRepository provideRepository(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        NetworkDataSource networkDataSource = NetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return AppRepository.getInstance(networkDataSource, executors);
    }

    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }


    public static VideoDetailsViewModelFactory provideVideoDetailsViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new VideoDetailsViewModelFactory(repository);
    }

    public static PlaybackViewModelFactory providePlaybackViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new PlaybackViewModelFactory(repository);
    }

    public static SearchViewModelFactory provideSearchViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new SearchViewModelFactory(repository);
    }


}