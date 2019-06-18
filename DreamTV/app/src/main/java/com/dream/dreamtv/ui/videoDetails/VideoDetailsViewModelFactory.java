package com.dream.dreamtv.ui.videoDetails;

import com.dream.dreamtv.repository.AppRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link AppRepository}
 */
public class VideoDetailsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppRepository mRepository;

    public VideoDetailsViewModelFactory(AppRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new VideoDetailsViewModel(mRepository);
    }
}
