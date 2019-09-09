package com.dream.dreamtv;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ui.categories.CategoryViewModel;
import com.dream.dreamtv.ui.home.HomeViewModel;
import com.dream.dreamtv.ui.playVideo.PlaybackViewModel;
import com.dream.dreamtv.ui.preferences.PreferencesViewModel;
import com.dream.dreamtv.ui.search.SearchViewModel;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsViewModel;
import com.dream.dreamtv.ui.welcome.WelcomeViewModel;

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

        } else if (modelClass.isAssignableFrom(PreferencesViewModel.class)) {
            //noinspection unchecked
            return (T) new PreferencesViewModel(mRepository);

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
