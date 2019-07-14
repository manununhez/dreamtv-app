package com.dream.dreamtv.utils;

import android.content.Context;

import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ui.categories.CategoryViewModelFactory;
import com.dream.dreamtv.ui.home.HomeViewModelFactory;
import com.dream.dreamtv.ui.playVideo.PlaybackViewModelFactory;
import com.dream.dreamtv.ui.search.SearchViewModelFactory;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsViewModelFactory;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    public static AppRepository provideRepository(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        NetworkDataSource networkDataSource = NetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return AppRepository.getInstance(networkDataSource, executors);
    }

    public static HomeViewModelFactory provideMainViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new HomeViewModelFactory(repository);
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

    public static CategoryViewModelFactory provideCategoryViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new CategoryViewModelFactory(repository);
    }


}