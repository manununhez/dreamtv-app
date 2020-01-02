package com.dreamproject.dreamtv;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dreamproject.dreamtv.repository.AppRepository;
import com.dreamproject.dreamtv.ui.categories.CategoryViewModel;
import com.dreamproject.dreamtv.ui.home.HomeViewModel;
import com.dreamproject.dreamtv.ui.playVideo.PlaybackViewModel;
import com.dreamproject.dreamtv.ui.preferences.AppPreferencesViewModel;
import com.dreamproject.dreamtv.ui.preferences.VideoPreferencesViewModel;
import com.dreamproject.dreamtv.ui.search.SearchViewModel;
import com.dreamproject.dreamtv.ui.videoDetails.VideoDetailsViewModel;
import com.dreamproject.dreamtv.ui.welcome.WelcomeViewModel;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link AppRepository}
 */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppRepository mRepository;

    public ViewModelFactory(AppRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            //noinspection unchecked
            return (T) new HomeViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(CategoryViewModel.class)) {
            //noinspection unchecked
            return (T) new CategoryViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(PlaybackViewModel.class)) {
            //noinspection unchecked
            return (T) new PlaybackViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(AppPreferencesViewModel.class)) {
            //noinspection unchecked
            return (T) new AppPreferencesViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(VideoPreferencesViewModel.class)) {
            //noinspection unchecked
            return (T) new VideoPreferencesViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            //noinspection unchecked
            return (T) new SearchViewModel(mRepository);

        } else if (modelClass.isAssignableFrom(VideoDetailsViewModel.class)) {
            //noinspection unchecked
            return (T) new VideoDetailsViewModel(mRepository);

        } else {
            //noinspection unchecked
            return (T) new WelcomeViewModel(mRepository);
        }
    }
}
